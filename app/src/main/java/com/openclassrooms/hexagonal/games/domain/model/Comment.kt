package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

/**
 * This class represents a Comment data object. It holds information about a comment on a post,
 * including its ID, content, creation timestamp, and the author (User object).
 * The class implements Serializable to allow for potential serialization needs.
 */
data class Comment(
  /**
   * Unique identifier for the Comment.
   */
  val id: String,

  /**
   * Content of the comment.
   */
  val content: String,

  /**
   * Timestamp representing the creation date and time of the Comment in milliseconds since epoch.
   */
  val timestamp: Long,

  /**
   * User object representing the author of the Comment.
   */
  val author: User?
) : Serializable
