package io.gituhb.demo

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.firebase_auth.AuthProvider
import io.github.firebase_auth.KAuthCredentials
import io.github.firebase_auth.KFirebaseAuth
import io.github.firebase_auth.KFirebaseUser
import io.github.sign_in_with_google.KGoogleSignIn
import io.gituhb.demo.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun App() = AppTheme {
    val kFirebaseAuth = KFirebaseAuth()
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<KFirebaseUser?>(null) }
    var verificationId by remember { mutableStateOf("") }
    val googleSign = KGoogleSignIn()

    LaunchedEffect(Unit) {
        user = kFirebaseAuth.currentUser().getOrNull()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Button(
                enabled = user != null,
                onClick = {
                    scope.launch {
                        val res = kFirebaseAuth.kUpdateEmail("eng.michelle.raouf@gmail.com")
                        res.onFailure {
                            println("kUpdateEmail failed: $it")
                        }
                        res.onSuccess {
                            println("kUpdateEmail success: $it")
                        }
                    }
                })

            {
                Text("updateEmail")
            }
        }
        item {
            Button(onClick = {
                scope.launch {
                    val res = kFirebaseAuth.createUserWithEmailAndPassword(
                        "meshoraouf515@gmail.com",
                        "123456"
                    )
                    res.onFailure {
                        println("createUserWithEmailAndPassword failed: $it")
                    }
                    res.onSuccess {
                        user = it
                        println("createUserWithEmailAndPassword success: $it")
                    }
                }
            }) {
                Text("Create Email")

            }
        }

        item {
            Button(onClick = {
                scope.launch {
                    val res = kFirebaseAuth.signInWithEmailAndPassword(
                        "meshoraouf515@gmail.com",
                        "123456"
                    )

                    res.onFailure {
                        println("sign in with email failed: $it")
                    }
                    res.onSuccess {
                        user = it
                        println("sign in with email success: $it")
                    }
                }
            }) {
                Text("Sign in with email")
            }

        }


        item {
            Text("this user email is verified : ${user?.isEmailVerified}")
        }
        item {
            Button(
                enabled = user != null,
                onClick = {
                    scope.launch {
                        kFirebaseAuth.kSendEmailVerification()
                    }
                }
            ) {
                Text("Sent email verification")
            }


        }

        item {
            Button(onClick = {
                scope.launch {
                    val cred = googleSign.getCredential(kFirebaseAuth.getClient())

                    cred.onSuccess {

                        println("getCredential: $it")
                        val signFirebase = kFirebaseAuth.signInWithCredential(
                            credential = KAuthCredentials(
                                idToken = it.idToken,
                                accessToken = it.accessToken ?: "",
                                provider = AuthProvider.GOOGLE
                            )
                        )
                        signFirebase.onFailure {
                            println("signInWithCredential failed: $it")
                        }
                        signFirebase.onSuccess {
                            user = it
                            println("signInWithCredential success: $it")
                        }
                    }
                    cred.onFailure {
                        println("getCredential failed: $it")
                    }
                }
            }) {
                Text("Sign in with google")
            }
        }
        item {
            Button(
                enabled = user != null,
                onClick = {
                    scope.launch {
                        googleSign.signOut()
                        kFirebaseAuth.kSignOut()
                        user = null
                    }
                }) {
                Text("Sign out")
            }
        }
        item {
            ElevatedButton(
                onClick = {
                    scope.launch {
                        kFirebaseAuth.sendOtp("+20 10 12661795", onCodeSent = {
                            verificationId = it
                        }, onCodeSentFailed = {
                            println("onCodeSentFailed: $it")
                        })
                    }
                }
            ) {
                Text(text = "Send OTP")
            }

        }
        item {
            ElevatedButton(
                enabled = verificationId.isNotBlank(),
                onClick = {
                    scope.launch {
                        kFirebaseAuth.verifyOtp(
                            verificationId,
                            "111111",
                            onVerificationCompleted = {
                                user = it
                            },
                            onVerificationFailed = {
                                println("onVerificationFailed: $it")
                            })
                    }
                }
            ) {
                Text(text = "Verify OTP")
            }

        }
        item {
            Text(
                text = "User: ${user?.uid ?: "null"}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
