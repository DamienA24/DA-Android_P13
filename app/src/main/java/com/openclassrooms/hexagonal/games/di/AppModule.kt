package com.openclassrooms.hexagonal.games.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.data.service.PostFirestoreApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This class acts as a Dagger Hilt module, responsible for providing dependencies to other parts of the application.
 * It's installed in the SingletonComponent, ensuring that dependencies provided by this module are created only once
 * and remain available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  /**
   * Provides a Singleton instance of PostApi using a PostFirestoreApi implementation.
   * This implementation uses Firebase Firestore to fetch and persist posts in real-time.
   *
   * @param firestore The FirebaseFirestore instance to be used by PostFirestoreApi.
   * @return A Singleton instance of PostFirestoreApi.
   */
  @Provides
  @Singleton
  fun providePostApi(firestore: FirebaseFirestore): PostApi {
    return PostFirestoreApi(firestore)
  }

  /**
   * Provides a Singleton instance of FirebaseAuth.
   * This ensures that the same FirebaseAuth instance is used throughout the application
   * for authentication operations.
   *
   * @return A Singleton instance of FirebaseAuth.
   */
  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
  }

  /**
   * Provides a Singleton instance of FirebaseFirestore.
   * This ensures that the same Firestore instance is used throughout the application
   * for database operations.
   *
   * @return A Singleton instance of FirebaseFirestore.
   */
  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
  }

  /**
   * Provides a Singleton instance of FirebaseStorage.
   * This ensures that the same Storage instance is used throughout the application
   * for file storage operations.
   *
   * @return A Singleton instance of FirebaseStorage.
   */
  @Provides
  @Singleton
  fun provideFirebaseStorage(): FirebaseStorage {
    return FirebaseStorage.getInstance()
  }
}
