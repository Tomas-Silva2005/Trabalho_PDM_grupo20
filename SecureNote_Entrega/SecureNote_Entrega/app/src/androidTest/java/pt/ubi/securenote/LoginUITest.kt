package pt.ubi.securenote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.core.app.ActivityScenario
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITest {

    // TESTE 1: Verificar elementos do ecrã de login
    @Test
    fun loginScreenElementsTest() {

        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.editEmail))
            .check(matches(isDisplayed()))

        onView(withId(R.id.editPassword))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    // TESTE 2: Login com credenciais válidas
    @Test
    fun successfulLoginTest() {

        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Introduzir email
        onView(withId(R.id.editEmail))
            .perform(typeText("demo@securenote.pt"))

        // Introduzir password
        onView(withId(R.id.editPassword))
            .perform(typeText("Secure123"), closeSoftKeyboard())

        // Clicar no botão de login
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Verificar se entrámos no ecrã das notas
        onView(withText("As tuas notas"))
            .check(matches(isDisplayed()))

        // Fechar o teste
        scenario.close()
    }

    // TESTE 3: Login com credenciais inválidas
    @Test
    fun failedLoginTest() {

        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Introduzir email errado
        onView(withId(R.id.editEmail))
            .perform(typeText("errado@email.com"))

        // Introduzir password errada
        onView(withId(R.id.editPassword))
            .perform(typeText("PasswordErrada123"), closeSoftKeyboard())

        // Clicar no botão de login
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Verificar se a mensagem de erro aparece
        onView(withId(R.id.textError))
            .check(matches(isDisplayed()))

        // Fechar o teste
        scenario.close()
    }
}