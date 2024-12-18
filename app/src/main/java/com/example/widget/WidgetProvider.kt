package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.view.drawToBitmap


class WidgetProvider : AppWidgetProvider() {
    private val data = Data()

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {

        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == ACTION_MANUAL_UPDATE) {
            println("refresh")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context!!, WidgetProvider::class.java)
            )
            updateWidget(context, appWidgetManager, appWidgetIds)
        }

        if (intent?.action == STATUS_CHANGED) {
            if (intent.getIntExtra(CURRENT_STATUS, -1) != -1){
                println("Checkbox clicked")
                println("onReceive: ${intent.getIntExtra(CURRENT_STATUS, -1)}")
////                val launchIntent = context?.packageManager?.getLaunchIntentForPackage("com.example.obsidian")
////                context?.startActivity(launchIntent)
            }
        }

        if (intent?.action == WIDGET_CLICKED) {
            println("Widget clicked")
            val extra = intent.getIntExtra(CURRENT_STATUS, -1)
            println("onReceive: $extra")
        }

        super.onReceive(context, intent)
    }

    private fun updateWidget(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        var backgroundArgb = mutableListOf<String>()
        val content = data.load(context, "background_argb").toString().split(",").toMutableList()
        if (content.size == 4)
        {
            backgroundArgb = content
        }
        else
        {
            backgroundArgb += "30"
            backgroundArgb += "0"
            backgroundArgb += "0"
            backgroundArgb += "0"
        }
        appWidgetIds?.forEach { appWidgetId ->

            val intent = Intent(context, WidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val views = RemoteViews(context?.packageName, R.layout.tasks_layout).apply {
                setRemoteAdapter(R.id.task_list, intent)
                setEmptyView(R.id.task_list, R.id.empty_view)
            }

            val clickIntent = Intent(context, WidgetProvider::class.java)

            val clickPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_MUTABLE
            )

            val refreshIntent = Intent(context, WidgetProvider::class.java).apply {
                action = ACTION_MANUAL_UPDATE
            }

            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_MUTABLE
            )

            views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

            views.setPendingIntentTemplate(R.id.task_list, clickPendingIntent)
            views.setInt(R.id.widget_background, "setBackgroundColor", Color.argb(backgroundArgb[0].toInt(), backgroundArgb[1].toInt(), backgroundArgb[2].toInt(), backgroundArgb[3].toInt()))

            appWidgetManager?.notifyAppWidgetViewDataChanged(appWidgetId, R.id.task_list)
            appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }
}


