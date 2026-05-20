package com.example.summitnavigator.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { _authState.value = AuthState.Success }
            .addOnFailureListener { e -> _authState.value = AuthState.Error(e.message ?: "Login failed") }
    }

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { _authState.value = AuthState.Success }
            .addOnFailureListener { e -> _authState.value = AuthState.Error(e.message ?: "Sign up failed") }
    }
}
