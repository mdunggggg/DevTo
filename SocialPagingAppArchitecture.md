# Kiến trúc App Social Feed với Paging — Tham khảo Now in Android

Tài liệu thiết kế kiến trúc cho **app social feed (DEV.to-style)** với feed bài viết phân trang, tham khảo và adapt từ pattern của Now in Android (NiA).

---

## 1. Bối cảnh & khác biệt với NiA

| | NiA | App social này |
|---|---|---|
| Dataset | Hữu hạn (vài trăm bài) | **Vô hạn** (feed cuộn liên tục) |
| API | Có change-list (`?after=version`) | Pagination thường (`?page=N&per_page=30`) |
| Sync model | **Push** — WorkManager prefetch hết | **Pull theo trang** — Paging 3 |
| Phát hiện delete | Có tombstone (`isDelete`) | Không có → cần workaround |
| Sort | Server sort theo popularity | Cần `state=fresh` để sort theo `published_at` |

→ **Giữ** triết lý offline-first của NiA. **Thay** `changeListSync` + WorkManager prefetch bằng **Paging 3 + RemoteMediator**.

---

## 2. Triết lý kiến trúc (kế thừa từ NiA)

1. **Offline-first** — UI luôn đọc từ Room; network chỉ là kênh ghi vào DB.
2. **Single Source of Truth** — Room là nguồn duy nhất cho feed; DataStore cho user data.
3. **Unidirectional Data Flow** — state chảy 1 chiều DB → ViewModel → UI.
4. **Tách read path & write path** — UI không bao giờ gọi network trực tiếp.

---

## 3. Sơ đồ tầng

```
┌────────────────────────────────────────────────────────┐
│  UI Layer (feature/*)                                  │
│  Compose Screen ── ViewModel ── collectAsLazyPagingItems
└────────────────────────┬───────────────────────────────┘
                         │ Flow<PagingData<Article>>
                         ▼
┌────────────────────────────────────────────────────────┐
│  Data Layer (core/data)                                │
│  ArticleRepository ── Pager(config, mediator, source)  │
└──────────┬──────────────────────┬──────────────────────┘
           │                      │
           ▼                      ▼
┌──────────────────┐    ┌─────────────────────┐
│  PagingSource    │    │  RemoteMediator     │
│  (Room DAO)      │    │  (fetch → ghi DB)   │
└────────┬─────────┘    └──────────┬──────────┘
         │                         │
         └─────► Room DB ◄─────────┘
                   ▲
                   │ (optional: refresh nền)
         ┌─────────┴──────────┐
         │  WorkManager       │
         │  (refresh page 1)  │
         └────────────────────┘
```

---

## 4. Tổ chức module (multi-module Gradle)

Theo style NiA — Gradle Version Catalog + Convention Plugins:

| Module | Vai trò |
|---|---|
| `app` | Application, navigation root, Hilt setup |
| `feature/feed` | Màn hình feed chính (LazyColumn + paging) |
| `feature/article` | Màn hình chi tiết bài viết |
| `feature/profile` | Profile user/organization |
| `feature/search` | Tìm kiếm bài viết |
| `feature/bookmarks` | Bài đã lưu |
| `core/data` | Repository interface + impl, Pager setup, RemoteMediator |
| `core/database` | Room: ArticleEntity, RemoteKeysEntity, BookmarkEntity, DAO |
| `core/datastore` | Proto DataStore — user settings, theme, login token |
| `core/network` | Retrofit API DTO + serialization |
| `core/model` | Domain model (data class thuần) |
| `core/designsystem` | Theme, Material 3 component dùng chung |
| `core/ui` | Composable component dùng chung (ArticleCard, ...) |
| `core/common` | Util, dispatcher qualifier |
| `sync/work` | WorkManager refresh nền (optional) |
| `core/testing` | Fake repository, test dispatcher |

**Quy tắc dependency** (giống NiA):
- `feature/*` không phụ thuộc `core/network`/`core/database` trực tiếp.
- `core/data` không phụ thuộc Compose/feature.

---

## 5. Stack công nghệ

### Ngôn ngữ & build
- **Kotlin** + KSP
- **Gradle Version Catalog** (`libs.versions.toml`)
- **Convention Plugins** (`build-logic/`)

### UI
- **Jetpack Compose** + Material 3
- **Compose Navigation** (type-safe routes)
- **androidx.paging:paging-compose** — `collectAsLazyPagingItems`
- **coil** — load ảnh cover/avatar

### Async
- **Coroutines** + **Flow** + **StateFlow**
- **Paging 3** (`androidx.paging:paging-runtime`)

