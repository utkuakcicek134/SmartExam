package com.utku.smartexam.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utku.smartexam.data.model.User
import com.utku.smartexam.data.model.UserRole
import com.utku.smartexam.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: User? = null
)

data class RegisterState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val registerSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun updateLoginEmail(email: String) {
        _loginState.update { it.copy(email = email, error = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginState.update { it.copy(password = password, error = null) }
    }

    fun updateLoginRole(role: UserRole) {
        _loginState.update { it.copy(selectedRole = role, error = null) }
    }

    fun login() {
        val state = _loginState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }
            
            val result = userRepository.login(state.email, state.password)
            result.fold(
                onSuccess = { user ->
                    if (user.role == state.selectedRole) {
                        _loginState.update { it.copy(isLoading = false, loginSuccess = user) }
                    } else {
                        _loginState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "This account is registered as ${user.role.name.lowercase()}. Please select the correct role."
                            ) 
                        }
                    }
                },
                onFailure = { e ->
                    _loginState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState()
    }

    fun updateRegisterFullName(name: String) {
        _registerState.update { it.copy(fullName = name, error = null) }
    }

    fun updateRegisterEmail(email: String) {
        _registerState.update { it.copy(email = email, error = null) }
    }

    fun updateRegisterPassword(password: String) {
        _registerState.update { it.copy(password = password, error = null) }
    }

    fun updateRegisterConfirmPassword(password: String) {
        _registerState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun updateRegisterRole(role: UserRole) {
        _registerState.update { it.copy(selectedRole = role, error = null) }
    }

    fun register() {
        val state = _registerState.value
        
        when {
            state.fullName.isBlank() || state.email.isBlank() || 
            state.password.isBlank() || state.confirmPassword.isBlank() -> {
                _registerState.update { it.copy(error = "Please fill in all fields") }
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                _registerState.update { it.copy(error = "Please enter a valid email address") }
                return
            }
            state.password.length < 6 -> {
                _registerState.update { it.copy(error = "Password must be at least 6 characters") }
                return
            }
            state.password != state.confirmPassword -> {
                _registerState.update { it.copy(error = "Passwords do not match") }
                return
            }
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }
            
            val user = User(
                email = state.email,
                password = state.password,
                fullName = state.fullName,
                role = state.selectedRole
            )
            
            val result = userRepository.registerUser(user)
            result.fold(
                onSuccess = {
                    _registerState.update { it.copy(isLoading = false, registerSuccess = true) }
                },
                onFailure = { e ->
                    _registerState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState()
    }
}
