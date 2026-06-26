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
    private val storage by lazy { NoteStorage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadNotes()
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            override fun onMove(rv: RecyclerView, from: RecyclerView.ViewHolder, to: RecyclerView.ViewHolder): Boolean {
                val fromPos = from.adapterPosition
                val toPos = to.adapterPosition
                adapter.moveItem(fromPos, toPos)
                adapter.getNotes().forEachIndexed { i, n -> storage.updateOrder(n.id, i) }
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val n = adapter.getNotes()[pos]
                storage.delete(n)
                adapter.removeNotes(setOf(n.id))
                Toast.makeText(this@MainActivity, "已删除", Toast.LENGTH_SHORT).show()
            }
        }).attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
        if (::adapter.isInitialized) adapter.clearSelection()
    }

    private fun loadNotes() {
        val list = storage.getAll().toMutableList()
        if (!::adapter.isInitialized) {
            adapter = NoteAdapter(list, { note ->
                startActivity(Intent(this, NoteEditActivity::class.java).putExtra("note_id", note.id))
            })
            recyclerView.adapter = adapter
        } else {
            adapter.updateNotes(list)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new -> { startActivity(Intent(this, NoteEditActivity::class.java)); true }
            R.id.action_import -> { importMd(); true }
            R.id.action_export -> { exportMd(); true }
            R.id.action_delete_selected -> { batchDelete(); true }
            R.id.action_select_all -> { adapter.selectAll(); true }
            R.id.action_cancel_select -> { adapter.clearSelection(); invalidateOptionsMenu(); true }
            R.id.action_select -> { adapter.enterSelectionMode(); invalidateOptionsMenu(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val sel = ::adapter.isInitialized && adapter.selectionMode
        menu.findItem(R.id.action_delete_selected).isVisible = sel
        menu.findItem(R.id.action_select_all).isVisible = sel
        menu.findItem(R.id.action_cancel_select).isVisible = sel
        menu.findItem(R.id.action_select).isVisible = !sel
        menu.findItem(R.id.action_new).isVisible = !sel
        menu.findItem(R.id.action_import).isVisible = !sel
        menu.findItem(R.id.action_export).isVisible = !sel
        return super.onPrepareOptionsMenu(menu)
    }

    private fun batchDelete() {
        val ids = adapter.getSelectedIds()
        if (ids.isEmpty()) { Toast.makeText(this, "未选择", Toast.LENGTH_SHORT).show(); return }
        AlertDialog.Builder(this)
            .setMessage("删除 ${ids.size} 条笔记？")
            .setPositiveButton("删除") { _, _ ->
                storage.deleteByIds(ids.toList())
                adapter.removeNotes(ids)
                adapter.clearSelection()
                invalidateOptionsMenu()
                Toast.makeText(this, "已删除 ${ids.size} 条", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun exportMd() {
        val list = storage.getAll()
        if (list.isEmpty()) { Toast.makeText(this, "无笔记可导出", Toast.LENGTH_SHORT).show(); return }
        val sb = StringBuilder()
        list.forEach { n ->
            sb.appendLine(n.content); sb.appendLine(); sb.appendLine("---"); sb.appendLine()
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "text/markdown"
            putExtra(Intent.EXTRA_TITLE, "mypad_${System.currentTimeMillis()}.md")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        pendingExport = sb.toString()
        exportLauncher.launch(intent)
    }

    private var pendingExport: String? = null

    private val exportLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            try {
                contentResolver.openOutputStream(result.data!!.data!!)?.use { out ->
                    out.write((pendingExport ?: "").toByteArray())
                }
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        pendingExport = null
    }

    private fun importMd() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "text/markdown"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        importLauncher.launch(intent)
    }

    private val importLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            try {
                val text = contentResolver.openInputStream(result.data!!.data!!)?.use {
                    BufferedReader(InputStreamReader(it)).readText()
                } ?: return@registerForActivityResult
                val parts = text.split(Regex("(?m)^---\\s*$")).map { it.trim() }.filter { it.isNotBlank() }
                if (parts.isEmpty()) { Toast.makeText(this, "未解析到笔记", Toast.LENGTH_SHORT).show(); return@registerForActivityResult }
                var maxOrder = storage.getAll().maxOfOrNull { it.orderIndex } ?: -1
                parts.forEach { content -> maxOrder++; storage.insert(Note(content = content, orderIndex = maxOrder)) }
                loadNotes()
                Toast.makeText(this, "导入 ${parts.size} 条笔记", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
