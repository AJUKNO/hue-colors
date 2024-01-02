package nl.hva.huecolors.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import nl.hva.huecolors.R


/**
 * Hue sub header
 *
 * @param text Text to display
 */
@Composable
fun HueSubHeader(text: String) {
    Text(
        modifier = Modifier.alpha(0.8F),
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 2.sp
    )
}