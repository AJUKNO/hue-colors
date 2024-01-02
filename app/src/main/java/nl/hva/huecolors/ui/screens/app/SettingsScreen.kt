package nl.hva.huecolors.ui.screens.app

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.ui.components.HueSubHeader
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.BridgeViewModel
import nl.hva.huecolors.viewmodel.LightViewModel

@Composable
fun SettingsScreen(navController: NavHostController, viewModel: BridgeViewModel) {
    val devices by viewModel.devices.observeAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED, Lifecycle.State.STARTED -> {
                coroutineScope.launch {
                    viewModel.initShade()
                    viewModel.getGroupedDevices()
                }
            }

            else -> {

            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                devices?.data?.forEach { device ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                Text(text = device.metadata.name)
                                Text(
                                    text = device.productData.productName,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.alpha(0.7F)
                                )
                                Text(
                                    text = device.productData.softwareVersion.full,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.alpha(0.7F)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}