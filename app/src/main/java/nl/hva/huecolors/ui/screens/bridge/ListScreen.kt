package nl.hva.huecolors.ui.screens.bridge

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.discover.structures.BridgeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Status
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.HueViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavHostController? = null, viewModel: HueViewModel? = null) {
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )
    val coroutineScope = rememberCoroutineScope()
    val status: State<Status<Any>?>? = viewModel?.status?.observeAsState()
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
                val (disabled, setDisabled) = remember { mutableStateOf(false) }
                LaunchedEffect(disabled) {
                    delay(2000)
                    setDisabled(false)
                }

                HueButton(
                    text = stringResource(id = R.string.bridge_scan),
                    icon = Icons.Filled.Search,
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            viewModel?.searchBridges()
                        }
                        setDisabled(true)
                    },
                    secondary = true,
                    disabled
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
            
            when (status?.value) {
                is Status.Success -> {
                    // BridgeList
                    BridgeList(bridges = bridges, viewModel)
                }

                is Status.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.height(2.dp).fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                else -> {
                    Text(text = "No bridges found.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun BridgeList(
    bridges: List<Bridge>,
    viewModel: HueViewModel? = null
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

                coroutineScope.launch(Dispatchers.IO) {
                    viewModel?.selectBridge(bridge)
                }
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
        RadioButton(selected = isSelected, onClick = {
            onSelect()
        })
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        ListScreen()
    }
}