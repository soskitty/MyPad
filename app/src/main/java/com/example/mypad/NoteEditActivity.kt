package com.example.mypad

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import java.text.SimpleDateFormat
import java.util.*

class NoteEditActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var noteId: Long = -1
    private val storage by lazy { NoteStorage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editText = EditText(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 16f
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.BLACK)
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
        }

        val scrollView = NestedScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setFillViewport(true)
            isNestedScrollingEnabled = true
            overScrollMode = NestedScrollView.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(editText)
        }

        setContentView(scrollView)

        noteId = intent.getLongExtra("note_id", -1L)
        if (noteId != -1L) {
            val note = storage.getAll().find { it.id == noteId }
            if (note != null) { editText.setText(note.content); editText.setSelection(note.content.length) }
        }
        setTitle(if (noteId == -1L) "新建笔记" else "编辑笔记")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.note_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_insert_date -> {
                val cursor = editText.selectionStart.coerceAtLeast(0)
                val text = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                editText.text.insert(cursor, text)
                true
            }
            R.id.action_insert_time -> {
                val cursor = editText.selectionStart.coerceAtLeast(0)
                val text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                editText.text.insert(cursor, text)
                true
            }
            R.id.action_save -> {
                saveAndFinish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveAndFinish() {
        val content = editText.text.toString()
        if (content.isBlank() && noteId == -1L) { finish(); return }
        if (noteId == -1L) {
            val maxOrder = storage.getAll().maxOfOrNull { it.orderIndex } ?: -1
            storage.insert(Note(content = content, orderIndex = maxOrder + 1))
        } else {
            storage.updateContent(noteId, content)
        }
        finish()
    }

    override fun onBackPressed() = saveAndFinish()
}
