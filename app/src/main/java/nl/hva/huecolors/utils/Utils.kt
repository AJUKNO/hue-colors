package nl.hva.huecolors.utils

import android.util.Log
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
    }
}