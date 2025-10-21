package com.example.proyecto.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto.data.local.AppDatabase
import com.example.proyecto.data.mapper.toDomain
import com.example.proyecto.data.mapper.toEntity
import com.example.proyecto.domain.model.User
import com.example.proyecto.domain.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch


class AuthRepository(
    private val database: AppDatabase,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val userCacheDao = database.userCacheDao()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Fetch user data when authenticated
                launch {
                    val user = getCurrentUser()
                    trySend(user)
                }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Validate UVG email
            if (!email.endsWith("@uvg.edu.gt")) {
                return Result.failure(Exception("Debe usar un correo institutional UVG"))
            }

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(
                Exception("Error al obtener usuario")
            )

            val user = fetchUserData(userId)

            // Cache user locally
            userCacheDao.insertUser(user.toEntity())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        return try {
            // Validate UVG email
            if (!email.endsWith("@uvg.edu.gt")) {
                return Result.failure(Exception("Debe usar un correo institucional UVG"))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(
                Exception("Error al crear usuario")
            )

            // Create user document in Firestore
            val user = User(
                id = userId,
                name = name,
                email = email,
                role = UserRole.STUDENT
            )

            firestore.collection("users")
                .document(userId)
                .set(mapOf(
                    "id" to user.id,
                    "name" to user.name,
                    "email" to user.email,
                    "role" to user.role.name
                ))
                .await()

            // Cache user locally
            userCacheDao.insertUser(user.toEntity())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val userId = currentUserId ?: return null

        // Try cache first
        val cached = userCacheDao.getUserById(userId)
        if (cached != null) return cached.toDomain()

        // Fetch from Firestore
        return fetchUserData(userId)
    }

    private suspend fun fetchUserData(userId: String): User {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val data = doc.data ?: throw Exception("Usuario no encontrado")

            User(
                id = data["id"] as? String ?: userId,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                role = UserRole.valueOf(data["role"] as? String ?: "STUDENT")
            )
        } catch (e: Exception) {
            // If user document doesn't exist, create one with auth data
            val firebaseUser = auth.currentUser
            User(
                id = userId,
                name = firebaseUser?.displayName ?: "",
                email = firebaseUser?.email ?: "",
                role = UserRole.STUDENT
            )
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            userCacheDao.clearAll()
            database.reservationDao().clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}