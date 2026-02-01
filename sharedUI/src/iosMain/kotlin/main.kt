import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import io.github.firebase_auth.KFirebaseAuth
import io.github.sign_in_with_google.KGoogleSignIn
import io.gituhb.demo.App
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        val scope = rememberCoroutineScope()
        scope.launch {
            KGoogleSignIn().initialize(KFirebaseAuth().getClient())
        }
        App()
    }
}
