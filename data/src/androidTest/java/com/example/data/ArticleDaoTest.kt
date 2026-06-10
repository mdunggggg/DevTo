package com.example.data

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.dao.ArticleDao
import com.example.data.local.database.DevToDatabase
import com.example.data.local.model.ArticleEntity
import com.example.data.local.model.ArticleWithRelations
import com.example.data.local.model.FlareTagEntity
import com.example.data.local.model.OrganizationEntity
import com.example.data.local.model.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class ArticleDaoTest {
    private lateinit var db: DevToDatabase
    private lateinit var articleDao: ArticleDao

    @Before
    fun initDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DevToDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        articleDao = db.articleDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertArticle_returnsArticleWithAllRelations() = runBlocking {
        val expected = articleWithRelations()

        articleDao.insertArticle(expected)

        assertEquals(listOf(expected), articleDao.getAllArticle().first())
    }

    @Test
    fun insertArticle_withoutOptionalRelations_returnsNullRelations() = runBlocking {
        val expected = articleWithRelations().copy(
            organization = null,
            flareTag = null
        )

        articleDao.insertArticle(expected)

        val actual = articleDao.getAllArticle().first().single()
        assertEquals(expected.article, actual.article)
        assertEquals(expected.user, actual.user)
        assertNull(actual.organization)
        assertNull(actual.flareTag)
    }

    @Test
    fun insertUser_forSameArticle_replacesExistingOneToOneRelation() = runBlocking {
        val article = articleWithRelations()
        val replacement = article.user.copy(id = "user-2", name = "Updated user")
        articleDao.insertArticle(article)

        articleDao.insertUser(replacement)

        assertEquals(replacement, articleDao.getAllArticle().first().single().user)
    }

    @Test
    fun insertRelation_withoutArticle_throwsForeignKeyConstraint() {
        val orphanUser = user(articleId = 404)

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { articleDao.insertUser(orphanUser) }
        }
    }

    @Test
    fun deleteArticle_cascadesToRelations() : Unit = runBlocking {
        val article = articleWithRelations()
        articleDao.insertArticle(article)

        articleDao.deleteArticle(article.article.id)

        assertEquals(emptyList<ArticleWithRelations>(), articleDao.getAllArticle().first())
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { articleDao.insertUser(article.user.copy(id = "orphan-user")) }
        }
    }

    private fun articleWithRelations(
        articleId: Int = 1
    ): ArticleWithRelations {
        return ArticleWithRelations(
            article = article(articleId),
            user = user(articleId),
            organization = organization(articleId),
            flareTag = flareTag(articleId)
        )
    }

    private fun article(id: Int): ArticleEntity {
        return ArticleEntity(
            id = id,
            typeOf = "article",
            title = "Room testing",
            description = "Testing ArticleDao",
            coverImage = null,
            readablePublishDate = "Jun 10",
            socialImage = "https://example.com/social.png",
            tagList = listOf("android", "room"),
            tags = "android, room",
            slug = "room-testing",
            path = "/example/room-testing",
            url = "https://example.com/room-testing",
            canonicalUrl = "https://example.com/room-testing",
            commentsCount = 2,
            positiveReactionsCount = 10,
            publicReactionsCount = 12,
            collectionId = null,
            createdAt = "2026-06-10T00:00:00Z",
            editedAt = null,
            crosspostedAt = null,
            publishedAt = "2026-06-10T00:00:00Z",
            lastCommentAt = "2026-06-10T01:00:00Z",
            publishedTimestamp = "2026-06-10T00:00:00Z",
            readingTimeMinutes = 3
        )
    }

    private fun user(articleId: Int): UserEntity {
        return UserEntity(
            id = "user-1",
            articleId = articleId,
            name = "Test user",
            username = "test-user",
            twitterUsername = null,
            githubUsername = "test-user",
            remoteUserId = 100,
            websiteUrl = null,
            profileImage = "https://example.com/user.png",
            profileImage90 = "https://example.com/user-90.png"
        )
    }

    private fun organization(articleId: Int): OrganizationEntity {
        return OrganizationEntity(
            id = "organization-1",
            articleId = articleId,
            name = "Test organization",
            username = "test-organization",
            slug = "test-organization",
            profileImage = "https://example.com/organization.png",
            profileImage90 = "https://example.com/organization-90.png"
        )
    }

    private fun flareTag(articleId: Int): FlareTagEntity {
        return FlareTagEntity(
            id = "flare-tag-1",
            articleId = articleId,
            name = "discuss",
            bgColorHex = "#000000",
            textColorHex = "#ffffff"
        )
    }
}
