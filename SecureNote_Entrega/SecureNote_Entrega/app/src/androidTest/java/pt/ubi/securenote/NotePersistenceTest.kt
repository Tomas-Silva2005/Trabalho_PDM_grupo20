
package pt.ubi.securenote

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ubi.securenote.data.NotesDatabase
import pt.ubi.securenote.model.Note
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NotePersistenceTest {

    @Test
    fun notesPersistAfterDatabaseReopenTest() = runBlocking {

        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "persistence_test_database"

        context.deleteDatabase(databaseName)

        // Criar a primeira base de dados
        val firstDatabase = Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            databaseName
        ).allowMainThreadQueries().build()

        try {
            val note = Note(
                title = "Nota persistente",
                content = "Esta nota deve continuar guardada.",
                date = "23/11/2026"
            )

            // Guardar a nota
            firstDatabase.noteDao().insertNote(note)

        } finally {
            firstDatabase.close()
        }

        // Reabrir a base de dados
        val secondDatabase = Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            databaseName
        ).allowMainThreadQueries().build()

        try {
            // Consultar as notas guardadas
            val notes = secondDatabase.noteDao()
                .getAllNotes()
                .getOrAwaitValue()

            // Verificar persistência
            assertEquals(1, notes.size)
            assertEquals("Nota persistente", notes[0].title)
            assertEquals(
                "Esta nota deve continuar guardada.",
                notes[0].content
            )

        } finally {
            secondDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun <T> LiveData<T>.getOrAwaitValue(): T {

        val latch = CountDownLatch(1)
        var result: T? = null

        val observer = Observer<T> { value ->
            result = value
            latch.countDown()
        }

        // Observar na thread principal
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync {
                observeForever(observer)
            }

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw AssertionError(
                    "O LiveData não recebeu dados a tempo."
                )
            }
        } finally {
            // Remover observer na thread principal
            InstrumentationRegistry.getInstrumentation()
                .runOnMainSync {
                    removeObserver(observer)
                }
        }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}
