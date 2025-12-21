package com.openclassrooms.hexagonal.games.screen.accountmanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.screen.auth.AuthViewModel
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

/**
 * Screen for managing user account actions such as sign out and account deletion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
  modifier: Modifier = Modifier,
  authViewModel: AuthViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSignOutSuccess: () -> Unit,
  onDeleteAccountSuccess: () -> Unit
) {
  val context = LocalContext.current
  val currentUser by authViewModel.currentUser.collectAsState()

  var showSignOutDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showErrorDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text("Gestion du compte")
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        }
      )
    }
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Compte utilisateur",
            style = MaterialTheme.typography.titleMedium
          )
          currentUser?.let { user ->
            Text(
              text = "Email: ${user.email ?: "Non disponible"}",
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = "Nom: ${user.displayName ?: "Non renseigné"}",
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedButton(
        onClick = { showSignOutDialog = true },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Se déconnecter")
      }

      Button(
        onClick = { showDeleteDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error
        )
      ) {
        Text("Supprimer le compte")
      }

      if (showSignOutDialog) {
        AlertDialog(
          onDismissRequest = { showSignOutDialog = false },
          title = { Text("Confirmation") },
          text = { Text("Êtes-vous sûr de vouloir vous déconnecter ?") },
          confirmButton = {
            TextButton(
              onClick = {
                showSignOutDialog = false
                authViewModel.signOut(context) {
                  onSignOutSuccess()
                }
              }
            ) {
              Text("Oui")
            }
          },
          dismissButton = {
            TextButton(onClick = { showSignOutDialog = false }) {
              Text("Annuler")
            }
          }
        )
      }

      if (showDeleteDialog) {
        AlertDialog(
          onDismissRequest = { showDeleteDialog = false },
          title = {
            Text(
              text = "Suppression du compte",
              color = MaterialTheme.colorScheme.error
            )
          },
          text = {
            Text(
              text = "Êtes-vous sûr de vouloir supprimer définitivement votre compte ? " +
                  "Cette action est irréversible et toutes vos données seront perdues.",
              textAlign = TextAlign.Justify
            )
          },
          confirmButton = {
            TextButton(
              onClick = {
                showDeleteDialog = false
                authViewModel.deleteAccount(
                  context = context,
                  onSuccess = {
                    onDeleteAccountSuccess()
                  },
                  onError = { error ->
                    errorMessage = error
                    showErrorDialog = true
                  }
                )
              },
              colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              )
            ) {
              Text("Supprimer")
            }
          },
          dismissButton = {
            TextButton(onClick = { showDeleteDialog = false }) {
              Text("Annuler")
            }
          }
        )
      }

      if (showErrorDialog) {
        AlertDialog(
          onDismissRequest = { showErrorDialog = false },
          title = { Text("Erreur") },
          text = { Text(errorMessage) },
          confirmButton = {
            TextButton(onClick = { showErrorDialog = false }) {
              Text("OK")
            }
          }
        )
      }
    }
  }
}

@PreviewLightDark
@Composable
private fun AccountManagementScreenPreview() {
  HexagonalGamesTheme {
    AccountManagementScreen(
      onBackClick = {},
      onSignOutSuccess = {},
      onDeleteAccountSuccess = {}
    )
  }
}
