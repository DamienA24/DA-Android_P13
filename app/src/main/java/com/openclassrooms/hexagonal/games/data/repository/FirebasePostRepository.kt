package com.openclassrooms.hexagonal.games.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Post data in Firebase Firestore and Firebase Storage.
 * Handles uploading media to Storage and saving post data to Firestore.
 */
@Singleton
class FirebasePostRepository @Inject constructor(
  private val firestore: FirebaseFirestore,
  private val storage: FirebaseStorage,
  private val auth: FirebaseAuth
) {

  companion object {
    private const val POSTS_COLLECTION = "posts"
    private const val STORAGE_POSTS_PATH = "posts"
  }

  /**
   * Uploads a media file to Firebase Storage and saves the post data to Firestore.
   *
   * @param post The post data to save
   * @param mediaUri The URI of the media file to upload (can be null)
   * @return Result indicating success or failure with error message
   */
  suspend fun uploadPostWithMedia(post: Post, mediaUri: Uri?): Result<String> {
    return try {
      // Get current user
      val currentUser = auth.currentUser
        ?: return Result.failure(Exception("User not authenticated"))

      // Upload media to Storage if provided
      val photoUrl = if (mediaUri != null) {
        uploadMediaToStorage(mediaUri, post.id)
      } else {
        null
      }

      // Create post with author and photo URL
      val postWithData = post.copy(
        photoUrl = photoUrl,
        author = User(
          id = currentUser.uid,
          firstname = currentUser.displayName?.split(" ")?.firstOrNull() ?: "Anonymous",
          lastname = currentUser.displayName?.split(" ")?.getOrNull(1) ?: ""
        ),
        timestamp = System.currentTimeMillis()
      )

      // Save to Firestore
      savePostToFirestore(postWithData)

      Result.success(post.id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Uploads a media file to Firebase Storage.
   *
   * @param uri The URI of the media file
   * @param postId The ID of the post (used for naming the file)
   * @return The download URL of the uploaded file
   */
  private suspend fun uploadMediaToStorage(uri: Uri, postId: String): String {
    val fileExtension = getFileExtension(uri.toString())
    val filename = "$postId.$fileExtension"
    val storageRef = storage.reference
      .child(STORAGE_POSTS_PATH)
      .child(filename)

    // Upload file
    val uploadTask = storageRef.putFile(uri).await()

    // Get download URL
    return storageRef.downloadUrl.await().toString()
  }

  /**
   * Saves post data to Firestore.
   *
   * @param post The post to save
   */
  private suspend fun savePostToFirestore(post: Post) {
    val postData = hashMapOf(
      "id" to post.id,
      "title" to post.title,
      "description" to post.description,
      "photoUrl" to post.photoUrl,
      "timestamp" to post.timestamp,
      "authorId" to post.author?.id,
      "authorFirstname" to post.author?.firstname,
      "authorLastname" to post.author?.lastname
    )

    firestore.collection(POSTS_COLLECTION)
      .document(post.id)
      .set(postData)
      .await()
  }

  /**
   * Extracts file extension from a URI string.
   *
   * @param uri The URI as a string
   * @return The file extension (defaults to "jpg" if not found)
   */
  private fun getFileExtension(uri: String): String {
    return uri.substringAfterLast(".", "jpg")
  }
}
