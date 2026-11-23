package pt.ubi.securenote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(NotesActivity::class.java)

    @Test
    fun notesScreenElementsTest() {

        // Verificar se o botão de criar nota está visível
        onView(withId(R.id.btnAddNote))
            .check(matches(isDisplayed()))

        // Verificar se a mensagem de notas vazias está visível
        onView(withId(R.id.emptyNotes))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addNoteButtonTest() {

        // Verificar se o botão está visível
        onView(withId(R.id.btnAddNote))
            .check(matches(isDisplayed()))

        // Clicar no botão para criar uma nova nota
        onView(withId(R.id.btnAddNote))
            .perform(click())
    }
}
