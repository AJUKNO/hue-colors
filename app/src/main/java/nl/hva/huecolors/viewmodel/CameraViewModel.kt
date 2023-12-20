package nl.hva.huecolors.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.hva.huecolors.data.Resource
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val _capturedImage = MutableLiveData<Bitmap>()
    val capturedImage: LiveData<Bitmap>
        get() = _capturedImage

    fun captureImage(image: Bitmap) {
        _capturedImage.value = image
    }

    fun saveToStorage(bitmap: Bitmap, context: Context) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                        throw IOException("Failed to save bitmap.")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}