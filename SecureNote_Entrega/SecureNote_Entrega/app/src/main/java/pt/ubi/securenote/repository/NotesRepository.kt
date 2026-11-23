package pt.ubi.securenote.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import pt.ubi.securenote.data.NoteDao
import pt.ubi.securenote.model.Note
import pt.ubi.securenote.security.CryptoManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesRepository(
    private val noteDao: NoteDao
) {

    private val cryptoManager = CryptoManager()

    // Obter notas e desencriptar para mostrar no ecrã
    fun getAllNotes(): LiveData<List<Note>> {

        val result = MediatorLiveData<List<Note>>()
        val source: LiveData<List<Note>> = noteDao.getAllNotes()

        result.addSource(source) { encryptedNotes: List<Note> ->

            val decryptedNotes = encryptedNotes.map { note: Note ->
                note.copy(
                    title = cryptoManager.decrypt(note.title),
                    content = cryptoManager.decrypt(note.content)
                )
            }

            result.value = decryptedNotes
        }

        return result
    }

    // Criar nota
    suspend fun addNote(title: String, content: String) {

        val date = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())

        val encryptedNote = Note(
            title = cryptoManager.encrypt(title),
            content = cryptoManager.encrypt(content),
            date = date
        )

        noteDao.insertNote(encryptedNote)
    }

    // Atualizar nota
    suspend fun updateNote(note: Note) {

        val encryptedNote = note.copy(
            title = cryptoManager.encrypt(note.title),
            content = cryptoManager.encrypt(note.content)
        )

        noteDao.updateNote(encryptedNote)
    }

    // Eliminar nota
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
}