package com.example.mypad

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class NoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val notes = NoteStorage(context).getAll().take(6)

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_note_list)
            views.setTextViewText(R.id.widget_title, "MyPad")

            val lines = intArrayOf(R.id.line_1, R.id.line_2, R.id.line_3, R.id.line_4, R.id.line_5, R.id.line_6)
            val divs = intArrayOf(R.id.div_1, R.id.div_2, R.id.div_3, R.id.div_4, R.id.div_5)

            for (i in 0 until 6) {
                if (i < notes.size) {
                    views.setTextViewText(lines[i], notes[i].title)
                    views.setViewVisibility(lines[i], android.view.View.VISIBLE)
                    if (i < 5) views.setViewVisibility(divs[i], android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(lines[i], android.view.View.GONE)
                    if (i < 5) views.setViewVisibility(divs[i], android.view.View.GONE)
                }
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
