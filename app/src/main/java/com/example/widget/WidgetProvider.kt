package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.net.toUri


class WidgetProvider : AppWidgetProvider() {
    private val data = Data()
    private var fileContent = mutableMapOf<String, String>()

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {

        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        //MANUAL UPDATE FUNCTION
        if (intent?.action == ACTION_MANUAL_UPDATE) {
            val uri = data.load(context, "selected_folder_uri")?.toUri()
            if (uri != null)
            {
                fileContent = data.getFileContents(context!!, uri)
                mergeNotes(context)
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context!!, WidgetProvider::class.java)
            )
            updateWidget(context, appWidgetManager, appWidgetIds)
        }

        //CHECKBOX STATUS CHANGE FUNCTION
        if (intent?.action == STATUS_CHANGED) {
            if (intent.getStringExtra(CURRENT_STATUS) != ""){
                val uri = data.load(context, "selected_folder_uri")?.toUri()
                if(uri != null && context != null)
                {
                    fileContent = data.getFileContents(context, uri)
                }
                val task = intent.getStringExtra(CURRENT_STATUS)
                val fileName = setCheckbox(context, task.toString().trim())
                if(uri != null && context != null && fileName != "")
                {
                    data.writeFile(context, uri, fileName, fileContent[fileName].toString())
                }
                if (uri != null)
                {
                    mergeNotes(context)
                }
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context!!, WidgetProvider::class.java)
                )
                updateWidget(context, appWidgetManager, appWidgetIds)

            }
        }

        // DELETE FUNCTION
        if (intent?.action == DELETE_TASKS) {
            val uri = data.load(context, "selected_folder_uri")?.toUri()
            if(uri != null && context != null)
            {
                fileContent = data.getFileContents(context, uri)
                deleteCheckedTasks(context, uri)
                mergeNotes(context)
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context!!, WidgetProvider::class.java)
            )
            updateWidget(context, appWidgetManager, appWidgetIds)
        }


        if (intent?.action == WIDGET_CLICKED) {
            try {
                context?.packageManager?.getPackageInfo("com.example.obsidian", 0)
                val launchIntent = context?.packageManager?.getLaunchIntentForPackage("com.example.obsidian")
                context?.startActivity(launchIntent)
            }
            catch (nameNotFoundException: Exception)
            {
                println("Package not found")
            }
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

            val deleteIntent = Intent(context, WidgetProvider::class.java).apply {
                action = DELETE_TASKS
            }

            val deletePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                deleteIntent,
                PendingIntent.FLAG_MUTABLE
            )

            views.setOnClickPendingIntent(R.id.delete_button, deletePendingIntent)

            val refreshIntent = Intent(context, WidgetProvider::class.java).apply {
                action = ACTION_MANUAL_UPDATE
            }

            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                2,
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

    private fun mergeNotes(context: Context?) {
        val currentOrder = data.load(context, "current_order").toString().removePrefix("[").removeSuffix("]").split(",")
        var content = ""
        currentOrder.forEach {
            content += "#" + it.trim() + "\n"
            content += fileContent[it.trim()]
            content += "\nend \n"
        }
        data.save(context, "current_content", content)
    }

    private fun setCheckbox(context: Context?, task: String): String
    {
        var fileName = ""
        val currentOrder = data.load(context, "current_order").toString().removePrefix("[").removeSuffix("]").split(",")
        currentOrder.forEach {
            var newContent = ""
            val lines = fileContent[it.trim()].toString().split("\n")
            for (line in lines)
            {
                when (line) {
                    ("- [ ] $task") -> {
                        newContent += line.replace("- [ ] $task", "- [x] $task") + "\n"
                        fileName = it.trim()
                    }
                    ("- [x] $task") -> {
                        newContent += line.replace("- [x] $task", "- [ ] $task") + "\n"
                        fileName = it.trim()
                    }
                    else -> {
                        newContent += line + "\n"
                    }
                }
            }
            fileContent[it.trim()] = newContent
        }
        return fileName
    }

    private fun deleteCheckedTasks(context: Context?, uri: Uri)
    {
        val currentOrder = data.load(context, "current_order").toString().removePrefix("[").removeSuffix("]").split(",")
        currentOrder.forEach {
            if (fileContent[it.trim()].toString().contains("- [x]")) {
                var newContent = ""
                val lines = fileContent[it.trim()].toString().split("\n")
                for (i in lines.indices)
                {
                    if (!lines[i].contains("- [x]") && lines[i] != "")
                    {
                        newContent += lines[i] + "\n"
                    }
                }
                fileContent[it.trim()] = newContent
                if(context != null)
                {
                    data.writeFile(context, uri, it.trim(), fileContent[it.trim()].toString())
                }
            }
        }
    }
}


