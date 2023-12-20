package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.ui.components.HueButton
import nl.hva.huecolors.ui.screens.Screens
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.viewmodel.HueViewModel

@Composable
fun ScanScreen(navController: NavHostController, viewModel: HueViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val brush = Utils.gradient(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary
    )

    val hue by viewModel.hue.observeAsState()

    Scaffold(bottomBar = {
        Column(
            modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HueButton(text = stringResource(R.string.bridge_ip_address),
                secondary = true,
                onClick = {
                    coroutineScope.launch {
                        viewModel.init()
                        navController.navigate(Screens.Bridge.Ip.route)
                    }
                })

            HueButton(text = stringResource(R.string.bridge_scan),
                icon = Icons.Filled.Search,
                onClick = {
                    coroutineScope.launch {
                        viewModel.init()
                        navController.navigate(Screens.Bridge.List.route)
                    }
                })
        }
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(padding)
                .fillMaxHeight()
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(
                space = 20.dp, alignment = Alignment.CenterVertically
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bridge),
                contentDescription = stringResource(R.string.bridge),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.scan_heading),
                    style = MaterialTheme.typography.titleLarge.copy(brush),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.scan_subheading),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(0.7F)
                        .fillMaxWidth(0.7F)
                )
            }
        }

    }

    AnimatedVisibility(
        visible = hue is Resource.Loading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(0.7F))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        ScanScreen(rememberNavController(), viewModel())
    }
}