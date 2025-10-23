<h1 align="center">KFirebaseAuth</h1><br>
<div align="center">
<a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
<a href="https://android-arsenal.com/api?level=23" rel="nofollow">
    <img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat" style="max-width: 100%;">
</a>
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
  <img src="https://img.shields.io/badge/iOS-15%2B-blue.svg?logo=apple" alt="iOS 15+ Badge" />

<a href="https://github.com/the-best-is-best/"><img alt="Profile" src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white" height="20"/></a>
</div>

<br>

### KFirebaseAuth is a Kotlin Multiplatform library that provides a unified and idiomatic API for integrating Firebase Authentication across Android and iOS. It enables you to handle sign-in, sign-out, user session management, and email verification using shared Kotlin code—no need to write platform-specific logic

### Ideal for KMM projects aiming to centralize authentication logic and streamline cross-platform development

---

[![Maven Central](https://img.shields.io/maven-central/v/io.github.the-best-is-best/kfirebase-auth)](https://central.sonatype.com/artifact/io.github.the-best-is-best/kfirebase-auth)

KFirebaseAuth is available on `mavenCentral()`.
---

## Version Compatibility

| KFirebaseAuth Version | Firebase iOS SDK | Minimum iOS Version |
|-----------------------|------------------|---------------------|
| 1.1.0                 | Firebase v11.x   | iOS 13+             |
| 2.0.0                 | Firebase v12.x   | iOS 15+             |

## Installation

```kotlin
implementation("io.github.the-best-is-best:kfirebase-auth:2.1.0")
```

### androidMain

```kotlin
KAndroidFirebaseCore.initialize(this)

AndroidKFirebaseAuth.init(
    this,
    "YourClientId"
)
```

### iOS (Using Swift Package Manager - SPM)

Make sure to add Firebase as a dependency using Swift Package Manager (SPM).

1. Open your Xcode project.
2. Go to **File > Add Packages...**
3. Use this URL: `https://github.com/the-best-is-best/KFirebaseAuthInterop`

---

## Usage

```kotlin
val auth = KFirebaseAuth()
val result = auth.signInWithEmailAndPassword("test@example.com", "securePassword")
result.onSuccess { user ->
    println("Signed in as: ${user?.email}")
}.onFailure { error ->
    println("Sign-in failed: ${error.message}")
}

// Send email verification
auth.kSendEmailVerification()

// Update profile
auth.kUpdateProfile(displayName = "Jane Doe", photoUrl = null)

//etc
// More APIs like signInAnonymously, verifyOtp, etc. are also available.

```

---

## API

```kotlin
expect class KFirebaseAuth {
    suspend fun getClient(): String
    suspend fun currentUser(): Result<KFirebaseUser?>

    suspend fun signInAnonymously(): Result<KFirebaseUser?>
    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<KFirebaseUser?>

    suspend fun signInWithCredential(credential: KAuthCredentials): Result<KFirebaseUser?>
    suspend fun linkProvider(credential: KAuthCredentials): Result<KFirebaseUser?>
    suspend fun isLinkEmail(email: String): Boolean

    suspend fun confirmPasswordReset(code: String, newPassword: String): Result<Boolean?>
    suspend fun applyActionWithCode(code: String): Result<Boolean?>
    suspend fun kResetPassword(password: String): Result<Boolean?>
    suspend fun kUpdateEmail(newEmail: String): Result<Boolean?>
    suspend fun kUpdateProfile(displayName: String?, photoUrl: String?): Result<Boolean?>

    suspend fun kSendEmailVerification(): Result<Boolean?>
    suspend fun kDelete(): Result<Boolean?>
    suspend fun kSignOut(): Result<Boolean?>

    suspend fun addListenerAuthStateChange(): Result<KFirebaseUser?>
    suspend fun addListenerIdTokenChanged(): Result<KFirebaseUser?>
    suspend fun removeListenerAuthStateChange(): Result<Boolean?>
    suspend fun removeListenerIdTokenChanged(): Result<Boolean?>

    suspend fun sendOtp(
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onCodeSentFailed: (Throwable) -> Unit
    )

    suspend fun verifyOtp(
        verificationId: String,
        otpCode: String,
        onVerificationCompleted: (user: KFirebaseUser) -> Unit,
        onVerificationFailed: (Throwable) -> Unit
    )

    fun setLanguageCodeLocale(locale: String)
    var languageCode: String?
}
```

---

## License

This project is licensed under the [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0).

---
