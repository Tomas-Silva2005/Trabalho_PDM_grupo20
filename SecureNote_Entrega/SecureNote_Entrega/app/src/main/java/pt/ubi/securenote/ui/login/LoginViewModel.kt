package pt.ubi.securenote.ui.login

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.credentials.exceptions.CreateCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ubi.securenote.security.AuthRepository
import pt.ubi.securenote.security.CredentialManagerHelper
import pt.ubi.securenote.security.SignInResult

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class RegisteredSuccess(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val credentialHelper = CredentialManagerHelper(app)
    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    companion object {
        private const val TAG = "LoginViewModel"
    }

    // Login com email + palavra-passe (chamado pelo MainActivity)
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val ok = repository.loginWithPassword(email, password)
                _loginState.value =
                    if (ok) LoginState.Success else LoginState.Error("Credenciais inválidas.")
            } catch (e: Exception) {
                Log.e(TAG, "Login with password error", e)
                _loginState.value = LoginState.Error("Erro de rede ou servidor (${e.message}).")
            }
        }
    }

    fun loginWithPasskey(activity: Activity) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val options = repository.getAuthenticationOptions()
                Log.d(TAG, "Authentication options: $options")
                when (val result = credentialHelper.signIn(activity, options)) {
                    is SignInResult.Passkey -> {
                        val ok = repository.verifyAssertion(result.responseJson)
                        _loginState.value =
                            if (ok) LoginState.Success else LoginState.Error("Autenticação recusada.")
                    }
                    is SignInResult.Password -> {
                        val ok = repository.loginWithPassword(result.username, result.password)
                        _loginState.value =
                            if (ok) LoginState.Success else LoginState.Error("Credenciais inválidas.")
                    }
                    is SignInResult.Failure -> {
                        _loginState.value = LoginState.Error(result.message)
                    }
                    else -> _loginState.value = LoginState.Error("Tipo de credencial não suportado.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login with passkey error", e)
                _loginState.value = LoginState.Error("Erro de rede ou servidor (${e.message}).")
            }
        }
    }

    fun loginWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val webClientId = "64543197808-prqsspvq72d1so47qbo8f4ktmlji6mvr.apps.googleusercontent.com"
                when (val result = credentialHelper.signInWithGoogle(activity, webClientId)) {
                    is SignInResult.Google -> {
                        val ok = repository.loginWithGoogle(result.email, result.idToken)
                        _loginState.value =
                            if (ok) LoginState.Success else LoginState.Error("Autenticação Google recusada pelo servidor.")
                    }
                    is SignInResult.Failure -> {
                        _loginState.value = LoginState.Error(result.message)
                    }
                    else -> _loginState.value = LoginState.Error("Resposta inesperada da Conta Google.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login with Google error", e)
                _loginState.value = LoginState.Error("Erro ao iniciar sessão com Google (${e.message}).")
            }
        }
    }

    fun registerPasskey(activity: Activity, username: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val options = repository.getRegistrationOptions(username)
                Log.d(TAG, "Registration options: $options")
                val responseJson = credentialHelper.registerPasskey(activity, options)
                Log.d(TAG, "Registration response: $responseJson")
                val ok = repository.verifyRegistration(responseJson)
                _loginState.value =
                    if (ok) LoginState.RegisteredSuccess("Passkey registada com sucesso! Já podes iniciar sessão.")
                    else LoginState.Error("Registo recusado pelo servidor.")
            } catch (e: CreateCredentialException) {
                Log.e(TAG, "Register passkey create exception", e)
                _loginState.value = LoginState.Error(e.message ?: "Falha ao criar passkey.")
            } catch (e: Exception) {
                Log.e(TAG, "Register passkey error", e)
                _loginState.value = LoginState.Error(e.message ?: "Erro ao criar passkey.")
            }
        }
    }
}
