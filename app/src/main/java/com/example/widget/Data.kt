package com.example.widget

import android.content.Context

class Data {

    fun save(context: Context?, key: String, content: String) {
        val sharedPreferences = context?.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString(key, content)?.apply()
    }

    fun load(context: Context?, key: String): String? {
        val sharedPreferences = context?.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val content = sharedPreferences?.getString(key, null)
        return content
    }
}