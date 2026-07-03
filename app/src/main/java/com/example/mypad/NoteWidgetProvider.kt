package com.example.mypad

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class NoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val notes = NoteStorage(context).getAll().take(4)
        val flags = if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_note_list)
            views.setTextViewText(R.id.widget_title, "MyPad")

            val slotIds = intArrayOf(R.id.slot_1, R.id.slot_2, R.id.slot_3, R.id.slot_4)
            for (i in 0 until 4) {
                if (i < notes.size) {
                    val note = notes[i]
                    views.setTextViewText(slotIds[i], note.title)
                    views.setViewVisibility(slotIds[i], android.view.View.VISIBLE)

                    val tapIntent = Intent(context, NoteEditActivity::class.java)
                    tapIntent.putExtra("note_id", note.id)
                    tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val pi = PendingIntent.getActivity(context, note.id.toInt(), tapIntent, flags)
                    views.setOnClickPendingIntent(slotIds[i], pi)
                } else {
                    views.setViewVisibility(slotIds[i], android.view.View.GONE)
                }
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
