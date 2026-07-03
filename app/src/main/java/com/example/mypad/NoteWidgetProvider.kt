package com.example.mypad

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class NoteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_note_list)

            val intent = Intent(context, NoteWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list, intent)
            views.setTextViewText(R.id.widget_title, "MyPad")

            val clickIntent = Intent(context, NoteWidgetProvider::class.java).apply {
                action = "com.example.mypad.OPEN_NOTE"
            }
            val flags = if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            val pi = PendingIntent.getBroadcast(context, 0, clickIntent, flags)
            views.setPendingIntentTemplate(R.id.widget_list, pi)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if ("com.example.mypad.OPEN_NOTE" == intent.action) {
            val noteId = intent.getLongExtra("note_id", -1L)
            if (noteId != -1L) {
                val editIntent = Intent(context, NoteEditActivity::class.java).apply {
                    putExtra("note_id", noteId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(editIntent)
            }
            return
        }
        super.onReceive(context, intent)
    }
}
