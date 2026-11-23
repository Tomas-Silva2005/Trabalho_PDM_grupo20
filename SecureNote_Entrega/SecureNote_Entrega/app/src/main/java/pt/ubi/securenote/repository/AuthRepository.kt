package pt.ubi.securenote.repository

class AuthRepository {

    fun login(email: String, password: String): Boolean {

        if (email.isBlank() || password.isBlank()) {
            return false
        }

        return email == "demo@securenote.pt" &&
                password == "Secure123"
    }
}