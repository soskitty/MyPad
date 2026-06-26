package com.example.mypad

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onItemClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    var selectionMode = false
    private val selectedIds = mutableSetOf<Long>()

    fun getSelectedIds(): Set<Long> = selectedIds.toSet()

    fun selectAll() {
        selectedIds.clear()
        if (selectionMode) selectedIds.addAll(notes.map { it.id })
        notifyDataSetChanged()
    }

    fun enterSelectionMode() {
        selectionMode = true
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedIds.clear()
        selectionMode = false
        notifyDataSetChanged()
    }

    fun removeNotes(ids: Set<Long>) {
        notes.removeAll { it.id in ids }
        selectedIds.removeAll(ids)
        notifyDataSetChanged()
    }

    fun updateNotes(newNotes: MutableList<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    fun moveItem(fromPos: Int, toPos: Int) {
        notes.removeAt(fromPos).also { notes.add(toPos, it) }
        notifyItemMoved(fromPos, toPos)
    }

    fun getNotes(): MutableList<Note> = notes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.date.text = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            .format(Date(note.updatedAt))
        holder.checkBox.visibility = if (selectionMode) android.view.View.VISIBLE else android.view.View.GONE
        holder.checkBox.isChecked = note.id in selectedIds
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedIds.add(note.id) else selectedIds.remove(note.id)
        }
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                holder.checkBox.toggle()
            } else {
                onItemClick(note)
            }
        }
        holder.itemView.setOnLongClickListener(null)
    }

    override fun getItemCount() = notes.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title)
        val date: TextView = view.findViewById(R.id.tv_date)
        val checkBox: CheckBox = view.findViewById(R.id.cb_select)
    }
}