### Persistence
- **Room** + **Room Paging** (`androidx.room:room-paging`)
- **Proto DataStore** — settings, auth token
- **Room FTS** (optional) — search offline

### Network
- **Retrofit** + **OkHttp**
- **Kotlinx.Serialization**

### DI
- **Hilt**

### Background
- **WorkManager** (chỉ cho refresh nền + sync user data)

### Testing
- JUnit4, Truth, Turbine, Paging test, Hilt testing, Roborazzi

---

## 6. Tầng Data — Repository

### 6.1. Phân nhóm Repository (theo style NiA)

#### Nhóm 1 — Feed data (server-driven, paged)
- **`ArticleRepository`** — feed bài viết, dùng Pager + RemoteMediator.
- **`UserRepository`** — thông tin user/organization (cache theo TTL).

#### Nhóm 2 — User-specific (local-only)
- **`BookmarkRepository`** — bài đã lưu (Room table riêng).
- **`UserPreferencesRepository`** — theme, font size, default feed tab (Proto DataStore).
- **`AuthRepository`** — JWT token, login state (DataStore mã hóa hoặc EncryptedSharedPreferences).
- **`RecentSearchRepository`** — lịch sử search.

#### Nhóm 3 — Derived (composite)
- **`UserArticleRepository`** — combine `ArticleRepository` × `BookmarkRepository` → article đã trang trí trạng thái bookmark.

```kotlin
fun pagedUserArticles(query: FeedQuery): Flow<PagingData<UserArticle>> =
    articleRepository.pagedArticles(query)
        .combine(bookmarkRepository.bookmarkedIds()) { paging, bookmarks ->
            paging.map { article ->
                UserArticle(article, isBookmarked = article.id in bookmarks)
            }
        }
```

### 6.2. ArticleRepository — interface

```kotlin
interface ArticleRepository {
    fun pagedArticles(query: FeedQuery): Flow<PagingData<Article>>
    suspend fun getArticleDetail(id: Long): Article    // lazy fetch + cache
    suspend fun refresh()                              // force refresh page 1
}

data class FeedQuery(
    val tag: String? = null,
    val username: String? = null,
    val state: FeedState = FeedState.Default,         // Fresh | Rising | Default
)
```

### 6.3. Impl với Pager + RemoteMediator

```kotlin
internal class DefaultArticleRepository @Inject constructor(
    private val db: AppDatabase,
    private val api: DevApi,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : ArticleRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun pagedArticles(query: FeedQuery): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = 30,
            prefetchDistance = 5,
            enablePlaceholders = false,
            initialLoadSize = 30,
        ),
        remoteMediator = ArticleRemoteMediator(db, api, query),
        pagingSourceFactory = { db.articleDao().pagingSource(query.tag, query.username) },
    ).flow.map { paging -> paging.map { it.asDomain() } }
}
```

---

## 7. RemoteMediator — fetch network, ghi Room

```kotlin
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val db: AppDatabase,
    private val api: DevApi,
    private val query: FeedQuery,
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MINUTES.toMillis(30)
        val lastUpdate = db.remoteKeysDao().lastUpdated(query.cacheKey()) ?: 0
        return if (System.currentTimeMillis() - lastUpdate > cacheTimeout) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH       // dùng cache cho tới khi TTL hết
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>,
    ): MediatorResult = try {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                db.remoteKeysDao().nextPageFor(lastItem.id)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        val articles = api.getArticles(
            page = page,
            perPage = state.config.pageSize,
            tag = query.tag,
            username = query.username,
            state = query.state.apiValue,
        )

        db.withTransaction {
            if (loadType == LoadType.REFRESH) {
                db.articleDao().clearByQuery(query.cacheKey())
                db.remoteKeysDao().clearByQuery(query.cacheKey())
            }
            val nextPage = if (articles.isEmpty()) null else page + 1
            db.remoteKeysDao().insertAll(articles.map {
                RemoteKeyEntity(
                    articleId = it.id,
                    queryKey = query.cacheKey(),
                    nextPage = nextPage,
                    updatedAt = System.currentTimeMillis(),
                )
            })
            db.articleDao().upsertAll(articles.map(NetworkArticle::asEntity))
        }

        MediatorResult.Success(endOfPaginationReached = articles.isEmpty())
    } catch (e: IOException) {
        MediatorResult.Error(e)
    } catch (e: HttpException) {
        MediatorResult.Error(e)
    }
}
```

