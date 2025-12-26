package com.openclassrooms.hexagonal.games.screen.ad

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
  modifier: Modifier = Modifier,
  viewModel: AddViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  // Photo Picker launcher
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    viewModel.onAction(FormEvent.MediaSelected(uri))
  }

  Scaffold(
    modifier = modifier,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.add_fragment_label))
        },
        navigationIcon = {
          IconButton(onClick = {
            onBackClick()
          }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        }
      )
    }
  ) { contentPadding ->
    val post by viewModel.post.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val selectedMediaUri by viewModel.selectedMediaUri.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val uploadError by viewModel.uploadError.collectAsStateWithLifecycle()

    // Show error message if upload fails
    LaunchedEffect(uploadError) {
      uploadError?.let {
        snackbarHostState.showSnackbar(
          message = it,
          withDismissAction = true
        )
      }
    }

    CreatePost(
      modifier = Modifier.padding(contentPadding),
      error = error,
      title = post.title,
      onTitleChanged = { viewModel.onAction(FormEvent.TitleChanged(it)) },
      description = post.description ?: "",
      onDescriptionChanged = { viewModel.onAction(FormEvent.DescriptionChanged(it)) },
      selectedMediaUri = selectedMediaUri,
      onSelectMediaClicked = {
        photoPickerLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
      },
      isLoading = isLoading,
      onSaveClicked = {
        viewModel.addPost(
          onSuccess = { onSaveClick() },
          onError = { /* Error is handled via uploadError state */ }
        )
      }
    )
  }
}

@Composable
private fun CreatePost(
  modifier: Modifier = Modifier,
  title: String,
  onTitleChanged: (String) -> Unit,
  description: String,
  onDescriptionChanged: (String) -> Unit,
  selectedMediaUri: Uri?,
  onSelectMediaClicked: () -> Unit,
  isLoading: Boolean,
  onSaveClicked: () -> Unit,
  error: FormError?
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .padding(16.dp)
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = title,
        isError = error is FormError.TitleError,
        onValueChange = { onTitleChanged(it) },
        label = { Text(stringResource(id = R.string.hint_title)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true,
        enabled = !isLoading
      )
      if (error is FormError.TitleError) {
        Text(
          text = stringResource(id = error.messageRes),
          color = MaterialTheme.colorScheme.error,
        )
      }
      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = description,
        onValueChange = { onDescriptionChanged(it) },
        label = { Text(stringResource(id = R.string.hint_description)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        enabled = !isLoading
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Media selection button
      OutlinedButton(
        onClick = onSelectMediaClicked,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
      ) {
        Icon(
          imageVector = Icons.Default.AddPhotoAlternate,
          contentDescription = stringResource(id = R.string.select_media),
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = if (selectedMediaUri != null) {
            stringResource(id = R.string.change_media)
          } else {
            stringResource(id = R.string.select_media)
          }
        )
      }

      // Display selected media preview
      if (selectedMediaUri != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .border(
              width = 1.dp,
              color = MaterialTheme.colorScheme.outline,
              shape = RoundedCornerShape(8.dp)
            )
        ) {
          AsyncImage(
            model = selectedMediaUri,
            contentDescription = stringResource(id = R.string.selected_media),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }
      }
    }

    // Save button with loading indicator
    Button(
      enabled = error == null && !isLoading,
      onClick = { onSaveClicked() },
      modifier = Modifier.fillMaxWidth()
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier
            .padding(8.dp)
            .size(24.dp),
          color = MaterialTheme.colorScheme.onPrimary
        )
      } else {
        Text(
          modifier = Modifier.padding(8.dp),
          text = stringResource(id = R.string.action_save)
        )
      }
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreatePostPreview() {
  HexagonalGamesTheme {
    CreatePost(
      title = "test",
      onTitleChanged = { },
      description = "description",
      onDescriptionChanged = { },
      selectedMediaUri = null,
      onSelectMediaClicked = { },
      isLoading = false,
      onSaveClicked = { },
      error = null
    )
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreatePostErrorPreview() {
  HexagonalGamesTheme {
    CreatePost(
      title = "test",
      onTitleChanged = { },
      description = "description",
      onDescriptionChanged = { },
      selectedMediaUri = null,
      onSelectMediaClicked = { },
      isLoading = false,
      onSaveClicked = { },
      error = FormError.TitleError
    )
  }
}