package com.example.domain

import com.example.core.result.Result
import com.example.domain.model.Article
import com.example.domain.model.FlareTag
import com.example.domain.model.Organization
import com.example.domain.model.User
import com.example.domain.model.params.ArticleParam
import com.example.domain.repo.ArticleRepository
import com.example.domain.usecase.FetchArticleUseCase
import kotlinx.coroutines.test.runTest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FetchArticleUCTest {

    private lateinit var fetchArticleUC: FetchArticleUseCase
    private lateinit var articleRepository: ArticleRepository


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        articleRepository = mockk<ArticleRepository>()
        fetchArticleUC = FetchArticleUseCase(articleRepository)
    }


    @Test
     fun `fetch get success articles`() = runTest {
        val param = ArticleParam(page = 1, perPage = 30)
        val expected = Result.success(listOf(dummyArticle))
        coEvery { articleRepository.fetchArticles(param) } returns expected
        val result = fetchArticleUC(param)
        assertEquals(expected, result)
    }

    @Test
    fun `fetch articles with perPage over 50 returns failure`() = runTest {
        val param = ArticleParam(page = 1, perPage = 51)

        val result = fetchArticleUC(param)

        assertTrue(result.isFailure())
        assertEquals("perPage max is 50", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { articleRepository.fetchArticles(any()) }
    }

    @Test
    fun `fetch articles with page less than 1 returns failure`() = runTest {
        val param = ArticleParam(page = 0, perPage = 30)

        val result = fetchArticleUC(param)

        assertTrue(result.isFailure())
        coVerify(exactly = 0) { articleRepository.fetchArticles(any()) }
    }

    val dummyArticle = Article(
        typeOf = "article",
        id = 123456,
        title = "Getting Started with Clean Architecture in Android",
        description = "A simple guide to understanding Clean Architecture in Android development.",
        readablePublishDate = "Jun 8",
        slug = "clean-architecture-android",
        path = "/android/clean-architecture-android",
        url = "https://dev.to/example/clean-architecture-android",
        commentsCount = 42,
        publicReactionsCount = 150,
        collectionId = null,
        publishedTimestamp = "2026-06-08T08:00:00Z",
        positiveReactionsCount = 140,
        coverImage = "https://picsum.photos/800/400",
        socialImage = "https://picsum.photos/1200/630",
        canonicalUrl = "https://dev.to/example/clean-architecture-android",
        createdAt = "2026-06-08T07:00:00Z",
        editedAt = null,
        crosspostedAt = null,
        publishedAt = "2026-06-08T08:00:00Z",
        lastCommentAt = "2026-06-08T10:30:00Z",
        readingTimeMinutes = 5,
        tagList = listOf("android", "kotlin", "cleanarchitecture"),
        tags = "android, kotlin, cleanarchitecture",
        user = User(
            name = "John Doe",
            username = "johndoe",
            twitterUsername = "john_doe",
            githubUsername = "johndoe",
            userId = 1001,
            websiteUrl = "https://johndoe.dev",
            profileImage = "https://picsum.photos/100",
            profileImage90 = "https://picsum.photos/90"
        ),
        organization = Organization(
            name = "DEV Community",
            username = "devteam",
            slug = "devteam",
            profileImage = "https://picsum.photos/200",
            profileImage90 = "https://picsum.photos/90"
        ),
        flareTag = FlareTag(
            name = "Featured",
            bgColorHex = "#3B49DF",
            textColorHex = "#FFFFFF"
        )
    )
}