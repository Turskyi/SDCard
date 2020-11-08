package io.github.turskyi.sdcard

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {

    companion object {
        const val APP_TAG = "CD card methods demonstration app"
    }

    private lateinit var photoPickerResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showPathsToFiles()
        showPathsToChaches()
        showPathsOfVisibleSdCards()
        showWhetherSdCardInserted()
        initListener()
        initResultLauncher()
        
    }

    private fun initListener() {
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val action: String = Intent.ACTION_OPEN_DOCUMENT
            val intent = Intent(action)
            intent.type = "image/jpeg"

            val intentChooser = Intent.createChooser(intent, getString(R.string.flag_chooser_title_complete_using))
            photoPickerResultLauncher.launch(intentChooser)
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

//                TODO: so far there is no way to find out the path programmatically
//                showPictureIfWeKnowPath("/sdcard/DSC_3524.JPG")
            } else {
                Toast.makeText(this, getString(R.string.flag_message_did_not_choose),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageFromUri(selectedImageUri: Uri?) {
        if (selectedImageUri != null) {
            val options = BitmapFactory.Options()
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
        /* Raw height and width of image */
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

    private fun showPictureIfWeKnowPath(path: String?) {
        val textJpgName = findViewById<View>(R.id.textviewjpgname) as TextView
        val jpgView: ImageView = findViewById<View>(R.id.jpgview) as ImageView
        /* display the path in the text box */
        textJpgName.visibility = VISIBLE
        jpgView.visibility = VISIBLE
        textJpgName.text = path
        val options = BitmapFactory.Options()
        /* makes image smaller for easier upload to image view */
        options.inSampleSize = 2
        val bm = BitmapFactory.decodeFile(path, options)
        jpgView.setImageBitmap(bm)
    }

    private fun showWhetherSdCardInserted() {
        val mInfoTextView = findViewById<TextView>(R.id.tvIsSdCardInserted)
        mInfoTextView.text = "Is SD card inserted: ${isSDCardInserted()}"
    }

    private fun isSDCardInserted(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun showPathsOfVisibleSdCards() {
        /* API 21 */
        val externalMediaDirs = externalMediaDirs
        var info = "\ngetExternalMediaDirs():\n"
        for (file in externalMediaDirs) {
            info += """
            ${file.absolutePath}
            """.trimIndent()
        }
        val mInfoTextView = findViewById<TextView>(R.id.tvExternalMediaDirs)
        /* show paths of sd card visible folders: internal and external*/
        mInfoTextView.text = info
    }

    private fun showPathsToChaches() {
        /* API 19 */
        val externalCacheDirs = externalCacheDirs
        var info = "\ngetExternalCacheDirs():\n"
        for (file in externalCacheDirs) {
            info += """
            ${file.absolutePath}
            """.trimIndent()
        }
        val mInfoTextView = findViewById<TextView>(R.id.tvExternalCacheDirs)
        /* show paths of cache directories: internal and external*/
        mInfoTextView.text = info
    }

    private fun showPathsToFiles() {
        /* API 19 */
        val externalFilesDirs: Array<File> = getExternalFilesDirs(null)
        var info = "\ngetExternalFilesDirs(null):\n"
        for (file in externalFilesDirs) {
            info += file.absolutePath.toString() + "\n"
        }
        val mInfoTextView = findViewById<TextView>(R.id.tvExternalFilesDirs)
        /* show paths of file directories: internal and external*/
        mInfoTextView.text = info
    }
}

