package nl.hva.huecolors.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Hue button
 *
 * @param text Label to display on the button
 * @param icon Optional icon to display on the button
 * @param onClick Called when the button is clicked
 * @param secondary Secondary button styling
 * @param disabled Controls the state of the button
 */
@Composable
fun HueButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    secondary: Boolean = false,
    disabled: Boolean? = false
) {
    val type = ButtonDefaults.buttonColors(
        containerColor = if (secondary) Color.Transparent else MaterialTheme.colorScheme.primary,
        contentColor = if (secondary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary
    )

    val modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .let {
            if (secondary) it.border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ) else it
        }

    Button(
        onClick = onClick,
        enabled = disabled?.not() ?: false,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = type
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(imageVector = it, contentDescription = text, modifier = Modifier.size(16.dp))
            }
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}