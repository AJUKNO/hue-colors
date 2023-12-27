package nl.hva.huecolors.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.hva.huecolors.utils.Utils
import java.io.File
import java.io.IOException
import java.io.OutputStream

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "CAMERA_MODEL"
    private val _capturedImage = MutableLiveData<Bitmap>()
    val capturedImage: LiveData<Bitmap>
        get() = _capturedImage

    fun captureImage(image: Bitmap) {
        _capturedImage.value = image
    }

    fun saveToStorage(bitmap: Bitmap?, context: Context) {
        if (bitmap != null) {
            viewModelScope.launch {
                try {
                    val hueFolder = "palette"
                    val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                    val relativeLocation =
                        Environment.DIRECTORY_PICTURES + File.separator + hueFolder
                    val filePath = File(relativeLocation)

                    if (!filePath.exists()) {
                        filePath.mkdirs()
                    }

                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation)
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    }

                    val resolver = context.contentResolver
                    val imageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    imageUri?.let {
                        try {
                            val outputStream: OutputStream? = resolver.openOutputStream(it)
                            outputStream?.use { stream ->
                                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                                    throw IOException("Failed to save bitmap.")
                                }
                            }
                        } catch (e: IOException) {
                            Utils.handleError(TAG, e)
                        }
                    }
                } catch (error: Exception) {
                    Utils.handleError(TAG, error)
                }
            }
        }
    }
}