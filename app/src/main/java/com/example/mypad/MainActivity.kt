package com.example.mypad

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private val noteDao by lazy { AppDatabase.get(this).noteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadNotes()

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            override fun onMove(rv: RecyclerView, from: RecyclerView.ViewHolder, to: RecyclerView.ViewHolder): Boolean {
                val notes = adapter.getNotes()
                val fromPos = from.adapterPosition
                val toPos = to.adapterPosition
                val moved = notes.removeAt(fromPos).also { notes.add(toPos, it) }
                notes.forEachIndexed { i, note -> noteDao.updateOrder(note.id, i) }
                adapter.updateNotes(notes)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val note = adapter.getNotes()[pos]
                noteDao.delete(note)
                adapter.removeNotes(setOf(note.id))
                Toast.makeText(this@MainActivity, "已删除", Toast.LENGTH_SHORT).show()
            }
        })
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
        if (::adapter.isInitialized) adapter.clearSelection()
    }

    private fun loadNotes() {
        val notes = noteDao.getAll().toMutableList()
        if (!::adapter.isInitialized) {
            adapter = NoteAdapter(notes, { note ->
                val intent = Intent(this, NoteEditActivity::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            })
            recyclerView.adapter = adapter
        } else {
            adapter.updateNotes(notes)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new -> {
                startActivity(Intent(this, NoteEditActivity::class.java))
                true
            }
            R.id.action_import -> { importMd(); true }
            R.id.action_export -> { exportMd(); true }
            R.id.action_delete_selected -> { batchDelete(); true }
            R.id.action_select_all -> { adapter.selectAll(); true }
            R.id.action_cancel_select -> { adapter.clearSelection(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_delete_selected).isVisible = ::adapter.isInitialized && adapter.selectionMode
        menu.findItem(R.id.action_select_all).isVisible = ::adapter.isInitialized && adapter.selectionMode
        menu.findItem(R.id.action_cancel_select).isVisible = ::adapter.isInitialized && adapter.selectionMode
        menu.findItem(R.id.action_new).isVisible = !(::adapter.isInitialized && adapter.selectionMode)
        menu.findItem(R.id.action_import).isVisible = !(::adapter.isInitialized && adapter.selectionMode)
        menu.findItem(R.id.action_export).isVisible = !(::adapter.isInitialized && adapter.selectionMode)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun batchDelete() {
        val ids = adapter.getSelectedIds()
        if (ids.isEmpty()) { Toast.makeText(this, "未选择", Toast.LENGTH_SHORT).show(); return }
        AlertDialog.Builder(this)
            .setMessage("删除 ${ids.size} 条笔记？")
            .setPositiveButton("删除") { _, _ ->
                noteDao.deleteByIds(ids.toList())
                adapter.removeNotes(ids)
                adapter.clearSelection()
                invalidateOptionsMenu()
                Toast.makeText(this, "已删除 ${ids.size} 条", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private val importExportFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private fun exportMd() {
        val notes = noteDao.getAll()
        if (notes.isEmpty()) { Toast.makeText(this, "无笔记可导出", Toast.LENGTH_SHORT).show(); return }
        val sb = StringBuilder()
        notes.forEach { note ->
            sb.appendLine("# ${note.title}")
            sb.appendLine()
            sb.appendLine(note.content)
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(Intent.EXTRA_TITLE, "mypad_${System.currentTimeMillis()}.md")
            flags = importExportFlags
        }
        exportLauncher.launch(intent)
        pendingExportContent = sb.toString()
    }

    private var pendingExportContent: String? = null

    private val exportLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            val uri = result.data!!.data!!
            try {
                contentResolver.openOutputStream(uri)?.use { out ->
                    out.write((pendingExportContent ?: "").toByteArray())
                }
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        pendingExportContent = null
    }

    private fun importMd() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            flags = importExportFlags
        }
        importLauncher.launch(intent)
    }

    private val importLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            val uri = result.data!!.data!!
            try {
                val text = contentResolver.openInputStream(uri)?.use { input ->
                    BufferedReader(InputStreamReader(input)).readText()
                } ?: return@registerForActivityResult
                val p = parseMarkdown(text)
                if (p.isEmpty()) {
                    Toast.makeText(this, "未解析到笔记", Toast.LENGTH_SHORT).show(); return@registerForActivityResult
                }
                var maxOrder = noteDao.getAll().maxOfOrNull { it.orderIndex } ?: -1
                p.forEach { content ->
                    maxOrder++
                    noteDao.insert(Note(content = content, orderIndex = maxOrder))
                }
                loadNotes()
                Toast.makeText(this, "导入 ${p.size} 条笔记", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun parseMarkdown(text: String): List<String> {
        val notes = mutableListOf<String>()
        text.split(Regex("(?m)^---\\s*$")).forEach { part ->
            val trimmed = part.trim()
            if (trimmed.isNotBlank()) notes.add(trimmed)
        }
        return notes
    }
}
