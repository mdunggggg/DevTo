# Offline-First Article Insert Strategy

Tài liệu mô tả cách `:data` lưu cache article cho offline-first: cách tổ chức bảng, cách insert/refresh theo từng filter, và lý do chọn many-to-many thay vì gắn `cache_key` trực tiếp vào article.

---

## 1. Mục tiêu

- **Offline-first**: UI luôn đọc từ Room (single source of truth). Mạng chỉ là nguồn đồng bộ.
- **Per-filter cache**: mỗi tổ hợp filter (`tag`, `tags`, `state`, `top`, `collectionId`, ...) có cache riêng, paging state riêng, TTL riêng.
- **Article không nhân bản**: một `article.id` chỉ tồn tại 1 row trong bảng `articles`, dù nó thuộc về N filter khác nhau.

---

## 2. Các thành phần

| Layer  | Class | Vai trò |
|--------|-------|---------|
| Remote | `ArticleApi` (Retrofit) | Gọi `GET /articles` với query params. |
| Local  | `DevToDatabase` (Room v3) | Lưu cache. |
| Local  | `ArticleEntity` | Dữ liệu article gốc — **không** chứa `cache_key`. |
| Local  | `ArticleCacheRef(cache_key, article_id, position)` | Bảng mapping many-to-many giữa filter và article. Lưu cả `position` để giữ thứ tự server trả về. |
| Local  | `RemoteKey(cache_key, next_page, last_updated)` | State paging + TTL cho từng filter. |
| Local  | `ArticleDao` | Truy vấn JOIN giữa `articles` và `article_cache_refs`. |
| Pager  | `ArticleRemoteMediator` | Cầu nối Paging 3 ↔ network ↔ DB. |
| Repo   | `ArticleRepositoryImpl` | Build `Pager` và expose `Flow<PagingData<Article>>`. |

### Sơ đồ quan hệ

```
                   ┌───────────────────────────┐
                   │   articles (PK = id)      │  ← single source of truth
                   │  (không còn cache_key)    │
                   └────────────┬──────────────┘
                                │ 1
                                │
                                │ N
                   ┌────────────┴───────────────┐
                   │ article_cache_refs         │
                   │  PK (cache_key, article_id)│
                   │  position                  │
                   └────────────┬───────────────┘
                                │ N
                                │ 1
                   ┌────────────┴───────────────┐
                   │ remote_keys (PK cache_key) │
                   │  next_page, last_updated   │
                   └────────────────────────────┘
```

---

## 3. Vì sao tách bảng `article_cache_refs` (many-to-many)?

Phương án cũ: `cache_key` nằm trực tiếp trên `ArticleEntity`, PK = `id`.

> Article id = 2 xuất hiện ở cả filter `tag=android` và `tag=mobile`.
> Khi cache filter `mobile`, Room dùng `OnConflictStrategy.REPLACE` →
> row id = 2 bị ghi đè `cache_key = "mobile"` → biến mất khỏi list `android`.

Tách bảng giải quyết bằng cách:
- Article chỉ lưu 1 lần ở `articles`.
- Một article thuộc về N filter thông qua N row trong `article_cache_refs`.
- Refresh một filter chỉ xóa **refs** của filter đó. Article gốc và các refs khác không bị ảnh hưởng.
- Query `getArticlesByCacheKey` là một `INNER JOIN` rồi `ORDER BY r.position ASC`, đảm bảo giữ đúng thứ tự server trả về cho từng filter.

---

## 4. Luồng insert khi REFRESH (cold start hoặc cache hết hạn)

```
UI collects PagingData
        │
        ▼
Pager.flow → RemoteMediator.initialize()
        │
        │  remoteKey == null  ||  (now - lastUpdated > 30 phút)
        ▼
LAUNCH_INITIAL_REFRESH
        │
        ▼
RemoteMediator.load(REFRESH)
        │  page = 1
        ▼
ArticleApi.getArticles(filter, page = 1)   // perPage items
        │
        ▼
withTransaction {
    // 1. Xóa state cũ của filter này (KHÔNG đụng tới article gốc)
    articleDao.deleteByCacheKey(cacheKey)      // chỉ xóa rows trong article_cache_refs
    remoteKeyDao.deleteByKey(cacheKey)

    // 2. Ghi state paging mới
    remoteKeyDao.insert(RemoteKey(cacheKey, nextPage = 2, lastUpdated = now))

    // 3. Upsert article gốc + insert refs với position
    articleDao.insertArticlesForCacheKey(
        articles, cacheKey, startPosition = 0
    )
}
        │
        ▼
PagingSource invalidate → UI nhận data mới
```

