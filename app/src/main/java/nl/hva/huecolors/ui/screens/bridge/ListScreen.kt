package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import inkapplications.shade.discover.structures.Bridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.screens.Screens
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.BridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavHostController, viewModel: BridgeViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary
    )
    val bridgeDiscovery by viewModel.bridgeDiscovery.observeAsState()

    Scaffold(topBar = {
        TopAppBar(modifier = Modifier.padding(horizontal = 8.dp), title = {}, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(
                        R.string.navigation_back
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        })
    }, bottomBar = {
        Column(
            modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val (disabled, setDisabled) = remember { mutableStateOf(false) }
            LaunchedEffect(disabled) {
                delay(2000)
                setDisabled(false)
            }

            HueButton(
                text = stringResource(id = R.string.bridge_scan),
                icon = Icons.Filled.Search,
                onClick = {
                    coroutineScope.launch(Dispatchers.Main) {
                        setDisabled(true)
                        viewModel.discoverBridges()
                    }
                },
                secondary = true,
                disabled
            )
            HueButton(text = stringResource(R.string.bridge_connect),
                disabled = bridgeDiscovery?.data.isNullOrEmpty(),
                onClick = { navController.navigate(Screens.Bridge.Interact.route) })
        }
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
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

            Crossfade(targetState = bridgeDiscovery, label = "Bridges") { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // BridgeList
                        resource.data?.let {
                            BridgeList(bridges = it) { bridge ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    viewModel.selectBridge(bridge)
                                }
                            }
                        }
                    }

                    is Resource.Loading -> {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.list_no_bridges_found),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

        }

    }
}

@Composable
fun BridgeList(
    bridges: List<Bridge>, onSelect: (Bridge) -> Unit
) {
    val (selectedBridge, setSelectedBridge) = remember { mutableStateOf<Bridge?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(bridges) { bridge ->
            BridgeListItem(bridge = bridge, isSelected = selectedBridge == bridge) {
                setSelectedBridge(bridge)
                onSelect(bridge)
            }
        }
    }
}

@Composable
fun BridgeListItem(
    bridge: Bridge, isSelected: Boolean, onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bridge),
                contentDescription = "Bridge",
                modifier = Modifier.size(36.dp)
            )

            Column {
                Text(text = "Hue Bridge")
                Text(
                    text = bridge.localIp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7F)
                )
                Text(
                    text = Utils.formatIdentifier(bridge.id.value),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7F)
                )
            }
        }
        RadioButton(selected = isSelected, onClick = onSelect)
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        ListScreen(rememberNavController(), viewModel())
    }
}