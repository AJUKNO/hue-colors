package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nl.hva.huecolors.R
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.screens.Screens
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    navController: NavHostController?,
) {
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HueButton(
                    text = stringResource(R.string.bridge_ip_address),
                    secondary = true,
                    onClick = {
                        navController?.navigate(Screens.Bridge.Scan.route)
                    }
                )

                HueButton(
                    text = stringResource(R.string.bridge_scan),
                    icon = Icons.Filled.Search,
                    onClick = {
                        navController?.navigate(Screens.Bridge.Scan.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                space = 20.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bridge),
                contentDescription = "Bridge",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(96.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Scan Hue Bridge",
                    style = MaterialTheme.typography.titleLarge.copy(brush),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Please connect your bridge to power and Ethernet. Check that you are on the same Wi-Fi network.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(0.7F)
                        .fillMaxWidth(0.7F)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        ScanScreen(navController = null)
    }
}