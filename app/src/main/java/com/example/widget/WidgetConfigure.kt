package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.widget.databinding.ConfigLayoutBinding
import com.example.widget.databinding.WidgetConfigureBinding
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * The configuration screen for the [WidgetProvider] AppWidget.
 */
class WidgetConfigure : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var selectFileButton: Button
    private var selectedFolderUri: Uri? = null
    private val data = Data()
    private lateinit var adapter: TextViewAdapter
    private val noteContent = mutableMapOf<String, String>()
    private val context = this@WidgetConfigure
    private lateinit var binding: WidgetConfigureBinding
    private lateinit var mergeBnd: ConfigLayoutBinding
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val content = result.data
            val fileUri = content?.data
            fileUri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                accessFolderContents(it)
                selectedFolderUri = it
                data.save(context, "selected_folder_uri", it.toString())
            }
        }
    }
    private var onClickListener = View.OnClickListener {
        if(noteContent.isNotEmpty()) {
            setColors()
            mergeNotes()
        }
        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        setResult(RESULT_OK, resultValue)
        finish()
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)


        binding = WidgetConfigureBinding.inflate(layoutInflater)
        mergeBnd = ConfigLayoutBinding.bind(binding.root)
        setContentView(binding.root)
        selectFileButton = findViewById(R.id.select_file_button)
        binding.addButton.setOnClickListener(onClickListener)
        selectFileButton.setOnClickListener {
            getFiles()
        }

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    private fun getFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        filePickerLauncher.launch(intent)
    }

    private fun setColors()
    {
        val bgAlpha = mergeBnd.bgAlpha.text.toString()
        val bgRed = mergeBnd.bgRed.text.toString()
        val bgGreen = mergeBnd.bgGreen.text.toString()
        val bgBlue = mergeBnd.bgBlue.text.toString()
        val textAlpha = mergeBnd.textAlpha.text.toString()
        val textRed = mergeBnd.textRed.text.toString()
        val textGreen = mergeBnd.textGreen.text.toString()
        val textBlue = mergeBnd.textBlue.text.toString()

        if (bgAlpha != "" && bgRed != "" && bgGreen != "" && bgBlue != "")
        {
            val backgroundArgb = ("$bgAlpha,$bgRed,$bgGreen,$bgBlue")
            data.save(context, "background_argb", backgroundArgb)
        }

        if (textAlpha != "" && textRed != "" && textGreen != "" && textBlue != "")
        {
            val textArgb = ("$textAlpha,$textRed,$textGreen,$textBlue")
            data.save(context, "text_argb", textArgb)
        }
    }

    private fun accessFolderContents(uri: Uri) {
        val folder = DocumentFile.fromTreeUri(this, uri)
        folder?.listFiles()?.forEach { file ->

            // Handle files inside the folder
            if (file.isFile) {
                if (file.name?.endsWith(".md") == true) {
                    val inputStream = contentResolver.openInputStream(file.uri)
                    if (inputStream != null) {
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val content = reader.use { it.readText() } // Read the entire file as a String
                        noteContent[file.name!!] = content
                        inputStream.close()
                    } else {
                        println("Failed to open InputStream for file: ${file.name}")
                    }
                }
            }
        }
        addLayouts()
    }

    private fun addLayouts()
    {
        val items = mutableListOf<String>()
        noteContent.forEach { (fileName, content) ->
            items += fileName
        }

        adapter = TextViewAdapter(items)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(DragManageAdapter(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun mergeNotes() {
        val currentOrder = adapter.getItems()
        data.save(context, "current_order", currentOrder.toString())
        var content = ""
        currentOrder.forEach {
            content += noteContent[it]
            content += "\n\n end \n"
        }
        data.save(context, "current_content", content)
    }

}
