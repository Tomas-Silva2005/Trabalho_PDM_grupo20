package pt.ubi.securenote.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ubi.securenote.repository.AuthRepository

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {

        _loginState.value = LoginState.Loading

        val success = authRepository.login(email, password)

        if (success) {
            _loginState.value = LoginState.Success
        } else {
            _loginState.value = LoginState.Error(
                "Email ou palavra-passe incorretos."
            )
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}