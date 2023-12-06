package nl.hva.huecolors.utils

import androidx.compose.material3.MaterialTheme
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

        fun String.isNumeric(): Boolean {
            return this.all { it.isDigit() || it == ".".single() }
        }
    }
}