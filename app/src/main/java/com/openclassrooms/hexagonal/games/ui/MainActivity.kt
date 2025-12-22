package com.openclassrooms.hexagonal.games.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.accountmanagement.AccountManagementScreen
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.auth.AuthViewModel
import com.openclassrooms.hexagonal.games.screen.auth.LoginScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    setContent {
      val navController = rememberNavController()
      
      HexagonalGamesTheme {
        HexagonalGamesNavHost(navHostController = navController)
      }
    }
  }

  // [START ask_post_notifications]
  // Declare the launcher at the top of your Activity/Fragment:
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { isGranted: Boolean ->
    if (isGranted) {
      // FCM SDK (and your app) can post notifications.
    } else {
      // TODO: Inform user that that your app will not show notifications.
    }
  }

  private fun askNotificationPermission() {
    // This is only necessary for API level >= 33 (TIRAMISU)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
      ) {
        // FCM SDK (and your app) can post notifications.
      } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
        // TODO: display an educational UI explaining to the user the features that will be enabled
        //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
        //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
        //       If the user selects "No thanks," allow the user to continue without notifications.
      } else {
        // Directly ask for the permission
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }
  
}

@Composable
fun HexagonalGamesNavHost(navHostController: NavHostController) {
  val context = LocalContext.current
  NavHost(
    navController = navHostController,
    startDestination = Screen.Login.route
  ) {
    composable(route = Screen.Login.route) {
      LoginScreen(
        onAuthSuccess = {
          navHostController.navigate(Screen.Homefeed.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
          }
        }
      )
    }
    composable(route = Screen.Homefeed.route) {
      HomefeedScreen(
        onPostClick = {
          //TODO
        },
        onSettingsClick = {
          navHostController.navigate(Screen.Settings.route)
        },
        onFABClick = {
          navHostController.navigate(Screen.AddPost.route)
        }
      )
    }
    composable(route = Screen.AddPost.route) {
      AddScreen(
        onBackClick = { navHostController.navigateUp() },
        onSaveClick = { navHostController.navigateUp() }
      )
    }
    composable(route = Screen.Settings.route) {
      val authViewModel: AuthViewModel = hiltViewModel()
      SettingsScreen(
        onBackClick = { navHostController.navigateUp() },
        onSignOutClick = {
          authViewModel.signOut(context)
          // Navigate to Login and clear the back stack
          navHostController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
          }
        },
        onAccountManagementClick = {
          navHostController.navigate(Screen.AccountManagement.route)
        }
      )
    }
    composable(route = Screen.AccountManagement.route) {
      val authViewModel: AuthViewModel = hiltViewModel()
      AccountManagementScreen(
        authViewModel = authViewModel,
        onBackClick = { navHostController.navigateUp() },
        onSignOutSuccess = {
          // Navigate to Login and clear the back stack
          navHostController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
          }
        },
        onDeleteAccountSuccess = {
          // Navigate to Login and clear the back stack
          navHostController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
          }
        }
      )
    }
  }
}
