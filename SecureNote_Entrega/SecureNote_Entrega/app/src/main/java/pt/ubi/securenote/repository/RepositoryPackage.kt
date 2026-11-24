package pt.ubi.securenote.repositorypackage

class AuthRepository {

    fun login(email: String, password: String): Boolean {

        if (email.isBlank() || password.isBlank()) {
            return false
        }
        return true
    }
}
