package com.example.data.repo

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.AppUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firestore: FirebaseFirestore
) {
    private val auth: FirebaseAuth get() = Firebase.auth

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fAuth ->
            trySend(fAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun observeUserDoc(uid: String): Flow<AppUser?> {
        return firestore.collection("users").document(uid)
            .snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(AppUser::class.java) else null
            }
    }

    suspend fun ensureUserDoc(firebaseUser: FirebaseUser): AppUser {
        val docRef = firestore.collection("users").document(firebaseUser.uid)
        val snap = docRef.get().await()
        if (snap.exists()) {
            return snap.toObject(AppUser::class.java) ?: AppUser(
                uid = firebaseUser.uid,
                fullName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = "USER"
            )
        }

        // Determine initial role: owner is admin, or if first user
        val isOwnerEmail = firebaseUser.email.equals("florenceabeesiga@gmail.com", ignoreCase = true)
        val count = try {
            firestore.collection("users").limit(1).get().await().size()
        } catch (_: Exception) {
            0
        }
        val role = if (isOwnerEmail || count == 0) "ADMIN" else "USER"

        val newUser = AppUser(
            uid = firebaseUser.uid,
            fullName = firebaseUser.displayName ?: "Farm Member",
            email = firebaseUser.email ?: "",
            role = role
        )

        docRef.set(
            mapOf(
                "uid" to newUser.uid,
                "fullName" to newUser.fullName,
                "email" to newUser.email,
                "role" to newUser.role,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()

        return newUser
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onFallback: () -> Unit
    ) {
        val current = auth.currentUser
        if (current != null) {
            onSuccess(current)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onFallback()
            return
        }

        if (clientId.isBlank()) {
            onFallback()
            return
        }

        val googleIdOption = try {
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()
        } catch (_: Exception) {
            onFallback()
            return
        }

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        ensureUserDoc(user)
                        onSuccess(user)
                    } else {
                        onFallback()
                    }
                } else {
                    onFallback()
                }
            } catch (_: Exception) {
                onFallback()
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        ensureUserDoc(user)
                        onSuccess(user)
                    } else {
                        onError("Sign-in succeeded but user details were not found.")
                    }
                } else {
                    onError("Unexpected credential type returned from Google Sign-In.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In flow cancelled: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In error", e)
                onError(e.localizedMessage ?: "Failed to sign in with Google")
            }
        }
    }

    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
