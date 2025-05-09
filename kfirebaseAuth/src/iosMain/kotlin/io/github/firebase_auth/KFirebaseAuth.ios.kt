package io.github.firebase_auth

import io.github.native.kfirebase.auth.AuthCredentials
import io.github.native.kfirebase.auth.KFirebaseAuthInterop
import io.github.native.kfirebase.auth.UserModel
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSURL
import toModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class ActionCodeResultEnum(val rawValue: Int) {
    SignInWithEmailLink(0),
    VerifyEmail(1),
    RecoverEmail(2),
    RevertSecondFactorAddition(3),
    VerifyBeforeChangeEmail(4);

    companion object {
        fun fromRawValue(value: Int): ActionCodeResultEnum? {
            return entries.find { it.rawValue == value }
        }
    }
}


@OptIn(ExperimentalForeignApi::class)
actual class KFirebaseAuth {

    companion object {
        @OptIn(ExperimentalForeignApi::class)
        val ios = KFirebaseAuthInterop()
        private var currentUser: UserModel? = null
    }


    init {
        currentUser = ios.getCurrentUser()
    }

    actual suspend fun currentUser(): Result<KFirebaseUser?> {
        return try {
            val fireUser = ios.getCurrentUser()
            if (fireUser != null) {
                currentUser = fireUser
                Result.success(fireUser.toModel())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signInAnonymously(): Result<KFirebaseUser?> {
        return suspendCancellableCoroutine { continuation ->
            ios.signInAnonymouslyWithCompletion { authResult, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    currentUser = authResult
                    continuation.resume(Result.success(authResult?.toModel()))
                }
            }
        }
    }

    actual suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?> {
        return suspendCancellableCoroutine { continuation ->

            ios.createUserWithEmailAndPasswordWithEmail(email, password) { authResult, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    currentUser = authResult
                    continuation.resume(Result.success(authResult?.toModel()))
                }
            }
        }
    }

    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<KFirebaseUser?> {
        return suspendCancellableCoroutine { continuation ->

            ios.signInWithEmail(
                email = email,
                password = password,
                completion = { authResult: UserModel?, error: NSError? ->
                    if (error != null) {
                        continuation.resume(Result.failure(Exception(error.localizedDescription)))
                    } else {
                        val userData = authResult?.toModel()!!
                        currentUser = authResult
                        continuation.resume(Result.success(userData))
                    }
                })

        }
    }

    actual suspend fun addListenerAuthStateChange(): Result<KFirebaseUser?> {
        return suspendCancellableCoroutine { continuation ->
            ios.addListenerAuthStateChangeWithCompletion { authUser ->
                if (authUser != null) {
                    continuation.resume(Result.success(authUser.toModel()))
                } else {
                    continuation.resume(Result.success(null))
                }
            }
        }
    }

    actual suspend fun addListenerIdTokenChanged(): Result<KFirebaseUser?> {
        return suspendCancellableCoroutine { continuation ->
            ios.addListenerAuthStateChangeWithCompletion { authUser ->
                if (authUser != null) {
                    continuation.resume(Result.success(authUser.toModel()))
                } else {
                    continuation.resume(Result.success(null))
                }
            }
        }
    }

    actual suspend fun removeListenerAuthStateChange(): Result<Boolean?> {
        ios.removeListenerAuthStateChange()
        return Result.success(true)
    }

    actual suspend fun removeListenerIdTokenChanged(): Result<Boolean?> {
        ios.removeListenerIdTokenChanged()
        return Result.success(true)
    }


    actual suspend fun confirmPasswordReset(
        code: String,
        newPassword: String
    ): Result<Boolean?> {
        return suspendCancellableCoroutine { continuation ->
            ios.confirmPasswordResetWithCode(code, newPassword) { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(true))
                }
            }
        }
    }

    actual fun setLanguageCodeLocale(locale: String) {
        ios.setLanguageCodeLocaleWithLanguageCode(locale)
    }

    actual suspend fun kUpdateProfile(
        displayName: String?,
        photoUrl: String?
    ): Result<Boolean?> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("No current user"))
            user.setDisplayName(displayName)
            if (photoUrl != null) {
                user.setPhotoURL(photoUrl)
            }
            val nsPhoto = if (photoUrl != null) NSURL.URLWithString(photoUrl) else null

            suspendCancellableCoroutine { continuation ->
                ios.updateProfileWithDisplayName(displayName, nsPhoto) { error ->
                    if (error != null) {
                        continuation.resume(Result.failure(Exception(error.localizedDescription)))
                    } else {
                        continuation.resume(Result.success(true))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    actual suspend fun isLinkEmail(email: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            ios.isLinkEmailWithEmail(email) { res, error ->
                if (error != null) {
                    continuation.resume(false)
                } else {
                    continuation.resume(res)
                }
            }
        }

    actual var languageCode: String?
        get() = ios.getLanguageCodeLocale()
        set(value) {
            setLanguageCodeLocale(value!!)
        }

    actual suspend fun applyActionWithCode(code: String): Result<Boolean?> {
        return suspendCancellableCoroutine { continuation ->
            ios.applyActionWithCodeWithCode(code) { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(true))
                }
            }
        }
    }
//
//    actual suspend fun <T : ActionCodeResult> checkActionWithCode(code: String): Result<T> {        return suspendCancellableCoroutine { continuation ->
//            ios.checkActionWithCodeWithCode(code) { wrapper: ActionCodeResultWrapper? ->
//                if (wrapper == null) {
//                    continuation.resume(Result.failure(Exception("Wrapper is null")))
//                    return@checkActionWithCodeWithCode
//                }
//
//                val error = wrapper.error()
//                if (error != null) {
//                    val message = error.localizedDescription ?: "Unknown error"
//                    continuation.resume(Result.failure(Exception(message)))
//                    return@checkActionWithCodeWithCode
//                }
//
//                val raw = wrapper.result()?.intValue
//                if (raw == null) {
//                    continuation.resume(Result.failure(Exception("Result is null")))
//                    return@checkActionWithCodeWithCode
//                }
//
//                val result = ActionCodeResultEnum.fromRawValue(raw)
//                if (result != null) {
//                    continuation.resume(Result.success(result as T))
//                } else {
//                    continuation.resumeWithException(IllegalArgumentException("Unknown ActionCodeResult rawValue: $raw"))
//                }
//            }
//        }
//    }

    actual suspend fun sendOtp(
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onCodeSentFailed: (Throwable) -> Unit
    ) {
        ios.sendOtpWithPhoneNumber(phoneNumber, { verificationId ->
            if (verificationId != null) {
                onCodeSent(verificationId)
            } else {
                onCodeSentFailed(Exception("Verification ID is null"))
            }
        }, { error ->
            if (error != null) {
                onCodeSentFailed(Exception(error.localizedDescription))
            } else {
                onCodeSentFailed(Exception("Unknown error"))
            }
        })
    }

    actual suspend fun verifyOtp(
        verificationId: String,
        otpCode: String,
        onVerificationCompleted: (user: KFirebaseUser) -> Unit,
        onVerificationFailed: (Throwable) -> Unit
    ) {
        ios.verifyOtpWithVerificationId(verificationId, otpCode, { authResult ->
            if (authResult != null) {
                currentUser = authResult
                onVerificationCompleted(authResult.toModel())
            } else {
                onVerificationFailed(Exception("Auth result is null"))
            }
        }, { error ->
            if (error != null) {
                onVerificationFailed(Exception(error.localizedDescription))
            } else {
                onVerificationFailed(Exception("Unknown error"))
            }

        })
    }

    actual suspend fun kResetPassword(password: String): Result<Boolean?> {
        return suspendCancellableCoroutine { continuation ->
            ios.updatePasswordWithNewPassword(password) { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(true))
                }
            }
        }
    }

    actual suspend fun kDelete(): Result<Boolean?> {
        return suspendCancellableCoroutine { continuation ->
            ios.deleteCurrentUserWithCompletion { error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    continuation.resume(Result.success(true))
                }
            }
        }

    }

    @OptIn(BetaInteropApi::class)
    actual suspend fun kSignOut(): Result<Boolean?> {
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()

            // Attempt to sign out
            ios.logoutWithCompletion {
                error.value = it
            }

            if (error.value == null) {
                currentUser = null
                return Result.success(true) // Sign out was successful
            } else {
                // Handle sign out error
                val errorMessage = error.value?.localizedDescription ?: "Unknown error"
                return Result.failure(Exception(errorMessage)) // Pass the error to the callback
            }

        }
    }

    actual suspend fun signInWithCredential(credential: KAuthCredentials): Result<KFirebaseUser?> {
        return suspendCoroutine { continuation ->
            ios.signInWithCredentialWithCredentials(
                AuthCredentials(
                    idToken = credential.idToken,
                    accessToken = credential.accessToken,
                    provider = credential.provider.ordinal.toLong()
                )
            ) { authResult, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    currentUser = authResult
                    continuation.resume(Result.success(authResult?.toModel()))
                }
            }
        }
    }


    actual suspend fun linkProvider(credential: KAuthCredentials): Result<KFirebaseUser?> {
        return suspendCoroutine { continuation ->
            ios.linkProviderWithCredentials(
                AuthCredentials(
                    idToken = credential.idToken,
                    accessToken = credential.accessToken,
                    provider = credential.provider.ordinal.toLong()
                )
            ) { authResult, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    currentUser = authResult
                    continuation.resume(Result.success(authResult?.toModel()))
                }
            }
        }
    }

    actual suspend fun getClient(): String {
        return suspendCoroutine { continuation ->
            ios.getClientIdWithCompletion { clientId ->
                continuation.resume(clientId ?: "")
            }
        }
    }

    actual suspend fun kSendEmailVerification(): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            ios.sendEmailVerificationWithCompletion { error ->
                if (error != null) {
                    cont.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    cont.resume(Result.success(true))
                }
            }
        }

    actual suspend fun kUpdateEmail(newEmail: String): Result<Boolean?> =
        suspendCancellableCoroutine { cont ->
            ios.updateEmailWithNewEmail(newEmail) { error ->
                if (error != null) {
                    cont.resume(Result.failure(Exception(error.localizedDescription)))
                } else {
                    cont.resume(Result.success(true))
                }
            }
        }


}

internal suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(Exception(error.localizedDescription))
        }
    }
    return job.await() as R
}