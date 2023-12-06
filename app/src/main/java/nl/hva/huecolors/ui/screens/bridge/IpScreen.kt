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
import androidx.compose.material3.TextField
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
fun IpScreen(navController: NavHostController? = null) {
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
                    text = stringResource(R.string.bridge_connect),
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
                painter = painterResource(id = R.drawable.ic_circuit),
                contentDescription = "Bridge",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(112.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.bridge_ip_address),
                    style = MaterialTheme.typography.titleLarge.copy(brush),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "If you are aware of your Bridge's IP address, kindly enter it.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(0.7F)
                        .fillMaxWidth(0.7F)
                )

                TextField(value = "", onValueChange = {}, placeholder = {
                    Text(text = stringResource(R.string.bridge_ip_placeholder))
                })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IpScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        IpScreen(navController = null)
    }
}