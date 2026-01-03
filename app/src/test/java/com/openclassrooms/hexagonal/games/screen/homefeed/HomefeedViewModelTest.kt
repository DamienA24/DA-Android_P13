package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomefeedViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomefeedViewModel
    private lateinit var postRepository: PostRepository

    private val testPosts = listOf(
        Post(
            id = "1",
            title = "First Post",
            description = "Description 1",
            photoUrl = null,
            timestamp = System.currentTimeMillis(),
            author = User("user1", "John", "Doe")
        ),
        Post(
            id = "2",
            title = "Second Post",
            description = "Description 2",
            photoUrl = "https://example.com/image.jpg",
            timestamp = System.currentTimeMillis() - 1000,
            author = User("user2", "Jane", "Smith")
        ),
        Post(
            id = "3",
            title = "Third Post",
            description = null,
            photoUrl = null,
            timestamp = System.currentTimeMillis() - 2000,
            author = User("user3", "Bob", "Johnson")
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        postRepository = mockk(relaxed = true)
        every { postRepository.posts } returns flowOf(testPosts)

        viewModel = HomefeedViewModel(postRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should load posts from repository on initialization`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.posts.test {
            val posts = awaitItem()
            assertEquals(3, posts.size)
            assertEquals("First Post", posts[0].title)
            assertEquals("Second Post", posts[1].title)
            assertEquals("Third Post", posts[2].title)
        }
    }

    @Test
    fun `should emit empty list when repository returns empty list`() = runTest {
        every { postRepository.posts } returns flowOf(emptyList())
        val emptyViewModel = HomefeedViewModel(postRepository)

        testDispatcher.scheduler.advanceUntilIdle()

        emptyViewModel.posts.test {
            val posts = awaitItem()
            assertTrue(posts.isEmpty())
        }
    }

    @Test
    fun `should update posts when repository emits new posts`() = runTest {
        val newPost = Post(
            id = "4",
            title = "New Post",
            description = "New Description",
            photoUrl = null,
            timestamp = System.currentTimeMillis(),
            author = User("user4", "Alice", "Williams")
        )

        val updatedPosts = testPosts + newPost
        every { postRepository.posts } returns flowOf(updatedPosts)

        val newViewModel = HomefeedViewModel(postRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        newViewModel.posts.test {
            val posts = awaitItem()
            assertEquals(4, posts.size)
            assertEquals("New Post", posts[3].title)
        }
    }

    @Test
    fun `posts should maintain order from repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.posts.test {
            val posts = awaitItem()
            assertEquals("1", posts[0].id)
            assertEquals("2", posts[1].id)
            assertEquals("3", posts[2].id)
        }
    }
}
