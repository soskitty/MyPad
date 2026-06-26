package com.example.mypad

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NoteEditActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private var noteId: Long = -1
    private val noteDao by lazy { AppDatabase.get(this).noteDao() }

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
            val note = noteDao.getAll().find { it.id == noteId }
            if (note != null) {
                editText.setText(note.content)
                editText.setSelection(note.content.length)
            }
        }
        setTitle(if (noteId == -1L) "新建笔记" else "编辑笔记")
    }

    override fun onBackPressed() {
        save()
        super.onBackPressed()
    }

    private fun save() {
        val content = editText.text.toString()
        if (content.isBlank() && noteId == -1L) return
        if (noteId == -1L) {
            val dao = noteDao
            val maxOrder = dao.getAll().maxOfOrNull { it.orderIndex } ?: -1
            dao.insert(Note(content = content, orderIndex = maxOrder + 1))
        } else {
            noteDao.updateContent(noteId, content)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_PASTE) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).coerceToText(this)
                val plain = stripHtml(text.toString())
                val current = editText.text
                val start = maxOf(editText.selectionStart, 0)
                val end = maxOf(editText.selectionEnd, 0)
                if (start != end) {
                    current.replace(minOf(start, end), maxOf(start, end), plain)
                } else {
                    current.insert(start, plain)
                }
            }
        }
    }

    companion object {
        fun stripHtml(html: String): String {
            val spanned: Spanned = if (android.os.Build.VERSION.SDK_INT >= 24) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(html)
            }
            return spanned.toString()
        }
    }
}
