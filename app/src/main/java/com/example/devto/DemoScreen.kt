package com.example.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.devto.DemoViewModel
import com.example.domain.model.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    viewModel: DemoViewModel = hiltViewModel()
) {
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val selectedTag by viewModel.selectedTag.collectAsState()

    // Trạng thái refresh gộp cả DB + network
    val isRefreshing = articles.loadState.refresh is LoadState.Loading

    Column(modifier = modifier.fillMaxSize()) {

        // ---------- Filter chips ----------
        FilterBar(
            tags = listOf("android", "ios", "kotlin", "compose"),
            selected = selectedTag,
            onSelect = viewModel::updateFilter
        )

        // ---------- Nội dung + pull to refresh ----------
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { articles.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            when (val refresh = articles.loadState.refresh) {

                // Load lần đầu: full-screen loading
                is LoadState.Loading -> {
                    FullScreenLoading()
                }

                // Lỗi load đầu: full-screen error
                is LoadState.Error -> {
                    FullScreenError(
                        message = refresh.error.message ?: "Không tải được dữ liệu",
                        onRetry = { articles.retry() }
                    )
                }

                is LoadState.NotLoading -> {
                    if (articles.itemCount == 0) {
                        EmptyState()
                    } else {
                        ArticleList(articles = articles)
                    }
                }
            }
        }
    }
}

// ---------------- Danh sách ----------------
@Composable
private fun ArticleList(
    articles: LazyPagingItems<Article>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id },
            contentType = articles.itemContentType { "article" }
        ) { index ->
            val article = articles[index]
            if (article != null) {
                ArticleItem(article)
            } else {
                ArticlePlaceholder()
            }
        }

        // ---------- Footer: load thêm khi scroll cuối (APPEND) ----------
        when (val append = articles.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    AppendErrorRow(
                        message = append.error.message ?: "Lỗi tải thêm",
                        onRetry = { articles.retry() }
                    )
                }
            }
            is LoadState.NotLoading -> {
                if (append.endOfPaginationReached && articles.itemCount > 0) {
                    item {
                        Text(
                            text = "Đã hết bài viết",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

// ---------------- Item ----------------
@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.user.name    ,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.width(8.dp))
                Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${article.readingTimeMinutes} phút đọc",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------- Placeholder (khi bật enablePlaceholders) ----------------
@Composable
fun ArticlePlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            repeat(3) {
                Box(
                    Modifier
                        .fillMaxWidth(if (it == 2) 0.5f else 1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ---------------- Filter bar ----------------
@Composable
private fun FilterBar(
    tags: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            FilterChip(
                selected = tag == selected,
                onClick = { onSelect(tag) },
                label = { Text(tag) }
            )
        }
    }
}

// ---------------- States ----------------
@Composable
private fun FullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Thử lại") }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Chưa có bài viết nào",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppendErrorRow(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRetry() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.width(8.dp))
        Text("Nhấn để thử lại", color = MaterialTheme.colorScheme.primary)
    }
}