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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.LightInfo
import nl.hva.huecolors.ui.components.HueHeader
import nl.hva.huecolors.ui.components.HueInfoCard
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.viewmodel.LightViewModel

@Composable
fun LightsScreen(navController: NavHostController, viewModel: LightViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val lights by viewModel.lights.observeAsState()

    DisposableEffect(Unit) {
        coroutineScope.launch {
            viewModel.initShade()
            viewModel.getLights()
        }

        onDispose {

        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurface
                ), checked = true, onCheckedChange = {

                })
            }

            HueHeader("Home")

            Spacer(modifier = Modifier.size(48.dp))

            HueInfoCard(
                headline = "What is this?",
                body = "Extract vibrant color palettes from your photos and watch as Philips Hue lights bring them to life, transforming your space into a personalized, dynamic environment with a touch of your fingertips."
            )

            Spacer(modifier = Modifier.size(48.dp))

            LightList(lights = lights, updateLight = { id, power ->
                coroutineScope.launch {
                    viewModel.toggleLight(id, power)
                }
            })

            Spacer(modifier = Modifier.size(48.dp))
        }
    }

}

@Composable
fun LightList(lights: Resource<List<LightInfo>?>?, updateLight: (String, Boolean) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.alpha(0.8F),
            text = "Lights".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp
        )

        Crossfade(targetState = lights, label = "Lights") { resource ->
            when (resource) {
                is Resource.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                is Resource.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        resource.data?.sortedBy { (it.v1Id.split("/")[2]).toInt() }
                            ?.forEach { light ->
                                LightItem(light = light, updateLight)
                            }
                    }
                }

                is Resource.Empty -> {
                    Text(
                        text = stringResource(R.string.no_lights_found),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                else -> {
                    Text(
                        text = stringResource(R.string.something_happened),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightItem(light: LightInfo, updateLight: (String, Boolean) -> Unit) {
    var power by remember { mutableStateOf(light.power) }
    val lightColor = light.color?.let { Color(it) }
    val state by animateFloatAsState(if (!power) 0.3F else 1F, label = "")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.graphicsLayer { alpha = state },
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 24.dp
        )
    ) {
        Box(Modifier.let {
            if (lightColor != null) {
                it.background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            lightColor.copy(0.5F), lightColor.copy(0.9F)
                        )
                    )
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
                            contentDescription = "Bridge"
                        )

                        Column {
                            Text(
                                text = "${light.label}",
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
                        updateLight(light.id, !power)
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