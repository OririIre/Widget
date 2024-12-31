package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.widget.databinding.ConfigLayoutBinding
import com.example.widget.databinding.WidgetConfigureBinding

/**
 * The configuration screen for the [WidgetProvider] AppWidget.
 */
class WidgetConfigure : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var selectFileButton: Button
    private var selectedFolderUri: Uri? = null
    private val data = Data()
    private lateinit var adapter: TextViewAdapter
    private var fileContent = mutableMapOf<String, String>()
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
                fileContent = data.getFileContents(context ,it)
                addLayouts()
                selectedFolderUri = it
                data.save(context, "selected_folder_uri", it.toString())
            }
        }
    }
    private var onClickListener = View.OnClickListener {
        if(fileContent.isNotEmpty()) {
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

    private fun addLayouts()
    {
        val items = mutableListOf<String>()
        fileContent.forEach { (fileName, _) ->
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
            content += "#fileName:" + it.trim() + "\n"
            content += fileContent[it]
            content += "\nend \n"
        }
        data.save(context, "current_content", content)
    }

}
