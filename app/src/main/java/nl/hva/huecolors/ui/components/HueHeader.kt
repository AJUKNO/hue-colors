package nl.hva.huecolors.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HueHeader(text: String) {
    Text(text = text, style = MaterialTheme.typography.displayMedium)
}