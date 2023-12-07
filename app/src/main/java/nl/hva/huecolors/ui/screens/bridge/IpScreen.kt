package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.discover.structures.BridgeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.screens.Screens
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.utils.Utils.Companion.isNumeric
import nl.hva.huecolors.viewmodel.HueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpScreen(navController: NavHostController? = null, viewModel: HueViewModel? = null) {
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )

    val coroutineScope = rememberCoroutineScope()
    val (host, setHost) = remember { mutableStateOf("") }

    val setBridge = { ip: String ->
        coroutineScope.launch {
            viewModel?.selectBridge(
                Bridge(
                    id = BridgeId(""),
                    localIp = ip,
                )
            )
        }
    }

    // Debounce host input to avoid calling setBridge on each value change
    LaunchedEffect(host) {
        delay(500)
        setBridge(host)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.navigation_back
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HueButton(
                    text = stringResource(R.string.bridge_connect),
                    onClick = {
                        coroutineScope.launch(Dispatchers.Main) {
                            viewModel?.authorizeBridge()
                        }
                        navController?.navigate(Screens.Bridge.Interact.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
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
                    text = stringResource(R.string.bridge_ip_subheading),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(0.7F)
                        .fillMaxWidth(0.7F)
                )

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(1F)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .height(52.dp),
                    value = host,
                    placeholder = {
                        Text(text = "IP Address")
                    },
                    onValueChange = {
                        if (it.isNumeric()) {
                            setHost(it)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
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