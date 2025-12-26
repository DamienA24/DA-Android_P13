package com.openclassrooms.hexagonal.games.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.data.service.PostFakeApi
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
   * Provides a Singleton instance of PostApi using a PostFakeApi implementation for testing purposes.
   * This means that whenever a dependency on PostApi is requested, the same instance of PostFakeApi will be used
   * throughout the application, ensuring consistent data for testing scenarios.
   *
   * @return A Singleton instance of PostFakeApi.
   */
  @Provides
  @Singleton
  fun providePostApi(): PostApi {
    return PostFakeApi()
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
