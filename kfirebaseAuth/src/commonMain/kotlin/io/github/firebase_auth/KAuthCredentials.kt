package io.github.firebase_auth

data class KAuthCredentials(
    val idToken: String,
    val accessToken: String,
    val provider: AuthProvider,
)