Bên trong `insertArticlesForCacheKey` (một `@Transaction`):

```kotlin
suspend fun insertArticlesForCacheKey(
    articles: List<ArticleWithRelations>,
    cacheKey: String,
    startPosition: Int
) {
    insertArticles(articles)                  // REPLACE theo PK = article.id
    insertCacheRefs(
        articles.mapIndexed { index, item ->
            ArticleCacheRef(
                cacheKey  = cacheKey,
                articleId = item.article.id,
                position  = startPosition + index
            )
        }
    )                                          // REPLACE theo PK (cache_key, article_id)
}
```

Khi `now - lastUpdated <= 30 phút`, `initialize()` trả về `SKIP_INITIAL_REFRESH` → UI đọc thẳng từ cache mà không gọi mạng.

---

## 5. Luồng insert khi APPEND (cuộn xuống)

```
Paging cần thêm dữ liệu
        │
        ▼
RemoteMediator.load(APPEND)
        │
        ▼
nextPage = remoteKeyDao.getRemoteKey(cacheKey).nextPage
   nếu null → Success(endOfPaginationReached = true)  // hết data, dừng
        │
        ▼
ArticleApi.getArticles(filter, page = nextPage)
        │
        ▼
withTransaction {
    // KHÔNG xóa refs hay article cũ
    remoteKeyDao.insert(RemoteKey(cacheKey, nextPage = page+1 | null, lastUpdated = now))
    articleDao.insertArticlesForCacheKey(
        articles, cacheKey, startPosition = (page - 1) * perPage
    )
}
```

`startPosition` đảm bảo refs của page 2 có `position` nối tiếp ngay sau page 1 → query `ORDER BY position ASC` giữ đúng thứ tự timeline.

`PREPEND` luôn trả `endOfPaginationReached = true` — feed timeline không kéo ngược lên trên.

---

## 6. Cơ chế REPLACE và tính idempotent

| Bảng | PK | Hành vi khi insert trùng |
|------|----|--------------------------|
| `articles` | `id` | REPLACE → bản article mới nhất luôn được giữ. Một article xuất hiện ở nhiều filter chỉ tồn tại 1 row. |
| `article_cache_refs` | `(cache_key, article_id)` | REPLACE → `position` mới ghi đè position cũ. Cùng 1 article trong cùng 1 filter chỉ có 1 ref. |
| `remote_keys` | `cache_key` | REPLACE → `next_page` và `last_updated` được cập nhật mỗi lần fetch. |

Hệ quả: `RemoteMediator.load()` có thể chạy lại an toàn (retry, race, …) mà không sinh duplicate.

---

## 7. Migration v2 → v3

`MIGRATION_2_3` thực hiện:

1. Tạo `articles_new` không có cột `cache_key`.
2. Copy dữ liệu từ `articles` cũ sang.
3. Tạo `article_cache_refs` với FK CASCADE về `articles_new`.
4. Seed refs từ cặp `(cache_key, id)` cũ với `position = 0` (chấp nhận mất thứ tự lịch sử — lần fetch kế tiếp sẽ overwrite).
5. Drop `articles` cũ, rename `articles_new` → `articles`.
6. Tạo index cho `article_cache_refs.article_id` và `article_cache_refs.cache_key`.

---

## 8. Vòng đời cache & orphan article

- **Per-filter TTL**: `remote_keys.last_updated`, `CACHE_TIMEOUT = 30 phút`. Khi hết hạn, `initialize()` → `LAUNCH_INITIAL_REFRESH` → xóa refs filter đó + fetch lại từ page 1.
- **Article gốc không bao giờ tự xóa**. Nếu một article không còn ref nào (vd. mọi filter chứa nó đều đã bị refresh), row trong `articles` vẫn còn. Đây là chủ ý — article là source of truth, có thể dùng cho detail screen.
- Khi cần dọn orphan, viết job định kỳ:
  ```sql
  DELETE FROM articles WHERE id NOT IN (SELECT article_id FROM article_cache_refs);
  ```

---

## 9. Checklist khi thêm filter mới

1. Bổ sung field vào `ArticleParam` + `toQueryMap()`.
2. Cập nhật `RemoteKey.buildKey(...)` để key phản ánh đúng filter (tránh 2 filter khác nhau dùng chung key).
3. **Không** cần đụng vào schema — mapping table tự xử lý.
4. Viết test: 2 filter cùng chứa 1 `article.id` phải đọc được từ cả hai PagingSource sau khi cache cả hai.
