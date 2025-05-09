package io.github.firebase_auth

import android.net.Uri
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

actual class KFirebaseAuth {
    internal var android = FirebaseAuth.getInstance()

    companion object {
        internal var currentUser: FirebaseUser? = null
    }

    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var idTokenListener: FirebaseAuth.IdTokenListener? = null

    init {
        currentUser = android.currentUser
    }

    actual suspend fun currentUser(): Result<KFirebaseUser?> = suspendCancellableCoroutine { cont ->
        if (currentUser == null) {
            currentUser = android.currentUser
        }
        cont.resume(Result.success(currentUser?.toModel()))
    }

    actual suspend fun signInAnonymously(): Result<KFirebaseUser?> =
        suspendCancellableCoroutine { cont ->
            android.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentUser = task.result.user
                        val userData = task.result?.user?.toModel()
                        cont.resume(Result.success(userData))
                    } else {
                        val exception = task.exception ?: Exception("Unknown error occurred.")
                        cont.resume(Result.failure(exception))
                    }
                }
        }

    actual suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?> = suspendCancellableCoroutine { cont ->
        android.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = task.result.user
                    val userData = task.result?.user?.toModel()
                    cont.resume(Result.success(userData))
                } else {
                    val exception = task.exception ?: Exception("Unknown error occurred.")
                    cont.resumeWith(Result.failure(exception))
                }
            }
            .addOnFailureListener {
                cont.resumeWith(Result.failure(it))
            }
    }

    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?> = suspendCancellableCoroutine { cont ->
        android.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = task.result.user
                    val userData = task.result?.user?.toModel()
                    cont.resume(Result.success(userData))
                } else {
                    val exception = task.exception ?: Exception("Unknown error occurred.")
                    cont.resume(Result.failure(exception))
                }
            }
    }

    actual fun setLanguageCodeLocale(locale: String) {
        android.setLanguageCode(locale)
    }

    actual suspend fun kUpdateProfile(displayName: String?, photoUrl: String?): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            if (currentUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName ?: currentUser!!.displayName)
                    .apply {
                        photoUrl?.let { photoUri = Uri.parse(it) }
                    }
                    .build()

                currentUser!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(true))
                        } else {
                            val exception = task.exception ?: Exception("Unknown error occurred.")
                            cont.resume(Result.failure(exception))
                        }
                    }
            } else {
                cont.resume(Result.failure(Exception("No user is currently signed in.")))
            }
        }

//    actual suspend fun signInWithCredential(credential: AuthCredential): Result<KFirebaseUser?> =
//        suspendCancellableCoroutine { cont ->
//            android.signInWithCredential(credential.android)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        currentUser = task.result.user
//                        val userData = task.result?.user?.toModel()
//                        cont.resume(Result.success(userData))
//                    } else {
//                        val exception = task.exception ?: Exception("Unknown error occurred.")
//                        cont.resume(Result.failure(exception))
//                    }
//                }
//        }

    actual suspend fun isLinkEmail(email: String): Boolean {
        return android.isSignInWithEmailLink(email)
    }

    actual suspend fun confirmPasswordReset(code: String, newPassword: String): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            android.confirmPasswordReset(code, newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(task.isSuccessful))
                    } else {
                        val exception = task.exception ?: Exception("Unknown error occurred.")
                        cont.resume(Result.failure(exception))
                    }
                }
        }

    actual suspend fun addListenerAuthStateChange(): Result<KFirebaseUser?> =
        suspendCancellableCoroutine { cont ->
            android.addAuthStateListener { firebaseAuth ->
                val currentUser = firebaseAuth.currentUser
                cont.resume(Result.success(currentUser?.toModel()))
            }
        }

    actual suspend fun addListenerIdTokenChanged(): Result<KFirebaseUser?> =
        suspendCancellableCoroutine { cont ->
            android.addAuthStateListener { firebaseAuth ->
                val currentUser = firebaseAuth.currentUser
                cont.resume(Result.success(currentUser?.toModel()))
            }
        }

    actual suspend fun removeListenerAuthStateChange(): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            authStateListener?.let {
                android.removeAuthStateListener(it)
                cont.resume(Result.success(true))
            } ?: cont.resume(Result.failure(Exception("No auth state listener found.")))
        }

    // Remove IdTokenListener
    actual suspend fun removeListenerIdTokenChanged(): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            idTokenListener?.let {
                android.removeIdTokenListener(it)
                cont.resume(Result.success(true))
            } ?: cont.resume(Result.failure(Exception("No id token listener found.")))
        }


    actual var languageCode: String?
        get() = android.languageCode
        set(value) {
            setLanguageCodeLocale(value!!)
        }

    actual suspend fun applyActionWithCode(code: String): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            android.applyActionCode(code)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(Result.success(true))
                    } else {
                        val exception = task.exception ?: Exception("Unknown error occurred.")
                        cont.resume(Result.failure(exception))
                    }
                }
        }

