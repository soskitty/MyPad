package com.example.mypad

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NoteEditActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var noteId: Long = -1
    private val storage by lazy { NoteStorage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editText = EditText(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            textSize = 16f
            setPadding(32, 32, 32, 32)
            setBackgroundColor(android.graphics.Color.WHITE)
            setTextColor(android.graphics.Color.BLACK)
        }
        setContentView(editText)
        noteId = intent.getLongExtra("note_id", -1L)
        if (noteId != -1L) {
            val note = storage.getAll().find { it.id == noteId }
            if (note != null) { editText.setText(note.content); editText.setSelection(note.content.length) }
        }
        setTitle(if (noteId == -1L) "新建笔记" else "编辑笔记")
    }

    override fun onBackPressed() {
        val content = editText.text.toString()
        if (content.isBlank() && noteId == -1L) { super.onBackPressed(); return }
        if (noteId == -1L) {
            val maxOrder = storage.getAll().maxOfOrNull { it.orderIndex } ?: -1
            storage.insert(Note(content = content, orderIndex = maxOrder + 1))
        } else {
            storage.updateContent(noteId, content)
        }
        super.onBackPressed()
    }
}
