package com.example.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.widget.RemoteViews
import android.widget.RemoteViewsService


const val STATUS_CHANGED = "com.example.widget.STATE_CHANGED"
const val WIDGET_CLICKED = "com.example.widget.WIDGET_CLICKED"
const val CURRENT_STATUS = "com.example.widget.CURRENT_STATUS"
const val ACTION_MANUAL_UPDATE = "com.example.widget.ACTION_MANUAL_UPDATE"

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetRemoteViewsFactory(this.applicationContext)
    }
}

class WidgetRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private val tasks = mutableListOf<MutableMap<String, Any?>>()
    private var textArgb = mutableListOf<String>()
    private val data = Data()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        tasks.clear()
        tasks.addAll(fetchNoteLists())
        val content = data.load(context, "text_argb").toString().split(",").toMutableList()
        if (content.size == 4)
        {
            textArgb = content
        }
        else
        {
            textArgb += "80"
            textArgb += "255"
            textArgb += "255"
            textArgb += "255"
        }
    }

    override fun onDestroy() {
        tasks.clear()
    }

    override fun getCount(): Int {
        return tasks.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.single_task_layout)
        val task = tasks[position]

        remoteViews.setCompoundButtonChecked(R.id.checkbox, task["checked"] as Boolean)
        remoteViews.setTextViewText(R.id.task_content, task["text"] as String)
        remoteViews.setTextViewTextSize(R.id.task_content, COMPLEX_UNIT_SP, task["heading"] as Float)
        remoteViews.setTextColor(R.id.task_content, Color.argb(textArgb[0].toInt(), textArgb[1].toInt(), textArgb[2].toInt(), textArgb[3].toInt()))
        remoteViews.setViewVisibility(R.id.checkbox, task["checkbox"] as Int)
        remoteViews.setViewVisibility(R.id.divider, task["end"] as Int)

        val fillInIntent = Intent(context, WidgetProvider::class.java).apply {
            action = STATUS_CHANGED
            putExtra(CURRENT_STATUS, position)
        }

        remoteViews.setOnClickFillInIntent(R.id.checkbox, fillInIntent)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun fetchNoteLists(): MutableList<MutableMap<String, Any?>> {
        val fileContent = data.load(context, "current_content")
        var lines = listOf("", "")
        if (fileContent != null) {
            lines = fileContent.split("\n")
        }
        val parsedContent = parseContent(lines)
        return parsedContent
    }

    private fun parseContent(lines: List<String>): MutableList<MutableMap<String, Any?>>
    {
        val list = mutableListOf<MutableMap<String, Any?>>()
        for (line in lines){
            if (line != "")
            {
                val map = mutableMapOf<String, Any?>()
                if (line.contains("- [ ]")) {
                    // Unchecked Checkbox
                    map["text"] = line.removePrefix("- [ ]")
                    map["checkbox"] = 0
                    map["checked"] = false
                    map["heading"] = 14f
                    map["end"] = 8
                }
                else if (line.contains("- [x]")) {
                    // Checked Checkbox
                    map["text"] = line.removePrefix("- [x]")
                    map["checkbox"] = 0
                    map["checked"] = true
                    map["heading"] = 14f
                    map["end"] = 8
                }
                else if (line.startsWith("#") or line.startsWith("##") or line.startsWith("###")) {
                    // Heading
                    map["text"] = line.removePrefix("#").removePrefix("##").removePrefix("###")
                    map["checkbox"] = 8
                    map["checked"] = false
                    map["heading"] = 18f
                    map["end"] = 8
                }
                else if (line.contains("end")) {
                    // End of note
                    map["text"] = ""
                    map["checkbox"] = 8
                    map["checked"] = false
                    map["heading"] = 14f
                    map["end"] = 0
                }
                else {
                    // Add a TextView for text content
                    map["text"] = line
                    map["checkbox"] = 8
                    map["checked"] = false
                    map["heading"] = 14f
                    map["end"] = 8
                }
                list += map
            }
        }
        return list
    }


}