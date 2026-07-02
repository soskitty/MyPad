package com.example.mypad

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.text.SimpleDateFormat
import java.util.*

class NoteWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var notes = emptyList<Note>()

    override fun onCreate() { loadNotes() }

    override fun onDataSetChanged() { loadNotes() }

    override fun onDestroy() {}

    override fun getCount() = notes.size

    override fun getViewAt(position: Int): RemoteViews {
        val note = notes[position]
        val views = RemoteViews(context.packageName, R.layout.widget_note_item)
        views.setTextViewText(R.id.widget_item_title, note.title)
        views.setTextViewText(R.id.widget_item_date, SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(note.updatedAt)))

        val fillIntent = Intent().apply {
            putExtra("note_id", note.id)
        }
        val flags = if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pi = PendingIntent.getBroadcast(context, note.id.toInt(), fillIntent, flags)
        views.setOnClickFillInIntent(R.id.widget_item_title, fillIntent)
        return views
    }

    override fun getLoadingView() = null

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = notes[position].id

    override fun hasStableIds() = true

    private fun loadNotes() {
        notes = NoteStorage(context).getAll()
    }
}
