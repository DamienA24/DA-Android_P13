package com.openclassrooms.hexagonal.games.data.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * This class implements the CommentApi interface and provides a Firestore-based data source for Comments.
 * It uses Firebase Firestore to fetch and persist comments in real-time.
 * Comments are stored as subcollections under each post document.
 */
class CommentFirestoreApi @Inject constructor(
  private val firestore: FirebaseFirestore
) : CommentApi {

  companion object {
    private const val POSTS_COLLECTION = "posts"
    private const val COMMENTS_COLLECTION = "comments"
    private const val FIELD_TIMESTAMP = "timestamp"
    private const val FIELD_ID = "id"
    private const val FIELD_CONTENT = "content"
    private const val FIELD_AUTHOR_ID = "authorId"
    private const val FIELD_AUTHOR_FIRSTNAME = "authorFirstname"
    private const val FIELD_AUTHOR_LASTNAME = "authorLastname"
  }

  /**
   * Retrieves a list of Comments for a specific post, ordered by their creation date in ascending order.
   * Uses Firestore snapshot listener to provide real-time updates.
   *
   * @param postId The ID of the post to retrieve comments for.
   * @return A Flow of list of Comments sorted by creation date (oldest first).
   */
  override fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
    val listenerRegistration = firestore
      .collection(POSTS_COLLECTION)
      .document(postId)
      .collection(COMMENTS_COLLECTION)
      .orderBy(FIELD_TIMESTAMP, Query.Direction.ASCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val comments = snapshot.documents.mapNotNull { document ->
            try {
              val authorId = document.getString(FIELD_AUTHOR_ID)
              val authorFirstname = document.getString(FIELD_AUTHOR_FIRSTNAME)
              val authorLastname = document.getString(FIELD_AUTHOR_LASTNAME)

              val author = if (authorId != null) {
                User(
                  id = authorId,
                  firstname = authorFirstname ?: "",
                  lastname = authorLastname ?: ""
                )
              } else {
                null
              }

              Comment(
                id = document.getString(FIELD_ID) ?: document.id,
                content = document.getString(FIELD_CONTENT) ?: "",
                timestamp = document.getLong(FIELD_TIMESTAMP) ?: 0L,
                author = author
              )
            } catch (e: Exception) {
              null
            }
          }
          trySend(comments)
        }
      }

    awaitClose {
      listenerRegistration.remove()
    }
  }

  /**
   * Adds a new Comment to Firestore.
   *
   * @param postId The ID of the post this comment belongs to.
   * @param comment The Comment object to be added.
   */
  override fun addComment(postId: String, comment: Comment) {
    val commentData = hashMapOf(
      FIELD_ID to comment.id,
      FIELD_CONTENT to comment.content,
      FIELD_TIMESTAMP to comment.timestamp,
      FIELD_AUTHOR_ID to comment.author?.id,
      FIELD_AUTHOR_FIRSTNAME to comment.author?.firstname,
      FIELD_AUTHOR_LASTNAME to comment.author?.lastname
    )

    firestore
      .collection(POSTS_COLLECTION)
      .document(postId)
      .collection(COMMENTS_COLLECTION)
      .document(comment.id)
      .set(commentData)
  }
}
