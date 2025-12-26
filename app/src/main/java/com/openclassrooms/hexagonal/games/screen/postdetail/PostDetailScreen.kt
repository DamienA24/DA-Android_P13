package com.openclassrooms.hexagonal.games.screen.postdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.util.DebugLogger
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
  modifier: Modifier = Modifier,
  viewModel: PostDetailViewModel = hiltViewModel(),
  onBackClick: () -> Unit = {}
) {
  val post by viewModel.post.collectAsStateWithLifecycle()
  val comments by viewModel.comments.collectAsStateWithLifecycle()
  val commentText by viewModel.commentText.collectAsStateWithLifecycle()
  val isSendingComment by viewModel.isSendingComment.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.post_detail_title))
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_back)
            )
          }
        }
      )
    },
    bottomBar = {
      if (post != null) {
        CommentInputBar(
          commentText = commentText,
          onCommentTextChanged = viewModel::onCommentTextChanged,
          onSendClick = viewModel::addComment,
          isSending = isSendingComment
        )
      }
    }
  ) { contentPadding ->
    if (post == null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        CircularProgressIndicator()
      }
    } else {
      PostDetailContent(
        modifier = Modifier.padding(contentPadding),
        post = post!!,
        comments = comments
      )
    }
  }
}

@Composable
private fun PostDetailContent(
  modifier: Modifier = Modifier,
  post: Post,
  comments: List<Comment>
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Post details
    item {
      PostDetailCard(post = post)
    }

    // Comments section header
    item {
      Text(
        text = stringResource(id = R.string.comments_section_title, comments.size),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    // Comments list
    items(comments) { comment ->
      CommentItem(comment = comment)
    }

    // Empty state for comments
    if (comments.isEmpty()) {
      item {
        Text(
          text = stringResource(id = R.string.no_comments_yet),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 16.dp)
        )
      }
    }
  }
}

@Composable
private fun PostDetailCard(post: Post) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      // Author
      Text(
        text = stringResource(
          id = R.string.by,
          post.author?.firstname ?: "",
          post.author?.lastname ?: ""
        ),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // Title
      Text(
        text = post.title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(top = 8.dp)
      )

      // Image
      if (post.photoUrl.isNullOrEmpty() == false) {
        AsyncImage(
          modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .aspectRatio(ratio = 16 / 9f),
          model = post.photoUrl,
          imageLoader = LocalContext.current.imageLoader.newBuilder()
            .logger(DebugLogger())
            .build(),
          placeholder = ColorPainter(Color.DarkGray),
          contentDescription = "Post image",
          contentScale = ContentScale.Crop,
        )
      }

      // Description
      if (post.description.isNullOrEmpty() == false) {
        Text(
          text = post.description,
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(top = 16.dp)
        )
      }

      // Timestamp
      Text(
        text = formatDate(post.timestamp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
private fun CommentItem(comment: Comment) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier.padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(
            id = R.string.by,
            comment.author?.firstname ?: "",
            comment.author?.lastname ?: ""
          ),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = formatDate(comment.timestamp),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Text(
        text = comment.content,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
private fun CommentInputBar(
  commentText: String,
  onCommentTextChanged: (String) -> Unit,
  onSendClick: () -> Unit,
  isSending: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .imePadding(),
    tonalElevation = 3.dp,
    shadowElevation = 3.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = commentText,
        onValueChange = onCommentTextChanged,
        modifier = Modifier.weight(1f),
        placeholder = {
          Text(stringResource(id = R.string.hint_add_comment))
        },
        enabled = !isSending,
        maxLines = 4
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = onSendClick,
        enabled = !isSending && commentText.isNotBlank()
      ) {
        if (isSending) {
          CircularProgressIndicator()
        } else {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(id = R.string.contentDescription_send_comment)
          )
        }
      }
    }
  }
}

private fun formatDate(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
  return sdf.format(Date(timestamp))
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PostDetailContentPreview() {
  HexagonalGamesTheme {
    PostDetailContent(
      post = Post(
        id = "1",
        title = "Amazing Game Release!",
        description = "Check out this new game that just came out. It's absolutely amazing with stunning graphics and gameplay!",
        photoUrl = "https://picsum.photos/id/85/1080/",
        timestamp = System.currentTimeMillis(),
        author = User(
          id = "1",
          firstname = "John",
          lastname = "Doe"
        )
      ),
      comments = listOf(
        Comment(
          id = "1",
          content = "This looks great! Can't wait to try it out.",
          timestamp = System.currentTimeMillis() - 3600000,
          author = User(
            id = "2",
            firstname = "Jane",
            lastname = "Smith"
          )
        ),
        Comment(
          id = "2",
          content = "I've been waiting for this for so long!",
          timestamp = System.currentTimeMillis() - 1800000,
          author = User(
            id = "3",
            firstname = "Bob",
            lastname = "Johnson"
          )
        )
      )
    )
  }
}
