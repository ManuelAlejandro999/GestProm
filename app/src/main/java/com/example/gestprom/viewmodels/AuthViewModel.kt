package com.example.gestprom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestprom.models.Usuario
import com.example.gestprom.services.AuthService
import com.example.gestprom.services.FirestoreService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authService = AuthService()
    private val firestoreService = FirestoreService()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<Usuario?>(null)
    val userData: StateFlow<Usuario?> = _userData.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = authService.getCurrentUser()
        _currentUser.value = user
        if (user != null) {
            _authState.value = AuthState.Authenticated
            loadUserData(user.uid)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.loginWithEmail(email, password)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated
                        loadUserData(user.uid)
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("password") == true -> "Contraseña incorrecta"
                            exception.message?.contains("user") == true -> "Usuario no encontrado"
                            exception.message?.contains("network") == true -> "Error de conexión"
                            exception.message?.contains("too many") == true -> "Demasiados intentos. Intenta más tarde"
                            else -> "Error de autenticación: ${exception.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun register(nombreCompleto: String, matricula: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.registerWithEmail(email, password)
                result.fold(
                    onSuccess = { user ->
                        // Create user document in Firestore
                        val usuario = Usuario(
                            id = user.uid,
                            nombreCompleto = nombreCompleto,
                            matricula = matricula,
                            correo = email,
                            contraseñaHash = authService.hashPassword(password)
                        )
                        
                        val createUserResult = firestoreService.createUser(usuario)
                        createUserResult.fold(
                            onSuccess = {
                                _currentUser.value = user
                                _userData.value = usuario
                                _authState.value = AuthState.Authenticated
                            },
                            onFailure = { exception ->
                                _authState.value = AuthState.Error(exception.message ?: "Error al crear usuario")
                            }
                        )
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Error de registro")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            try {
                val result = firestoreService.getUser(userId)
                result.fold(
                    onSuccess = { usuario ->
                        _userData.value = usuario
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Error al cargar datos del usuario")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun logout() {
        authService.logout()
        _currentUser.value = null
        _userData.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Initial
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
} 