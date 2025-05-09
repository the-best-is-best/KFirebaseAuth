import io.github.firebase_auth.KFirebaseUser
import io.github.firebase_auth.KFirebaseUserMetaData
import io.github.native.kfirebase.auth.UserMetaDataModel
import io.github.native.kfirebase.auth.UserModel
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun UserMetaDataModel.toModel(): KFirebaseUserMetaData = KFirebaseUserMetaData(
    creationTime = this.creationTime(),
    lastSignInTime = this.lastSignInTime()
)


@OptIn(ExperimentalForeignApi::class)
fun UserModel.toModel(): KFirebaseUser {
    return KFirebaseUser(
        uid = this.uid(),
        displayName = this.displayName(),
        email = this.email(),
        phoneNumber = this.phoneNumber(),
        photoURL = this.photoURL(),
        isAnonymous = this.isAnonymous(),
        isEmailVerified = this.isEmailVerified(),
        metaData = this.metaData()?.toModel(),
    )
}
