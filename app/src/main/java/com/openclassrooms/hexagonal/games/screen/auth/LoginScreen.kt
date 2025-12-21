package com.openclassrooms.hexagonal.games.screen.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

/**
 * Login screen that displays a sign-in button and launches FirebaseUI Auth flow.
 * Once authenticated, the user is automatically navigated to the home screen.
 *
 * @param onAuthSuccess Callback invoked when authentication is successful
 * @param viewModel The AuthViewModel for managing authentication state
 */
@Composable
fun LoginScreen(
  onAuthSuccess: () -> Unit,
  viewModel: AuthViewModel = hiltViewModel()
) {
  val authState by viewModel.authState.collectAsState()

  val signInLauncher = rememberLauncherForActivityResult(
    contract = FirebaseAuthUIActivityResultContract()
  ) { result ->
    viewModel.handleSignInResult(result)
  }

  LaunchedEffect(authState) {
    if (authState is AuthState.Authenticated) {
      onAuthSuccess()
    }
  }

  if (authState is AuthState.Error) {
    AlertDialog(
      onDismissRequest = { viewModel.clearError() },
      title = { Text("Erreur de connexion") },
      text = { Text((authState as AuthState.Error).message) },
      confirmButton = {
        TextButton(onClick = { viewModel.clearError() }) {
          Text("OK")
        }
      }
    )
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Hexagonal Games",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Bienvenue",
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
          onClick = {
            val signInIntent = viewModel.createSignInIntent()
            signInLauncher.launch(signInIntent)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
        ) {
          Text(
            text = "Se connecter / S'inscrire",
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }
  }
}