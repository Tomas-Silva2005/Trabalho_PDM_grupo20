package pt.ubi.securenote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import pt.ubi.securenote.data.NotesDatabase
import pt.ubi.securenote.repository.NotesRepository

@RunWith(AndroidJUnit4::class)
class NotesRepositoryTest {

    private lateinit var database: NotesDatabase
    private lateinit var repository: NotesRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            NotesDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = NotesRepository(database.noteDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun addNoteTest() = runBlocking {
        val title = "Nota secreta"
        val content = "Conteúdo privado"

        repository.addNote(title, content)

        // Verificar dados guardados na base de dados
        val storedNotes = database.noteDao()
            .getAllNotes()
            .getOrAwaitValue()

        assertEquals(1, storedNotes.size)
        assertTrue(
            storedNotes[0].title.startsWith("SECURENOTE_ENC_V1:")
        )
        assertTrue(
            storedNotes[0].content.startsWith("SECURENOTE_ENC_V1:")
        )

        // Verificar se o Repository devolve os dados legíveis
        val readableNotes = repository
            .getAllNotes()
            .getOrAwaitValue()

        assertEquals(title, readableNotes[0].title)
        assertEquals(content, readableNotes[0].content)
    }

    @Test
    fun updateNoteTest() = runBlocking {
        repository.addNote(
            "Título original",
            "Conteúdo original"
        )

        val note = repository.getAllNotes()
            .getOrAwaitValue()
            .first()

        val updatedNote = note.copy(
            title = "Título alterado",
            content = "Conteúdo alterado"
        )

        repository.updateNote(updatedNote)

        val readableNotes = repository
            .getAllNotes()
            .getOrAwaitValue()

        assertEquals(
            "Título alterado",
            readableNotes[0].title
        )
        assertEquals(
            "Conteúdo alterado",
            readableNotes[0].content
        )

        // Confirmar que os dados continuam encriptados na BD
        val storedNotes = database.noteDao()
            .getAllNotes()
            .getOrAwaitValue()

        assertTrue(
            storedNotes[0].title.startsWith("SECURENOTE_ENC_V1:")
        )
        assertTrue(
            storedNotes[0].content.startsWith("SECURENOTE_ENC_V1:")
        )
    }

    @Test
    fun deleteNoteTest() = runBlocking {
        repository.addNote(
            "Nota para eliminar",
            "Conteúdo a eliminar"
        )

        val note = repository.getAllNotes()
            .getOrAwaitValue()
            .first()

        repository.deleteNote(note)

        val result = repository.getAllNotes()
            .getOrAwaitValue()

        assertEquals(0, result.size)
    }
}

