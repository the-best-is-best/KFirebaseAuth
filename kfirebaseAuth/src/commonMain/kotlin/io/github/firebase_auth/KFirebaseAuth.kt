package io.github.firebase_auth

expect class KFirebaseAuth() {
    suspend fun getClient(): String
    suspend fun currentUser(): Result<KFirebaseUser?>
    suspend fun signInAnonymously(): Result<KFirebaseUser?>
    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?>

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?>

    suspend fun addListenerAuthStateChange(): Result<KFirebaseUser?>
    suspend fun addListenerIdTokenChanged(): Result<KFirebaseUser?>
    suspend fun confirmPasswordReset(
        code: String,
        newPassword: String
    ): Result<Boolean?>

    fun setLanguageCodeLocale(locale: String)
    suspend fun kUpdateProfile(
        displayName: String?,
        photoUrl: String?
    ): Result<Boolean?>

    suspend fun signInWithCredential(
        credential: KAuthCredentials
    ): Result<KFirebaseUser?>

    suspend fun isLinkEmail(email: String): Boolean
    var languageCode: String?

    suspend fun applyActionWithCode(code: String): Result<Boolean?>

    //    suspend fun <T : ActionCodeResult> checkActionWithCode(code: String): Result<T>
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

    suspend fun kSendEmailVerification(): Result<Boolean?>
    suspend fun kUpdateEmail(newEmail: String): Result<Boolean?>

    suspend fun linkProvider(credential: KAuthCredentials): Result<KFirebaseUser?>
    suspend fun kResetPassword(password: String): Result<Boolean?>
    suspend fun kDelete(): Result<Boolean?>
    suspend fun kSignOut(): Result<Boolean?>

}


//sealed class ActionCodeResult {
//    data object SignInWithEmailLink : ActionCodeResult()
//    class PasswordReset internal constructor(val email: String) : ActionCodeResult()
//    class VerifyEmail internal constructor(val email: String) : ActionCodeResult()
//    class RecoverEmail internal constructor(val email: String, val previousEmail: String) :
//        ActionCodeResult()
//
//    class VerifyBeforeChangeEmail internal constructor(
//        val email: String,
//        val previousEmail: String
//    ) : ActionCodeResult()
//
//    class RevertSecondFactorAddition internal constructor(
//        val email: String,
////        val multiFactorInfo: MultiFactorInfo?
//    ) : ActionCodeResult()
//}

//expect class MultiFactorInfo {
//    val displayName: String?
//    val enrollmentTime: Double?
//    val factorId: String
//    val uid: String
//}