**Điểm cốt lõi**:
- **`RemoteKeyEntity`** lưu `(articleId, queryKey, nextPage, updatedAt)` — cần thiết vì DEV.to API dùng page number, không có cursor.
- **`queryKey`** = hash của `FeedQuery` để cache nhiều tab feed (Home, tag=android, user=ben…) cùng lúc mà không lẫn lộn.
- **TTL 30 phút** trong `initialize()` — tránh refresh mỗi lần mở app.
- **`withTransaction`** — clear + insert atomic, tránh UI flash.

---

## 8. DAO — Room Paging

```kotlin
@Dao
interface ArticleDao {
    @Query("""
        SELECT a.* FROM articles a
        INNER JOIN remote_keys r ON r.articleId = a.id
        WHERE r.queryKey = :queryKey
        ORDER BY a.publishedAt DESC
    """)
    fun pagingSource(queryKey: String): PagingSource<Int, ArticleEntity>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE id IN (SELECT articleId FROM remote_keys WHERE queryKey = :queryKey)")
    suspend fun clearByQuery(queryKey: String)
}
```

**Lưu ý**: PagingSource bind theo `queryKey` → mỗi tab có cache riêng, refresh độc lập.

---

## 9. ViewModel — expose PagingData

```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    repo: UserArticleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query = MutableStateFlow(FeedQuery())

    val articles: Flow<PagingData<UserArticle>> = query
        .flatMapLatest { repo.pagedUserArticles(it) }
        .cachedIn(viewModelScope)            // QUAN TRỌNG: giữ qua config change

    fun setTag(tag: String?) { query.update { it.copy(tag = tag) } }
    fun setState(state: FeedState) { query.update { it.copy(state = state) } }
}
```

`cachedIn(viewModelScope)` giữ PagingData qua xoay màn hình mà không refetch.

---

## 10. UI — Compose

```kotlin
@Composable
fun FeedRoute(viewModel: FeedViewModel = hiltViewModel()) {
    val items = viewModel.articles.collectAsLazyPagingItems()
    FeedScreen(
        items = items,
        onTagClick = viewModel::setTag,
    )
}

@Composable
fun FeedScreen(items: LazyPagingItems<UserArticle>, onTagClick: (String) -> Unit) {
    val isRefreshing = items.loadState.refresh is LoadState.Loading

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { items.refresh() },
    ) {
        LazyColumn {
            items(
                count = items.itemCount,
                key = items.itemKey { it.article.id },
            ) { i ->
                val item = items[i] ?: return@items
                ArticleCard(item, onTagClick = onTagClick)
            }

            when (items.loadState.append) {
                is LoadState.Loading -> item { LoadingRow() }
                is LoadState.Error -> item { ErrorRow(onRetry = items::retry) }
                else -> Unit
            }
        }
    }
}
```

---

## 11. Xử lý "delete" không có tombstone

DEV.to API không trả về bài đã xóa. Chiến lược:

| Cách | Khi nào | Triển khai |
|---|---|---|
| **Lazy 404 check** | Khi user mở detail | `GET /articles/{id}` → 404 → `dao.deleteById(id)` |
| **TTL local** | Bài quá N ngày không refresh | DAO query exclude `updatedAt < now - TTL`; cron xóa |
| **Full reconciliation** | Định kỳ (WorkManager hàng tuần) | Tải toàn bộ id của user → diff với DB → xóa diff |

Với social feed, **lazy 404 + TTL** thường đủ. Reconciliation chỉ cần cho data critical (bookmark).

---

## 12. WorkManager — vai trò trong app này

Khác NiA, **không dùng để prefetch feed** (Paging lo rồi). Chỉ dùng cho:

1. **Refresh page 1 nền** mỗi 30 phút (CoroutineWorker gọi `items.refresh()` qua repository).
2. **Sync user data** lên server: bookmark, reaction, comment (write-behind queue).
3. **Upload draft article** offline.
4. **Pre-warm cache** khi app start (gọi `repository.refresh()` cho tab Home).

```kotlin
@HiltWorker
class RefreshFeedWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: ArticleRepository,
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = runCatching {
        repo.refresh()
        Result.success()
    }.getOrElse { Result.retry() }
}
```

---

## 13. Detail screen — lazy fetch + cache

Pattern khác feed: 1 item, không paging, fetch khi cần.

