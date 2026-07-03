package com.example.mypad

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class NoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val notes = NoteStorage(context).getAll().take(6)
        val text = if (notes.isEmpty()) "无笔记" else notes.joinToString("\n") { it.title }

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_note_list)
            views.setTextViewText(R.id.widget_text, text)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
