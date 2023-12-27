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
        /**
         * Horizontal gradient brush
         *
         * @param first Start of the gradient color
         * @param second End of the gradient color
         * @return Brush object with a horizontal gradient
         */
        fun gradient(first: Color, second: Color): Brush = Brush.horizontalGradient(
            listOf(
                first, second
            )
        )

        /**
         * Helper function to log errors in viewModels
         *
         * @param tag Tag used in logging
         * @param error Exception with information about the error
         */
        fun handleError(tag: String, error: Exception) {
            Log.e(tag, error.message ?: "An unknown error occurred.")
        }

        /**
         * Helper function to decide whether the string is a digit or a dot
         *
         * @return Boolean
         */
        fun String.isNumeric(): Boolean {
            return this.all { it.isDigit() || it == ".".single() }
        }

        /**
         * Helper function to format the bridge identifier
         *
         * @param id Unformatted id of the bridge
         */
        fun formatIdentifier(id: String) = "(.{2})".toRegex().replace(id, "$1:").removeSuffix(":")

        /**
         * Helper function to check permissions
         *
         * @param context Application context
         * @param permissions List of permissions
         * @return Boolean if permission is granted or not
         */
        fun checkPermissions(
            context: Context, permissions: Array<String>
        ): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(
                    context, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * Get palette by bitmap
         *
         * @param bitmap Bitmap used to extract colors
         * @param maxAmount Max amount of generated palette colors
         * @return Palette object
         */
        fun getPalette(bitmap: Bitmap, maxAmount: Int): Palette {
            return bitmap.let { image ->
                Palette.from(image).maximumColorCount(maxAmount).generate()
            }
        }
    }
}