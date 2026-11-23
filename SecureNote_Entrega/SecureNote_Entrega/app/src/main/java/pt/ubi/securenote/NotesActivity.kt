package pt.ubi.securenote

import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import pt.ubi.securenote.model.Note
import pt.ubi.securenote.ui.notes.NotesViewModel

class NotesActivity : AppCompatActivity() {

    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notesContainer: LinearLayout
    private lateinit var emptyNotes: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val btnAddNote = findViewById<Button>(R.id.btnAddNote)
        notesContainer = findViewById(R.id.notesContainer)
        emptyNotes = findViewById(R.id.emptyNotes)

        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        notesViewModel.notes.observe(this) { notes ->
            displayNotes(notes)
        }

        btnAddNote.setOnClickListener {
            showCreateNoteDialog()
        }
    }

    // Criar nota
    private fun showCreateNoteDialog() {

        val layout = createDialogLayout()

        val titleInput = EditText(this).apply {
            hint = "Título da nota"
            setSingleLine(true)
        }

        val contentInput = EditText(this).apply {
            hint = "Escreve a tua nota aqui..."
            minLines = 4
            gravity = Gravity.TOP
        }

        layout.addView(titleInput)
        layout.addView(contentInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nova nota")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title = titleInput.text.toString().trim()
                    val content = contentInput.text.toString().trim()

                    if (title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Preenche o título e o conteúdo da nota.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        notesViewModel.addNote(title, content)
                        dialog.dismiss()
                    }
                }
        }

        dialog.show()
    }

    // Editar nota
    private fun showEditNoteDialog(note: Note) {

        val layout = createDialogLayout()

        val titleInput = EditText(this).apply {
            hint = "Título da nota"
            setSingleLine(true)
            setText(note.title)
        }

        val contentInput = EditText(this).apply {
            hint = "Conteúdo da nota"
            minLines = 4
            gravity = Gravity.TOP
            setText(note.content)
        }

        layout.addView(titleInput)
        layout.addView(contentInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar nota")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar alterações", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val newTitle = titleInput.text.toString().trim()
                    val newContent = contentInput.text.toString().trim()

                    if (newTitle.isEmpty() || newContent.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Preenche o título e o conteúdo da nota.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {

                        val updatedNote = note.copy(
                            title = newTitle,
                            content = newContent
                        )

                        notesViewModel.updateNote(updatedNote)
                        dialog.dismiss()

                        Toast.makeText(
                            this,
                            "Nota atualizada!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialog.show()
    }

    // Confirmar eliminação
    private fun showDeleteConfirmation(note: Note) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar nota")
            .setMessage("Tens a certeza de que queres eliminar \"${note.title}\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->

                notesViewModel.deleteNote(note)

                Toast.makeText(
                    this,
                    "Nota eliminada!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    // Layout dos diálogos
    private fun createDialogLayout(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
        }
    }

    // Mostrar notas
    private fun displayNotes(notes: List<Note>) {

        notesContainer.removeAllViews()

        emptyNotes.visibility =
            if (notes.isEmpty()) View.VISIBLE
            else View.GONE

        for (note in notes) {

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(16), dp(18), dp(16))
                background = roundedBackground(Color.WHITE, dp(16))
            }

            // Título
            val title = TextView(this).apply {
                text = note.title
                textSize = 18f
                setTextColor(Color.rgb(23, 35, 60))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            // Conteúdo
            val content = TextView(this).apply {
                text = note.content
                textSize = 15f
                setTextColor(Color.rgb(67, 85, 120))
                setPadding(0, dp(10), 0, dp(10))
            }

            // Data
            val date = TextView(this).apply {
                text = note.date
                textSize = 12f
                setTextColor(Color.rgb(120, 130, 150))
            }

            // Botões
            val buttonsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
                setPadding(0, dp(12), 0, 0)
            }

            val btnEdit = Button(this).apply {
                text = "Editar"
                textSize = 12f
                setTextColor(Color.rgb(35, 90, 180))
            }

            val btnDelete = Button(this).apply {
                text = "Eliminar"
                textSize = 12f
                setTextColor(Color.rgb(200, 55, 55))
            }

            btnEdit.setOnClickListener {
                showEditNoteDialog(note)
            }

            btnDelete.setOnClickListener {
                showDeleteConfirmation(note)
            }

            buttonsLayout.addView(btnEdit)
            buttonsLayout.addView(btnDelete)

            card.addView(title)
            card.addView(content)
            card.addView(date)
            card.addView(buttonsLayout)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            notesContainer.addView(card, params)
        }
    }

    // Fundo dos cartões
    private fun roundedBackground(color: Int, radius: Int): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    // Converter dp
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}