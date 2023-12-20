package nl.hva.huecolors

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.hva.huecolors.ui.screens.Screens
import nl.hva.huecolors.ui.screens.app.LightsScreen
import nl.hva.huecolors.ui.screens.bridge.InteractScreen
import nl.hva.huecolors.ui.screens.bridge.IpScreen
import nl.hva.huecolors.ui.screens.bridge.ListScreen
import nl.hva.huecolors.ui.screens.bridge.ScanScreen
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.viewmodel.HueViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HueColorsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HueColorsApp()
                }
            }
        }
    }
}

@Composable
fun HueColorsApp() {
    val navController = rememberNavController()
    HueNavHost(navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HueNavHost(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: HueViewModel = viewModel()
    var bridgePresent by remember { mutableStateOf<Boolean?>(null) }

    // TODO: Check if bridge is present

    val startDestination =
        rememberUpdatedState(if (bridgePresent == true) Screens.App.route else Screens.Bridge.route)

    if (bridgePresent != null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            bottomBar = {
//                if (navController.currentBackStackEntryAsState().value?.destination?.route in Screens.App.getAllRoutes()) {
//                    BottomNavBar(null, navController)
//                }
            },
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination.value
                ) {
                    BridgeGraph<HueViewModel>(navController)
                    AppGraph<HueViewModel>(navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(lambda: (() -> Job)?, navController: NavHostController) {
    val navEntry by navController.currentBackStackEntryAsState()

    NavigationBar(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0F0F0F),
                    MaterialTheme.colorScheme.scrim
                )
            )
        ).height(80.dp),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 16.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
//            BottomNavBarItem(
//                active = navEntry?.destination?.route == Screens.App.Library.route,
//                painter = R.drawable.ic_folder,
//                label = "Library",
//                navController = navController,
//                route = Screens.App.Library,
//                modifier = Modifier.weight(1 / 4F)
//            )
//
//            BottomNavBarItem(
//                active = navEntry?.destination?.route == Screens.App.Lights.route,
//                painter = R.drawable.ic_bulb,
//                label = "Lights",
//                navController = navController,
//                route = Screens.App.Lights,
//                modifier = Modifier.weight(1 / 4F)
//            )
//
//            BottomNavBarItem(
//                active = navEntry?.destination?.route == Screens.App.Camera.route,
//                painter = R.drawable.ic_settings,
//                label = "Settings",
//                navController = navController,
//                route = Screens.App.Camera,
//                modifier = Modifier.weight(1 / 4F)
//            )
//
//            BottomNavBarItem(
//                active = navEntry?.destination?.route == Screens.App.Camera.route,
//                painter = R.drawable.ic_camera,
//                label = "Camera",
//                navController = navController,
//                route = Screens.App.Camera,
//                modifier = Modifier.weight(1 / 4F)
//            )
        }
    }
}

@SuppressLint("ResourceType")
@Composable
fun BottomNavBarItem(active: Boolean, painter: Int, label: String, navController: NavHostController, route: Screens, modifier: Modifier) {
    val alpha by animateFloatAsState(targetValue = if (active) 1F else 0F, label = "")

    Button(
        onClick = { navController.navigate(route.route) {
            popUpTo(1)
        } },
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp),
                painter = painterResource(id = painter),
                contentDescription = "Bridge",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                modifier = Modifier.alpha(alpha),
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp
                ),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return viewModel(parentEntry)
}

inline fun <reified T : ViewModel> NavGraphBuilder.BridgeGraph(navController: NavHostController) {
    navigation(
        startDestination = Screens.Bridge.Scan.route,
        route = Screens.Bridge.route
    ) {
        composable(Screens.Bridge.Scan.route) {
            ScanScreen(navController = navController, it.sharedViewModel(navController))
        }

        composable(Screens.Bridge.Ip.route) {
            IpScreen(navController = navController, it.sharedViewModel(navController))
        }

        composable(Screens.Bridge.List.route) {
            ListScreen(navController = navController, it.sharedViewModel(navController))
        }
        composable(Screens.Bridge.Interact.route) {
            InteractScreen(navController = navController, it.sharedViewModel(navController))
        }
    }
}

inline fun <reified T : ViewModel> NavGraphBuilder.AppGraph(navController: NavHostController) {
    navigation(
        startDestination = Screens.App.Lights.route,
        route = Screens.App.route
    ) {
        composable(Screens.App.Lights.route) {
            LightsScreen(navController = navController, it.sharedViewModel(navController))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HueColorsPreview() {
    HueColorsTheme(darkTheme = true) {
        HueColorsApp()
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    HueColorsTheme(darkTheme = true) {
        BottomNavBar(null, rememberNavController())
    }
}