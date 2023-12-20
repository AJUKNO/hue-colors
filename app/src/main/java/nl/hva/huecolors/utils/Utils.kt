package nl.hva.huecolors.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette

class Utils {

    companion object {
        fun gradient(first: Color, second: Color): Brush = Brush.horizontalGradient(
            listOf(
                first,
                second
            )
        )

        fun handleError(tag: String, error: Exception) {
            Log.e(tag, error.message ?: "An unknown error occurred.")
        }

        fun String.isNumeric(): Boolean {
            return this.all { it.isDigit() || it == ".".single() }
        }

        fun formatIdentifier(id: String) = "(.{2})".toRegex().replace(id, "$1:").removeSuffix(":")

        fun checkPermissions(
            context: Context,
            permissions: Array<String>
        ): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun getPalette(bitmap: Bitmap, maxAmount: Int): Palette {
            return bitmap.let { bitmap ->
                Palette.from(bitmap).maximumColorCount(maxAmount).generate()
            }
        }
    }
}