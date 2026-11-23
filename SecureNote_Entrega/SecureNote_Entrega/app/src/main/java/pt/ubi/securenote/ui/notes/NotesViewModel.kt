package pt.ubi.securenote.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import pt.ubi.securenote.data.NotesDatabase
import pt.ubi.securenote.model.Note
import pt.ubi.securenote.repository.NotesRepository
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NotesDatabase.getDatabase(application)
    private val repository = NotesRepository(database.noteDao())

    val notes: LiveData<List<Note>> = repository.getAllNotes()

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.addNote(title, content)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}