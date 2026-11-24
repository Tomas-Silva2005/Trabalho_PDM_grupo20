package pt.ubi.securenote

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import pt.ubi.securenote.ui.login.LoginState
import pt.ubi.securenote.ui.login.LoginViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnPasskey = findViewById<Button>(R.id.btnPassKey)
        val btnRegisterPasskey = findViewById<Button>(R.id.btnRegisterPasskey)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)
        val textError = findViewById<TextView>(R.id.textError)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        loginViewModel.loginState.observe(this) { state ->
            when (state) {

                is LoginState.Idle -> {
                    btnLogin.isEnabled = true
                    btnPasskey.isEnabled = true
                    btnRegisterPasskey.isEnabled = true
                    btnGoogleSignIn.isEnabled = true
                    btnLogin.text = "Iniciar sessão"
                    textError.visibility = View.GONE
                }

                is LoginState.Loading -> {
                    btnLogin.isEnabled = false
                    btnPasskey.isEnabled = false
                    btnRegisterPasskey.isEnabled = false
                    btnGoogleSignIn.isEnabled = false
                    btnLogin.text = "A validar..."
                    textError.visibility = View.GONE
                }

                is LoginState.RegisteredSuccess -> {
                    btnLogin.isEnabled = true
                    btnPasskey.isEnabled = true
                    btnRegisterPasskey.isEnabled = true
                    btnGoogleSignIn.isEnabled = true
                    btnLogin.text = "Iniciar sessão"
                    textError.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }

                is LoginState.Success -> {
                    btnLogin.isEnabled = true
                    btnPasskey.isEnabled = true
                    btnRegisterPasskey.isEnabled = true
                    btnGoogleSignIn.isEnabled = true
                    btnLogin.text = "Iniciar sessão"
                    textError.visibility = View.GONE

                    startActivity(Intent(this, NotesActivity::class.java))
                    finish()
                }

                is LoginState.Error -> {
                    btnLogin.isEnabled = true
                    btnPasskey.isEnabled = true
                    btnRegisterPasskey.isEnabled = true
                    btnGoogleSignIn.isEnabled = true
                    btnLogin.text = "Iniciar sessão"
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }
            }
        }

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()
            loginViewModel.login(email, password)
        }

        btnPasskey.setOnClickListener {
            loginViewModel.loginWithPasskey(this)
        }

        btnRegisterPasskey.setOnClickListener {
            val email = editEmail.text.toString().trim()
            if (email.isEmpty()) {
                textError.text = "Por favor, introduz o email para registar a passkey."
                textError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            loginViewModel.registerPasskey(this, email)
        }

        btnGoogleSignIn.setOnClickListener {
            loginViewModel.loginWithGoogle(this)
        }
    }
}
