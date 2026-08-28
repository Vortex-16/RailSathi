package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

data class GoogleSignInResult(
    val success: Boolean,
    val idToken: String? = null,
    val googleId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val errorMessage: String? = null
)

class GoogleAuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    // Optional web client ID (can be overridden or read from BuildConfig/Resources)
    var webClientId: String = "100234567890-railsaathi.apps.googleusercontent.com"

    suspend fun signInWithGoogle(activityContext: Context): GoogleSignInResult {
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("GoogleAuthManager", "Google Sign-In successful: ${googleIdTokenCredential.id}")
                GoogleSignInResult(
                    success = true,
                    idToken = googleIdTokenCredential.idToken,
                    googleId = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Commuter",
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                Log.w("GoogleAuthManager", "Unexpected credential type: ${credential.type}")
                GoogleSignInResult(
                    success = false,
                    errorMessage = "Unexpected credential received"
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i("GoogleAuthManager", "Google Sign-in was cancelled by user")
            GoogleSignInResult(
                success = false,
                errorMessage = "Sign-in cancelled"
            )
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthManager", "Credential Manager failed: ${e.message}", e)
            // If in test/emulator environment without active Google account, provide graceful fallback
            GoogleSignInResult(
                success = false,
                errorMessage = e.message ?: "Sign-in failed"
            )
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Sign-in general error: ${e.message}", e)
            GoogleSignInResult(
                success = false,
                errorMessage = e.message ?: "Unknown authentication error"
            )
        }
    }
}