//    actual suspend fun <T : ActionCodeResult> checkActionWithCode(code: String): Result<T> {
//        return suspendCancellableCoroutine { cont ->
//            android.checkActionCode(code)
//                .addOnCompleteListener { task ->
//                    if (task.exception != null) {
//                        cont.resumeWithException(Exception(task.exception))
//                        return@addOnCompleteListener
//                    }
//                    val result = task.result
//                    val operation = task.result.operation
//
//                    val resOperation = when (operation) {
//                        SIGN_IN_WITH_EMAIL_LINK -> ActionCodeResult.SignInWithEmailLink
//                        VERIFY_EMAIL -> ActionCodeResult.VerifyEmail(result.info!!.email)
//                        PASSWORD_RESET -> ActionCodeResult.PasswordReset(
//                            result.info!!.email
//                        )
//
//                        RECOVER_EMAIL -> (result.info as ActionCodeEmailInfo).run {
//                            ActionCodeResult.RecoverEmail(
//                                email,
//                                previousEmail
//                            )
//                        }
//
//                        VERIFY_BEFORE_CHANGE_EMAIL -> (result.info as ActionCodeEmailInfo).run {
//                            ActionCodeResult.VerifyBeforeChangeEmail(
//                                email,
//                                previousEmail
//                            )
//                        }
//
//                        REVERT_SECOND_FACTOR_ADDITION -> (result.info as ActionCodeMultiFactorInfo).run {
//                            ActionCodeResult.RevertSecondFactorAddition(
//                                email,
//                               // MultiFactorInfo(multiFactorInfo)
//                            )
//                        }
//
//                        ERROR -> throw UnsupportedOperationException(result.operation.toString())
//                        else -> throw UnsupportedOperationException(result.operation.toString())
//                    } as T
//
//                    cont.resume(Result.success(resOperation))
//                }
//        }
//    }

//actual suspend fun KFirebaseUser.kUpdateEmail(email: String): Result<Boolean?> =
//    suspendCancellableCoroutine { cont ->
//    currentUser?.updateEmail(email)?.addOnCompleteListener { task ->
//        if (task.isSuccessful) {
//            cont.resume(Result.success(true))
//        } else {
//            val exception = task.exception ?: Exception("Unknown error occurred.")
//            cont.resume(Result.failure(exception))
//        }
//    }
//}
//
//actual suspend fun KFirebaseUser.kSendEmailVerification(): Result<Boolean?> =
//    suspendCancellableCoroutine { cont ->
//    currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
//        if (task.isSuccessful) {
//            cont.resume(Result.success(true))
//        } else {
//            val exception = task.exception ?: Exception("Unknown error occurred.")
//            cont.resume(Result.failure(exception))
//        }
//    }
//}

    actual suspend fun kResetPassword(password: String): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            currentUser?.updatePassword(password)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(Result.success(true))
                } else {
                    val exception = task.exception ?: Exception("Unknown error occurred.")
                    cont.resume(Result.failure(exception))
                }
            }
        }

    actual suspend fun kDelete(): Result<Boolean?> = suspendCancellableCoroutine { cont ->
        currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser = null
                cont.resume(Result.success(true))
            } else {
                val exception = task.exception ?: Exception("Unknown error occurred.")
                cont.resume(Result.failure(exception))
            }
        }
    }

    actual suspend fun kSignOut(): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            KFirebaseAuth().android.signOut()
            currentUser = null
            cont.resume(Result.success(true))
        }

    actual suspend fun sendOtp(
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onCodeSentFailed: (Throwable) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNumber)       // الرقم المراد إرسال OTP إليه
            .setTimeout(60L, TimeUnit.SECONDS) // المهلة
            .setActivity(AndroidKFirebaseAuth.getActivity()!!)              // النشاط المستخدم
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // التحقق من OTP تم بنجاح
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onCodeSentFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    actual suspend fun verifyOtp(
        verificationId: String,
        otpCode: String,
        onVerificationCompleted: (user: KFirebaseUser) -> Unit,
        onVerificationFailed: (Throwable) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)

        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = task.result?.user
                    currentUser?.toModel()?.let { user ->
                        onVerificationCompleted(user)
                    } ?: onVerificationFailed(Exception("User data is null"))
                } else {
                    val exception = task.exception ?: Exception("Unknown error occurred.")
                    onVerificationFailed(exception)
                }
            }
    }


    actual suspend fun getClient(): String {
        return AndroidKFirebaseAuth.webClientId ?: throw Exception("Web client ID not set.")
    }

    actual suspend fun signInWithCredential(credential: KAuthCredentials): Result<KFirebaseUser?> {
        return try {
            val authCredential = credential.toModel()
            val result = android.signInWithCredential(authCredential).await()
            Result.success(result.user?.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    actual suspend fun linkProvider(credential: KAuthCredentials): Result<KFirebaseUser?> {
        return try {
            val authCredential = credential.toModel()
            val result = android.currentUser?.linkWithCredential(authCredential)?.await()
            Result.success(result?.user?.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun kSendEmailVerification(): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                currentUser.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(true))
                        } else {
                            cont.resume(
                                Result.failure(
                                    task.exception ?: Exception("Unknown error")
                                )
                            )
                        }
                    }
            } else {
                cont.resume(Result.failure(Exception("No user is logged in")))
            }
        }

    actual suspend fun kUpdateEmail(newEmail: String): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                currentUser.verifyBeforeUpdateEmail(newEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(true))
                        } else {
                            cont.resume(
                                Result.failure(
                                    task.exception ?: Exception("Unknown error")
                                )
                            )
                        }
                    }
            } else {
                cont.resume(Result.failure(Exception("No user is logged in")))
            }
        }
}


//
//actual class MultiFactorInfo(private val android: com.google.firebase.auth.MultiFactorInfo) {
//    actual val displayName: String?
//        get() = android.displayName
//    actual val enrollmentTime: Double?
//        get() = android.enrollmentTimestamp.toDouble()
//    actual val factorId: String
//        get() = android.factorId
//    actual val uid: String
//        get() = android.uid
//}
//
//actual suspend fun KFirebaseUser.linkProvider(credential: AuthCredential): Result<KFirebaseUser?> {
//    return try {
//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            val result = user.linkWithCredential(credential.android).await()
//            // Return the linked user as KFirebaseUser
//            Result.success(result.user?.toModel())
//        } else {
//            // Handle the case when there is no authenticated user
//            Result.failure(Exception("No authenticated user found"))
//        }
//    } catch (e: Exception) {
//        // Handle any exceptions during the linking process
//        Result.failure(e)
//    }
//}