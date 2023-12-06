package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.discover.structures.BridgeId
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.screens.Screens
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.HueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavHostController? = null, viewModel: HueViewModel? = null) {
    var loading by remember { mutableStateOf(false) }
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )
    val bridges = listOf(
        Bridge(
            id = BridgeId("ecb5fafffea4e537"),
            localIp = "192.168.1.27",
            port = 443
        ),
        Bridge(
            id = BridgeId("ecb5fafffea4e537"),
            localIp = "192.168.1.28",
            port = 443
        ),
        Bridge(
            id = BridgeId("ecb5fafffea4e537"),
            localIp = "192.168.1.29",
            port = 443
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.navigation_back
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HueButton(
                    text = stringResource(id = R.string.bridge_scan),
                    onClick = { /* TODO: Perform bridge search */ },
                    secondary = true
                )
                HueButton(
                    text = stringResource(R.string.bridge_connect),
                    onClick = {
                        //TODO: Navigate to Interact screen navController?.navigate(Screens.Bridge.Interact.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.list_heading),
                style = MaterialTheme.typography.titleLarge.copy(brush),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.list_subheading),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.7F)
            )

            // Bridge List
            bridges?.let {
                BridgeList(bridges = it)
            }
        }
    }
}

@Composable
fun BridgeList(
    bridges: List<Bridge>,
) {
    val (selectedBridge, setSelectedBridge) = remember { mutableStateOf<Bridge?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(bridges) { bridge ->
            BridgeListItem(bridge = bridge, isSelected = selectedBridge == bridge) {
                setSelectedBridge(bridge)
            }
        }
    }
}

@Composable
fun BridgeListItem(
    bridge: Bridge,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    RadioButton(selected = isSelected, onClick = {
        onSelect()
    })
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        ListScreen()
    }
}