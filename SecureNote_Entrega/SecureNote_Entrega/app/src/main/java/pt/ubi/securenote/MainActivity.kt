package pt.ubi.securenote

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import pt.ubi.securenote.ui.login.LoginState
import pt.ubi.securenote.ui.login.LoginViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referências aos elementos da interface
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val textError = findViewById<TextView>(R.id.textError)

        // Criar o ViewModel
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Observar as alterações do estado do login
        loginViewModel.loginState.observe(this) { state ->

            when (state) {

                is LoginState.Idle -> {
                    btnLogin.isEnabled = true
                    textError.visibility = View.GONE
                }

                is LoginState.Loading -> {
                    btnLogin.isEnabled = false
                    btnLogin.text = "A validar..."
                    textError.visibility = View.GONE
                }

                is LoginState.Success -> {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Iniciar sessão"
                    textError.visibility = View.GONE

                    // Abrir o ecrã das notas
                    val intent = Intent(this, NotesActivity::class.java)
                    startActivity(intent)

                    // Fechar o ecrã de login
                    finish()
                }

                is LoginState.Error -> {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Iniciar sessão"
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }
            }
        }

        // Ação do botão de login
        btnLogin.setOnClickListener {

            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()

            loginViewModel.login(email, password)
        }
    }
}
