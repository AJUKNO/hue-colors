package nl.hva.huecolors.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

/**
 * Hue header
 *
 * @param text Text to display
 */
@Composable
fun HueHeader(text: String) {
    Text(
        text = text, style = MaterialTheme.typography.displayMedium, letterSpacing = 2.sp
    )
}