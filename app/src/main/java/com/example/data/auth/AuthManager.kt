package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

data class AuthResult(
    val success: Boolean,
    val idToken: String? = null,
    val googleId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val errorMessage: String? = null
)

/**
 * AuthManager manages Google One Tap and Credential Manager authentication flows
 * for secure session linkage with the backend and PostgreSQL users table.
 */
class AuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    // Configurable Google Web Client ID for OAuth2 / ID token exchange
    var serverClientId: String = "39959879941-8upe4qr36vc5q3cvkqt7vd4s0vq68cdl.apps.googleusercontent.com"

    /**
     * Triggers Google One-Tap / ID token sign-in using CredentialManager.
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        autoSelect: Boolean = false,
        filterByAuthorizedAccounts: Boolean = false
    ): AuthResult {
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(autoSelect)
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
                Log.d(TAG, "Google One Tap CredentialManager successful for: ${googleIdTokenCredential.id}")
                AuthResult(
                    success = true,
                    idToken = googleIdTokenCredential.idToken,
                    googleId = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Commuter",
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                Log.w(TAG, "Unexpected credential type: ${credential.type}")
                AuthResult(
                    success = false,
                    errorMessage = "Unexpected credential format returned"
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "Google One Tap sign-in cancelled by user")
            AuthResult(
                success = false,
                errorMessage = "Sign-in cancelled"
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error: ${e.message}", e)
            AuthResult(
                success = false,
                errorMessage = e.message ?: "Authentication failed"
            )
        } catch (e: Exception) {
            Log.e(TAG, "General AuthManager error: ${e.message}", e)
            AuthResult(
                success = false,
                errorMessage = e.message ?: "Unexpected authentication error"
            )
        }
    }

    /**
     * Clears cached credentials on logout.
     */
    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "Successfully cleared credential state")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear credential state: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "AuthManager"
    }
}
