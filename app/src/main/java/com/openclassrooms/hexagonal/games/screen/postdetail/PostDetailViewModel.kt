package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.data.service.CommentApi
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * This ViewModel manages data and interactions related to displaying post details and comments.
 * It utilizes dependency injection to retrieve repository instances for interacting with post and comment data.
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val postRepository: PostRepository,
  private val commentApi: CommentApi,
  private val auth: FirebaseAuth
) : ViewModel() {

  private val postId: String = checkNotNull(savedStateHandle["postId"])

  /**
   * Internal mutable state flow representing the current post being displayed.
   */
  private val _post = MutableStateFlow<Post?>(null)

  /**
   * Public state flow representing the current post being displayed.
   */
  val post: StateFlow<Post?> = _post.asStateFlow()

  /**
   * StateFlow representing the list of comments for the current post.
   * Comments are automatically updated in real-time via Firestore snapshot listeners.
   */
  val comments: StateFlow<List<Comment>> = commentApi
    .getCommentsForPost(postId)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  /**
   * Internal mutable state flow representing the comment text being typed.
   */
  private val _commentText = MutableStateFlow("")

  /**
   * Public state flow representing the comment text being typed.
   */
  val commentText: StateFlow<String> = _commentText.asStateFlow()

  /**
   * Internal mutable state flow representing whether a comment is being sent.
   */
  private val _isSendingComment = MutableStateFlow(false)

  /**
   * Public state flow representing whether a comment is being sent.
   */
  val isSendingComment: StateFlow<Boolean> = _isSendingComment.asStateFlow()

  init {
    loadPost()
  }

  /**
   * Loads the post from the repository based on the postId.
   */
  private fun loadPost() {
    viewModelScope.launch {
      postRepository.posts.collect { posts ->
        _post.value = posts.find { it.id == postId }
      }
    }
  }

  /**
   * Updates the comment text being typed.
   *
   * @param text The new comment text.
   */
  fun onCommentTextChanged(text: String) {
    _commentText.value = text
  }

  /**
   * Adds a new comment to the current post.
   * The comment is associated with the currently authenticated user.
   */
  fun addComment() {
    val currentUser = auth.currentUser ?: return
    val text = _commentText.value.trim()

    if (text.isEmpty()) return

    viewModelScope.launch {
      _isSendingComment.value = true

      try {
        val comment = Comment(
          id = UUID.randomUUID().toString(),
          content = text,
          timestamp = System.currentTimeMillis(),
          author = User(
            id = currentUser.uid,
            firstname = currentUser.displayName?.split(" ")?.firstOrNull() ?: "Anonymous",
            lastname = currentUser.displayName?.split(" ")?.getOrNull(1) ?: ""
          )
        )

        commentApi.addComment(postId, comment)
        _commentText.value = "" // Clear the input field
      } finally {
        _isSendingComment.value = false
      }
    }
  }
}
