package nl.hva.huecolors.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.hva.huecolors.utils.Utils

/**
 * Hue info card
 *
 * @param headline Headline to display
 * @param body Body to display
 */
@Composable
fun HueInfoCard(headline: String, body: String) {
    Card(
        border = BorderStroke(
            1.dp, brush = Utils.gradient(
                MaterialTheme.colorScheme.secondary.copy(0.3F),
                MaterialTheme.colorScheme.onSecondary
            )
        ), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.1F)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = headline.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 2.sp
            )

            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.8F)
            )
        }
    }
}