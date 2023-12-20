package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
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
fun InteractScreen(navController: NavHostController, viewModel: BridgeViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary
    )
    val isBridgeAuthorized by viewModel.isBridgeAuthorized.observeAsState()

    DisposableEffect(true) {
        coroutineScope.launch(Dispatchers.Main) {
            viewModel.authorizeBridge()
        }

        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HueButton(
                    text = stringResource(R.string.bridge_next),
                    disabled = !(isBridgeAuthorized is Resource.Success),
                    onClick = {
                        navController.navigate(Screens.App.route)
                    }
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                space = 20.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.5F,
                    targetValue = 1F,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = ""
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_press),
                    contentDescription = "Press",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(alpha)
                        .offset(y = 20.dp, x = 8.dp)
                        .graphicsLayer(0.99F)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush, blendMode = BlendMode.SrcAtop)
                            }
                        }
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_bridge),
                    contentDescription = "Bridge",
                    modifier = Modifier
                        .size(96.dp)
                        .alpha(0.5F)
                )
            }

            Text(
                text = stringResource(R.string.interact_heading),
                style = MaterialTheme.typography.titleLarge.copy(brush),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.interact_subheading),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(0.7F)
                    .fillMaxWidth(0.7F)
            )

            Crossfade(targetState = isBridgeAuthorized, label = "Bridges") { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Icon(
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.interact_success)
                        )
                    }

                    else -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun InteractScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        InteractScreen(rememberNavController(), viewModel())
    }
}