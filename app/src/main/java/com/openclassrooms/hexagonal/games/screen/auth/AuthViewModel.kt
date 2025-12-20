package com.openclassrooms.hexagonal.games.screen.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the different authentication states.
 */
sealed class AuthState {
  data object Initial : AuthState()
  data object Loading : AuthState()
  data class Authenticated(val user: FirebaseUser) : AuthState()
  data object Unauthenticated : AuthState()
  data class Error(val message: String) : AuthState()
}

/**
 * ViewModel responsible for managing authentication state using Firebase Auth.
 * This ViewModel monitors the current user's authentication status and exposes it
 * as a StateFlow that UI components can observe.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
  private val firebaseAuth: FirebaseAuth
) : ViewModel() {

  private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  /**
   * Returns whether a user is currently authenticated.
   */
  val isAuthenticated: Boolean
    get() = firebaseAuth.currentUser != null

  init {
    checkAuthState()
    observeAuthChanges()
  }

  /**
   * Checks the current authentication state.
   */
  private fun checkAuthState() {
    val user = firebaseAuth.currentUser
    _authState.value = if (user != null) {
      AuthState.Authenticated(user)
    } else {
      AuthState.Unauthenticated
    }
    _currentUser.value = user
  }

  /**
   * Observes Firebase Auth state changes using a Flow.
   * This approach automatically cleans up the listener when the Flow is cancelled.
   */
  private fun observeAuthChanges() {
    viewModelScope.launch {
      observeAuthState().collect { user ->
        _currentUser.value = user
        _authState.value = if (user != null) {
          AuthState.Authenticated(user)
        } else {
          AuthState.Unauthenticated
        }
      }
    }
  }

  /**
   * Creates a Flow that emits the current user whenever auth state changes.
   * The listener is automatically removed when the Flow is cancelled.
   */
  private fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
      trySend(auth.currentUser)
    }

    firebaseAuth.addAuthStateListener(authStateListener)

    awaitClose {
      firebaseAuth.removeAuthStateListener(authStateListener)
    }
  }

  /**
   * Creates the sign-in intent for FirebaseUI Auth.
   *
   * @return Intent configured for email/password authentication
   */
  fun createSignInIntent(): Intent {
    val providers = arrayListOf(
      AuthUI.IdpConfig.EmailBuilder()
        .setRequireName(true)
        .setAllowNewAccounts(true)
        .build()
    )

    return AuthUI.getInstance()
      .createSignInIntentBuilder()
      .setAvailableProviders(providers)
      .build()
  }

  /**
   * Handles the result from FirebaseUI Auth flow.
   *
   * @param result The authentication result
   */
  fun handleSignInResult(result: FirebaseAuthUIAuthenticationResult) {
    when (result.resultCode) {
      android.app.Activity.RESULT_OK -> {
        // Authentication successful - state will be updated by the AuthStateListener
        val user = firebaseAuth.currentUser
        if (user != null) {
          _authState.value = AuthState.Authenticated(user)
        }
      }
      else -> {
        val response = result.idpResponse
        if (response == null) {
          // User cancelled the sign-in flow
          _authState.value = AuthState.Unauthenticated
        } else {
          // Authentication error
          val errorMessage = response.error?.localizedMessage
            ?: "Une erreur est survenue lors de la connexion"
          Log.d("AuthViewModel", "Authentication error: $errorMessage")
          _authState.value = AuthState.Error(errorMessage)
        }
      }
    }
  }

  /**
   * Signs out the current user.
   */
  fun signOut(context: Context, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      AuthUI.getInstance()
        .signOut(context)
        .addOnCompleteListener {
          _authState.value = AuthState.Unauthenticated
          _currentUser.value = null
          onComplete()
        }
    }
  }

  /**
   * Clears the error state.
   */
  fun clearError() {
    if (_authState.value is AuthState.Error) {
      _authState.value = AuthState.Unauthenticated
    }
  }
}