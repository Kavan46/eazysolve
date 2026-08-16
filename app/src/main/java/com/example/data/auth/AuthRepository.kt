package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isAnonymous: Boolean = false,
    val isSignedIn: Boolean = false
)

data class CloudSyncData(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val totalCoins: Int = 0,
    val totalXp: Int = 0,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val completedDailyDates: List<String> = emptyList(),
    val totalLevelsCleared: Int = 0,
    val totalStars: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    // Web Client ID from google-services.json
    val serverClientId = "964950733032-jnl95b4agp7ut9hl071t3snlgp370dot.apps.googleusercontent.com"

    private val _currentUserState = MutableStateFlow(getCurrentUserProfile())
    val currentUserState: StateFlow<UserProfile> = _currentUserState.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>("Ready")
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserState.value = mapFirebaseUser(firebaseAuth.currentUser)
        }
    }

    private fun mapFirebaseUser(user: FirebaseUser?): UserProfile {
        return if (user != null) {
            UserProfile(
                uid = user.uid,
                displayName = user.displayName ?: "Puzzle Master",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString() ?: "",
                isAnonymous = user.isAnonymous,
                isSignedIn = true
            )
        } else {
            UserProfile(
                displayName = "Guest Solver",
                isSignedIn = false
            )
        }
    }

    private fun getCurrentUserProfile(): UserProfile {
        return mapFirebaseUser(auth.currentUser)
    }

    suspend fun signInWithGoogle(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                val profile = mapFirebaseUser(user)
                _currentUserState.value = profile
                _syncStatus.value = "Connected as ${profile.displayName}"
                Result.success(profile)
            } else {
                Result.failure(Exception("Unsupported credential type received"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthRepository", "User cancelled Google Sign In")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google Sign In Error", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            auth.signOut()
            _currentUserState.value = UserProfile(displayName = "Guest Solver", isSignedIn = false)
            _syncStatus.value = "Signed out"
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing out", e)
        }
    }

    /**
     * Backup and sync user progress to Firebase Firestore
     */
    suspend fun syncUserDataToCloud(syncData: CloudSyncData): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            _syncStatus.value = "Guest mode (Local only)"
            return@withContext Result.success(false)
        }

        try {
            _syncStatus.value = "Syncing to Cloud..."
            val docRef = firestore.collection("users").document(user.uid)
            val dataMap = hashMapOf(
                "uid" to user.uid,
                "email" to (user.email ?: syncData.email),
                "displayName" to (user.displayName ?: syncData.displayName),
                "coins" to syncData.totalCoins,
                "xp" to syncData.totalXp,
                "currentStreak" to syncData.currentStreak,
                "longestStreak" to syncData.longestStreak,
                "completedDailyDates" to syncData.completedDailyDates,
                "totalLevelsCleared" to syncData.totalLevelsCleared,
                "totalStars" to syncData.totalStars,
                "lastUpdated" to System.currentTimeMillis()
            )

            docRef.set(dataMap, SetOptions.merge()).await()
            _syncStatus.value = "Cloud Backup Synced"
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to sync Firestore data", e)
            _syncStatus.value = "Sync failed: ${e.localizedMessage}"
            Result.failure(e)
        }
    }

    /**
     * Fetch user progress from Firebase Firestore on sign-in
     */
    suspend fun fetchCloudUserData(): CloudSyncData? = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext null
        try {
            val docRef = firestore.collection("users").document(user.uid)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: return@withContext null
                val completedDates = (data["completedDailyDates"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                CloudSyncData(
                    uid = user.uid,
                    email = data["email"]?.toString() ?: user.email ?: "",
                    displayName = data["displayName"]?.toString() ?: user.displayName ?: "Puzzle Champion",
                    totalCoins = (data["coins"] as? Number)?.toInt() ?: 100,
                    totalXp = (data["xp"] as? Number)?.toInt() ?: 50,
                    currentStreak = (data["currentStreak"] as? Number)?.toInt() ?: 1,
                    longestStreak = (data["longestStreak"] as? Number)?.toInt() ?: 1,
                    completedDailyDates = completedDates,
                    totalLevelsCleared = (data["totalLevelsCleared"] as? Number)?.toInt() ?: 0,
                    totalStars = (data["totalStars"] as? Number)?.toInt() ?: 0,
                    lastUpdated = (data["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to load Cloud Data", e)
            null
        }
    }
}
