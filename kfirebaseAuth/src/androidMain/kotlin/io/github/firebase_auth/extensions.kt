package io.github.firebase_auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.google.firebase.auth.GoogleAuthProvider

fun FirebaseUser.toModel(): KFirebaseUser =
    KFirebaseUser(
        uid = this.uid,
        displayName = this.displayName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        photoURL = this.phoneNumber,
        isAnonymous = this.isAnonymous,
        isEmailVerified = this.isEmailVerified,
        metaData = this.metadata?.toModel()
    )

fun FirebaseUserMetadata.toModel(): KFirebaseUserMetaData =
    KFirebaseUserMetaData(
        creationTime = this.creationTimestamp.toDouble(),
        lastSignInTime = this.lastSignInTimestamp.toDouble()
    )

fun KAuthCredentials.toModel(): AuthCredential {
    return when (this.provider) {
        AuthProvider.GOOGLE -> GoogleAuthProvider.getCredential(
            this.idToken,
            this.accessToken.ifEmpty { null })

        AuthProvider.FACEBOOK -> FacebookAuthProvider.getCredential(this.accessToken)
        // Add more providers if necessary
        else -> throw IllegalArgumentException("Unsupported provider: ${this.provider}")
    }
}
