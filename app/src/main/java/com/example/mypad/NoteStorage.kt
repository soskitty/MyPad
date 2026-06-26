package com.example.mypad

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class NoteStorage(context: Context) {

    private val file = File(context.filesDir, "notes.json")

    fun getAll(): List<Note> {
        if (!file.exists()) return emptyList()
        val text = file.readText()
        val arr = JSONArray(text)
        val list = mutableListOf<Note>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(Note(
                id = obj.getLong("id"),
                content = obj.getString("content"),
                orderIndex = obj.getInt("orderIndex"),
                createdAt = obj.getLong("createdAt"),
                updatedAt = obj.getLong("updatedAt")
            ))
        }
        return list.sortedBy { it.orderIndex }
    }

    fun insert(note: Note) {
        val list = getAll().toMutableList()
        list.add(note)
        saveAll(list)
    }

    fun updateContent(id: Long, content: String) {
        val list = getAll().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(content = content, updatedAt = System.currentTimeMillis())
            saveAll(list)
        }
    }

    fun updateOrder(id: Long, orderIndex: Int) {
        val list = getAll().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(orderIndex = orderIndex)
            saveAll(list)
        }
    }

    fun delete(note: Note) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == note.id }
        saveAll(list)
    }

    fun deleteByIds(ids: List<Long>) {
        val list = getAll().toMutableList()
        list.removeAll { it.id in ids }
        saveAll(list)
    }

    private fun saveAll(notes: List<Note>) {
        val arr = JSONArray()
        notes.forEach { note ->
            arr.put(JSONObject().apply {
                put("id", note.id)
                put("content", note.content)
                put("orderIndex", note.orderIndex)
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
            })
        }
        file.writeText(arr.toString())
    }
}
