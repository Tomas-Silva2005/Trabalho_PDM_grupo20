package pt.ubi.securenote.security

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse

class CredentialManagerHelper(private val appContext: Context) {

    private val credentialManager =
        CredentialManager.create(appContext)

    suspend fun getCredential(
        request: GetCredentialRequest
    ): GetCredentialResponse {
        return credentialManager.getCredential(
            appContext,
            request
        )
    }
}