```kotlin
override suspend fun getArticleDetail(id: Long): Article {
    val cached = db.articleDao().getById(id)
    val isStale = cached == null ||
        System.currentTimeMillis() - cached.updatedAt > TimeUnit.HOURS.toMillis(1)

    if (isStale) {
        try {
            val fresh = api.getArticle(id)
            db.articleDao().upsert(fresh.asEntity())
        } catch (e: HttpException) {
            if (e.code() == 404) db.articleDao().deleteById(id)
            else if (cached == null) throw e             // không có cache → ném
        }
    }
    return db.articleDao().getById(id)!!.asDomain()
}
```

Detail screen vẫn đọc qua `Flow` từ DAO để bookmark/reaction reactive.

---

## 14. User data — write-behind pattern

Bookmark/reaction phải mượt: bấm là phản hồi ngay, đồng bộ server sau.

```kotlin
override suspend fun toggleBookmark(articleId: Long) {
    db.bookmarkDao().toggle(articleId)               // ghi local NGAY
    WorkManager.getInstance(ctx).enqueueUniqueWork(
        "sync-bookmark-$articleId",
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<SyncBookmarkWorker>()
            .setInputData(workDataOf("id" to articleId))
            .setConstraints(NETWORK_CONSTRAINT)
            .build(),
    )
}
```

UI đọc qua `Flow` → bấm là toggle ngay, WorkManager retry với backoff cho tới khi server nhận.

---

## 15. Search

DEV.to API có `tag`/`tags` filter. Nếu cần full-text:
- **Online**: gọi `GET /articles?tag=...` qua Paging riêng.
- **Offline**: Room FTS table trên `articles` đã cache → search trong local trước, có kết quả nào hiện trước; song song fetch online để bổ sung.

---

## 16. Auth

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenProvider.currentToken() }
        val req = chain.request().newBuilder()
            .apply { token?.let { header("api-key", it) } }
            .build()
        return chain.proceed(req)
    }
}
```

`TokenProvider` đọc từ Proto DataStore (mã hóa) — single source of truth cho session.

---

## 17. Test strategy (theo NiA)

| Loại | Cách |
|---|---|
| Repository | Fake DAO + Fake API; assert PagingData qua `AsyncPagingDataDiffer` |
| RemoteMediator | `PagingState` mock; verify `MediatorResult` |
| ViewModel | Inject fake repository; collect StateFlow qua Turbine |
| Compose screen | Stateless `FeedScreen` với fake `LazyPagingItems` |
| Screenshot | Roborazzi cho ArticleCard, EmptyState, ErrorRow |
| E2E | Hilt test runner + MockWebServer |

---

## 18. Cây quyết định khi mở rộng feature

```
Feature mới cần data?
├── Là feed/list dài → Pager + RemoteMediator (giống ArticleRepository)
├── 1 item lazy (detail, profile) → suspend fetch + cache TTL
├── User toggle (bookmark, like, follow) → Write-behind: ghi DB ngay, WorkManager đồng bộ
├── Settings/preference → Proto DataStore
└── Realtime (notification, chat) → WebSocket/FCM, handler ghi DB
```

---

## 19. Checklist khởi tạo project

- [ ] Setup Gradle Version Catalog + Convention Plugins
- [ ] Module skeleton: `app`, `feature/feed`, `core/data`, `core/database`, `core/network`, `core/model`, `core/designsystem`
- [ ] Hilt setup ở `app` + `@HiltAndroidApp`
- [ ] Room database + ArticleEntity + RemoteKeyEntity + DAO
- [ ] Retrofit + Kotlinx.Serialization + DTO
- [ ] `ArticleRepository` interface + `DefaultArticleRepository` impl
- [ ] `ArticleRemoteMediator` với TTL initialize
- [ ] `FeedViewModel` + `Pager` + `cachedIn(viewModelScope)`
- [ ] `FeedScreen` với `collectAsLazyPagingItems` + SwipeRefresh + retry
- [ ] Baseline Profile cho startup
- [ ] Spotless + Detekt + Dependency Guard
- [ ] GitHub Actions CI

---

## 20. Tóm tắt mental model

> **Paging ghi vào Room. UI đọc từ Room. RemoteMediator là dây dẫn giữa network và DB.**
>
> WorkManager chỉ lo việc nền & write-behind (bookmark, draft), KHÔNG đụng feed.
>
> Detail = lazy fetch + cache TTL. Toggle = ghi local ngay + WorkManager đồng bộ.
>
> Mọi screen đều "offline-first": có DB là có UI, không cần chờ network.

App giữ trọn vẹn triết lý của NiA — **read path qua Flow từ DB, write path tập trung** — chỉ thay engine ghi từ `SyncWorker.changeListSync` sang `RemoteMediator` cho phù hợp với feed vô hạn.
