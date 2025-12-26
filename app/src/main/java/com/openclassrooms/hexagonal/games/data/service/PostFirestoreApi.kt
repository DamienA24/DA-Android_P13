package com.openclassrooms.hexagonal.games.data.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * This class implements the PostApi interface and provides a Firestore-based data source for Posts.
 * It uses Firebase Firestore to fetch and persist posts in real-time.
 */
class PostFirestoreApi @Inject constructor(
  private val firestore: FirebaseFirestore
) : PostApi {

  companion object {
    private const val POSTS_COLLECTION = "posts"
    private const val FIELD_TIMESTAMP = "timestamp"
    private const val FIELD_ID = "id"
    private const val FIELD_TITLE = "title"
    private const val FIELD_DESCRIPTION = "description"
    private const val FIELD_PHOTO_URL = "photoUrl"
    private const val FIELD_AUTHOR_ID = "authorId"
    private const val FIELD_AUTHOR_FIRSTNAME = "authorFirstname"
    private const val FIELD_AUTHOR_LASTNAME = "authorLastname"
  }

  /**
   * Retrieves a list of Posts ordered by their creation date in descending order.
   * Uses Firestore snapshot listener to provide real-time updates.
   *
   * @return A Flow of list of Posts sorted by creation date (newest first).
   */
  override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> = callbackFlow {
    val listenerRegistration = firestore.collection(POSTS_COLLECTION)
      .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val posts = snapshot.documents.mapNotNull { document ->
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

              Post(
                id = document.getString(FIELD_ID) ?: document.id,
                title = document.getString(FIELD_TITLE) ?: "",
                description = document.getString(FIELD_DESCRIPTION),
                photoUrl = document.getString(FIELD_PHOTO_URL),
                timestamp = document.getLong(FIELD_TIMESTAMP) ?: 0L,
                author = author
              )
            } catch (e: Exception) {
              null
            }
          }
          trySend(posts)
        }
      }

    awaitClose {
      listenerRegistration.remove()
    }
  }

  /**
   * Adds a new Post to Firestore.
   *
   * @param post The Post object to be added.
   */
  override fun addPost(post: Post) {
    val postData = hashMapOf(
      FIELD_ID to post.id,
      FIELD_TITLE to post.title,
      FIELD_DESCRIPTION to post.description,
      FIELD_PHOTO_URL to post.photoUrl,
      FIELD_TIMESTAMP to post.timestamp,
      FIELD_AUTHOR_ID to post.author?.id,
      FIELD_AUTHOR_FIRSTNAME to post.author?.firstname,
      FIELD_AUTHOR_LASTNAME to post.author?.lastname
    )

    firestore.collection(POSTS_COLLECTION)
      .document(post.id)
      .set(postData)
  }
}
