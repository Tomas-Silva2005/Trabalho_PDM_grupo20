package pt.ubi.securenote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pt.ubi.securenote.data.NotesDatabase
import pt.ubi.securenote.data.NoteDao
import pt.ubi.securenote.model.Note
import org.junit.Assert.assertEquals
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: NotesDatabase
    private lateinit var noteDao: NoteDao

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

        noteDao = database.noteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertNoteTest() = runBlocking {
        val note = Note(
            title = "Teste",
            content = "Conteúdo de teste",
            date = "23/11/2026"
        )

        noteDao.insertNote(note)

        val notes = noteDao.getAllNotes().getOrAwaitValue()

        assertEquals(1, notes.size)
        assertEquals("Teste", notes[0].title)
    }

    @Test
    fun updateNoteTest() = runBlocking {
        val note = Note(
            title = "Original",
            content = "Texto original",
            date = "23/11/2026"
        )

        noteDao.insertNote(note)

        val insertedNote = noteDao.getAllNotes()
            .getOrAwaitValue()
            .first()

        val updatedNote = insertedNote.copy(
            title = "Editada",
            content = "Texto alterado"
        )

        noteDao.updateNote(updatedNote)

        val result = noteDao.getAllNotes().getOrAwaitValue()

        assertEquals("Editada", result[0].title)
        assertEquals("Texto alterado", result[0].content)
    }

    @Test
    fun deleteNoteTest() = runBlocking {
        val note = Note(
            title = "Eliminar",
            content = "Nota para eliminar",
            date = "23/11/2026"
        )

        noteDao.insertNote(note)

        val insertedNote = noteDao.getAllNotes()
            .getOrAwaitValue()
            .first()

        noteDao.deleteNote(insertedNote)

        val result = noteDao.getAllNotes().getOrAwaitValue()

        assertEquals(0, result.size)
    }
}

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {

    val latch = CountDownLatch(1)
    var data: T? = null

    val observer = Observer<T> { value ->
        data = value
        latch.countDown()
    }

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        observeForever(observer)
    }

    try {
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData não emitiu nenhum valor.")
        }
    } finally {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            removeObserver(observer)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
