package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for interacting with Comment data from a data source.
 * It outlines the methods for retrieving and adding Comments, abstracting the underlying
 * implementation details of fetching and persisting data.
 */
interface CommentApi {
  /**
   * Retrieves a list of Comments for a specific post, ordered by their creation date in ascending order.
   *
   * @param postId The ID of the post to retrieve comments for.
   * @return A Flow of list of Comments sorted by creation date (oldest first).
   */
  fun getCommentsForPost(postId: String): Flow<List<Comment>>

  /**
   * Adds a new Comment to the data source.
   *
   * @param postId The ID of the post this comment belongs to.
   * @param comment The Comment object to be added.
   */
  fun addComment(postId: String, comment: Comment)
}
