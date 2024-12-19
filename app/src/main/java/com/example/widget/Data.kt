package com.example.widget

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

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

    fun getFileContents(context: Context, uri: Uri): MutableMap<String, String> {
        val noteContent = mutableMapOf<String, String>()
        val folder = DocumentFile.fromTreeUri(context, uri)
        folder?.listFiles()?.forEach { file ->

            // Handle files inside the folder
            if (file.isFile) {
                if (file.name?.endsWith(".md") == true) {
                    val inputStream = context.contentResolver.openInputStream(file.uri)
                    if (inputStream != null) {
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val content = reader.use { it.readText() } // Read the entire file as a String
                        noteContent[file.name.toString().removeSuffix(".md")] = content
                        inputStream.close()
                    } else {
                        println("Failed to open InputStream for file: ${file.name}")
                    }
                }
            }
        }
        return noteContent
    }

    fun writeFile (context: Context, uri: Uri, fileName: String, fileContent: String)
    {
        val folder = DocumentFile.fromTreeUri(context, uri)
        folder?.listFiles()?.forEach { file ->
            // Handle files inside the folder
            if (file.isFile) {
                if (file.name?.endsWith(".md") == true && file.name?.removeSuffix(".md")?.trim() == fileName) {
                    val outputStream = context.contentResolver.openOutputStream(file.uri, "wt")
                    if (outputStream != null) {
                        try {
                            outputStream.write(fileContent.toByteArray())
                            println("Successfully wrote to file: ${file.name}")
                        } catch (e: IOException) {
                            println("Error writing to file: ${e.message}")
                        } finally {
                            try {
                                outputStream.close()
                            } catch (closeException: IOException) {
                                println("Error closing the stream: ${closeException.message}")
                            }
                        }
                    } else {
                        println("Failed to write file: ${file.name}")
                    }
                }
            }
        }
    }
}