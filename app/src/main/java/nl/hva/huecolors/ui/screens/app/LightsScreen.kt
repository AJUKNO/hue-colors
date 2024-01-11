package nl.hva.huecolors.ui.screens.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.LightInfo
import nl.hva.huecolors.ui.components.HueHeader
import nl.hva.huecolors.ui.components.HueInfoCard
import nl.hva.huecolors.ui.components.HueSubHeader
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.LightViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightsScreen(navController: NavHostController, viewModel: LightViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val lights by viewModel.lights.observeAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                coroutineScope.launch {
                    viewModel.initShade()
                    viewModel.getLights()
                }
            }

            else -> {}
        }
    }
    val isRefreshing by remember {
        mutableStateOf(false)
    }
    val state = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        coroutineScope.launch {
            viewModel.getLights()
        }
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.size(48.dp))

                HueHeader(stringResource(R.string.home))

                Spacer(modifier = Modifier.size(48.dp))

                HueInfoCard(
                    headline = stringResource(R.string.what_is_this),
                    body = stringResource(R.string.lights_description)
                )

                Spacer(modifier = Modifier.size(48.dp))

                LightList(lights = lights, updateLight = { id, power ->
                    coroutineScope.launch {
                        viewModel.toggleLight(id, power)
                    }
                }, identifyLight = { id ->
                    coroutineScope.launch {
                        viewModel.identifyLight(id)
                    }
                })

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing, state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
    }

}

/**
 * Light list
 *
 * @param lights List of lights using Resource as a wrapper
 * @param updateLight Lambda to update the light
 * @param identifyLight Lambda to identify the light
 */
@Composable
fun LightList(
    lights: Resource<List<LightInfo>?>?,
    updateLight: (String, Boolean) -> Unit,
    identifyLight: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HueSubHeader(text = stringResource(R.string.lights_title))

        Crossfade(targetState = lights, label = stringResource(R.string.lights_title)) { resource ->
            when (resource) {
                is Resource.Loading -> LinearProgressIndicator(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary
                )

                is Resource.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        resource.data?.sortedBy { it.v1Id.split("/")[2].toInt() }
                            ?.forEach { light ->
                                LightItem(
                                    light = light,
                                    onToggle = updateLight,
                                    onClick = identifyLight
                                )
                            }
                    }
                }

                is Resource.Empty -> Text(
                    text = stringResource(R.string.no_lights_found),
                    style = MaterialTheme.typography.bodySmall
                )

                else -> Text(
                    text = stringResource(R.string.something_happened),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Light item
 *
 * @param light LightInfo object
 * @param onToggle Lambda that calls on switch toggle
 * @param onClick Lambda that calls on card click
 */
@Composable
fun LightItem(light: LightInfo, onToggle: (String, Boolean) -> Unit, onClick: (String) -> Unit) {
    var power by remember { mutableStateOf(light.power) }
    val lightColor = light.color?.let { Color(it) }
    val state by animateFloatAsState(if (!power) 0.3F else 1F, label = "")

    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ),
        modifier = Modifier.graphicsLayer { alpha = state },
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 24.dp
        ),
        onClick = { onClick(light.v1Id) }
    ) {
        Box(Modifier.let {
            if (lightColor != null) {
                it.background(
                    brush = Utils.gradient(lightColor.copy(0.6F), lightColor.copy(1F))
                )
            } else it
        }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(36.dp),
                            painter = painterResource(id = R.drawable.ic_bulb),
                            contentDescription = stringResource(id = R.string.bridge)
                        )

                        Column {
                            Text(
                                text = light.label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                modifier = Modifier.alpha(0.7F),
                                text = light.v1Id,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                    }

                    Switch(colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.inverseSurface,
                        checkedTrackColor = MaterialTheme.colorScheme.inverseOnSurface.copy(0.5F),
                        uncheckedThumbColor = MaterialTheme.colorScheme.inverseSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.inverseOnSurface.copy(
                            0.5F
                        ),
                        uncheckedBorderColor = MaterialTheme.colorScheme.inverseOnSurface.copy(
                            0.5F
                        )
                    ), checked = power, onCheckedChange = {
                        onToggle(light.id, !power)
                        power = !power
                    })
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun LightsScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        LightsScreen(rememberNavController(), viewModel())
    }
}