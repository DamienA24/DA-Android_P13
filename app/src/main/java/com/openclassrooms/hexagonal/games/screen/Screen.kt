package com.openclassrooms.hexagonal.games.screen

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
  val route: String,
  val navArguments: List<NamedNavArgument> = emptyList()
) {
  data object Login : Screen("login")

  data object Homefeed : Screen("homefeed")

  data object AddPost : Screen("addPost")

  data object Settings : Screen("settings")

  data object AccountManagement : Screen("accountManagement")

  data object PostDetail : Screen(
    route = "postDetail/{postId}",
    navArguments = listOf(
      navArgument("postId") {
        type = NavType.StringType
      }
    )
  ) {
    fun createRoute(postId: String) = "postDetail/$postId"
  }
}