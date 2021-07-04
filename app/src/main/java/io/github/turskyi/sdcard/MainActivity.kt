package io.github.turskyi.sdcard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var photoPickerResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showPathsToFiles()
        showPathsToChaches()
        showPathsOfVisibleSdCards()
        showWhetherSdCardInserted()
        initListeners()
        initResultLauncher()

    }

    private fun initListeners() {
        val button: Button = findViewById(R.id.button)
        val saveFileButton = findViewById<Button>(R.id.button_save)
        val fileNameEditText = findViewById<TextInputEditText>(R.id.edit_filename)
        val contentEditText = findViewById<TextInputEditText>(R.id.edit_content)
        val getFileListButton: Button = findViewById(R.id.button_get_file_list)
        val savedFilesListView: ListView = findViewById(R.id.list_files)
        button.setOnClickListener {
            val action: String = Intent.ACTION_OPEN_DOCUMENT
            val intent = Intent(action)
            intent.type = "image/jpeg"

            val intentChooser: Intent = Intent.createChooser(intent, getString(R.string.flag_chooser_title_complete_using))
            photoPickerResultLauncher.launch(intentChooser)
        }

        saveFileButton.setOnClickListener {
            val fileName: String = fileNameEditText.text.toString() // name of future file
            val content: String = contentEditText.text.toString() // text of description text
            val fos: FileOutputStream
            try {
                fos = openFileOutput(
                    fileName,
                    Context.MODE_PRIVATE
                ) // open file to write
                fos.write(content.toByteArray()) // write data
                fos.close() // close file

                // show if success
                Toast.makeText(
                    applicationContext,
                    "File $fileName saved", Toast.LENGTH_LONG
                ).show()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        getFileListButton.setOnClickListener { v ->
            val savedFilesArray: Array<String> = fileList() // get array of names

            Toast.makeText(this, "${savedFilesArray.size} files", Toast.LENGTH_SHORT).show()

            // set array to adapter
            val adapter: ArrayAdapter<String> = ArrayAdapter(
                v.context,  //getApplicationContext(),
                android.R.layout.simple_list_item_1, savedFilesArray
            )
            // show list of files
            savedFilesListView.adapter = adapter
        }
    }

    private fun initResultLauncher() {
        photoPickerResultLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoChooserIntent: Intent? = result.data
                val selectedImageUri = photoChooserIntent?.data
                showImageFromUri(selectedImageUri)
            } else {
                Toast.makeText(this, getString(R.string.flag_message_did_not_choose),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageFromUri(selectedImageUri: Uri?) {
        if (selectedImageUri != null) {
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            try {
                BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri), null, options)
                val reqSize = Resources.getSystem().displayMetrics.widthPixels
                options.inSampleSize = calculateInSampleSize(options, reqSize, reqSize)
                options.inJustDecodeBounds = false
                val image = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri), null, options)
                val jpgView: ImageView = findViewById<View>(R.id.ivUriPicture) as ImageView
                val tv: TextView = findViewById(R.id.tvUriPictureTitle)
                tv.visibility = VISIBLE
                jpgView.visibility = VISIBLE
                jpgView.setImageBitmap(image)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateInSampleSize(
            options: BitmapFactory.Options, @Suppress("SameParameterValue") reqWidth: Int, @Suppress("SameParameterValue") reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            /* Calculate the largest inSampleSize value that is a power of 2 and keeps both
             height and width larger than the requested height and width. */
            while (halfHeight / inSampleSize > reqHeight
                    && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun showWhetherSdCardInserted() {
        val mInfoTextView: TextView = findViewById<TextView>(R.id.tvIsSdCardInserted)
        mInfoTextView.text = "Is SD card inserted: ${isSDCardInserted()}"
    }

    private fun isSDCardInserted(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun showPathsOfVisibleSdCards() {
        val externalMediaDirs = externalMediaDirs
        var info = "\ngetExternalMediaDirs():\n"
        for (file in externalMediaDirs) {
            info += """
            ${file.absolutePath}
            """.trimIndent()
        }
        val mInfoTextView = findViewById<TextView>(R.id.tvExternalMediaDirs)
        // show paths of sd card visible folders: internal and external
        mInfoTextView.text = info
    }

    private fun showPathsToChaches() {
        // API 19
        val externalCacheDirs = externalCacheDirs
        var info = "\ngetExternalCacheDirs():\n"
        for (file in externalCacheDirs) {
            info += """
            ${file.absolutePath}
            """.trimIndent()
        }
        val mInfoTextView = findViewById<TextView>(R.id.tvExternalCacheDirs)
        // show paths of cache directories: internal and external
        mInfoTextView.text = info
    }

    private fun showPathsToFiles() {
        // API 19
        val externalFilesDirs: Array<File> = getExternalFilesDirs(null)
        var info = "\ngetExternalFilesDirs(null):\n"
        for (file in externalFilesDirs) {
            info += file.absolutePath.toString() + "\n"
        }
        val mInfoTextView = findViewById<TextView>(R.id.tvExternalFilesDirs)
        // show paths of file directories: internal and external
        mInfoTextView.text = info
    }
}

