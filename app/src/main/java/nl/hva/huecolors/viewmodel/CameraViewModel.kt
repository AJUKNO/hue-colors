package nl.hva.huecolors.viewmodel

import android.annotation.SuppressLint
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.hva.huecolors.R
import nl.hva.huecolors.utils.Utils
import java.io.File
import java.io.IOException
import java.io.OutputStream

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val TAG = context.getString(R.string.camera_model)
    private val _capturedImage = MutableLiveData<Bitmap>()
    val capturedImage: LiveData<Bitmap>
        get() = _capturedImage

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    /**
     * Capture image, set captured image as LiveData
     *
     * @param image
     */
    fun captureImage(image: Bitmap) {
        _capturedImage.postValue(image)
    }

    /**
     * Save bitmap to storage (media folder)
     *
     * @param bitmap
     * @param context
     */
    suspend fun saveToStorage(bitmap: Bitmap?, context: Context) {
        if (bitmap != null) {
            try {
                withContext(Dispatchers.IO) {
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
                        val outputStream: OutputStream? = resolver.openOutputStream(it)
                        outputStream?.use { stream ->
                            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                                throw IOException(context.getString(R.string.failed_to_save_bitmap))
                            }
                        }
                    }
                }
                
                showToast(context.getString(R.string.saved_image))
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    private fun showToast(message: String) {
        _toastMessage.postValue(message)
    }

    private fun handleError(error: Exception) {
        Utils.handleError(TAG, error)
        val errorMessage = error.message ?: context.getString(R.string.an_unknown_error_occurred)
        showToast(errorMessage)
    }
}