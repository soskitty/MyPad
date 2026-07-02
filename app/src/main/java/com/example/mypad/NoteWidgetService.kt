package com.example.mypad

import android.content.Intent
import android.widget.RemoteViewsService

class NoteWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return NoteWidgetFactory(applicationContext)
    }
}
