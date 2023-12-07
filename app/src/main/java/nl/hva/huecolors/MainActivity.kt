package nl.hva.huecolors

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
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
    HueNavHost(navController = navController)
}

@Composable
fun HueNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    context: Context? = null
) {
    val viewModel: HueViewModel = viewModel()

    // Bridge flow
    NavHost(
        navController = navController,
        startDestination = Screens.Bridge.route,
        modifier = modifier
    ) {
        BridgeGraph(navController, viewModel)
        AppGraph(navController, viewModel)
    }

//    // App
//    if (viewModel.hue.value?.data?.token?.observeAsState()?.value?.data != null) {
//        NavHost(
//            navController = navController,
//            startDestination = Screens.App.Lights.route,
//            modifier = modifier
//        ) {
//            composable(Screens.App.Lights.route) {
//                LightsScreen(navController = navController, viewModel)
//            }
//        }
//    }
}

fun NavGraphBuilder.BridgeGraph(navController: NavHostController, viewModel: HueViewModel) {
    navigation(
        startDestination = Screens.Bridge.Scan.route,
        route = Screens.Bridge.route
    ) {
        composable(Screens.Bridge.Scan.route) {
            ScanScreen(navController = navController, viewModel)
        }

        composable(Screens.Bridge.Ip.route) {
            IpScreen(navController = navController, viewModel)
        }

        composable(Screens.Bridge.List.route) {
            ListScreen(navController = navController, viewModel)
        }
        composable(Screens.Bridge.Interact.route) {
            InteractScreen(navController = navController, viewModel)
        }
    }
}

fun NavGraphBuilder.AppGraph(navController: NavHostController, viewModel: HueViewModel) {
    navigation(
        startDestination = Screens.App.Lights.route,
        route = Screens.App.route
    ) {
        composable(Screens.App.Lights.route) {
            LightsScreen(navController = navController, viewModel)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HueColorsPreview() {
    HueColorsTheme(darkTheme = true) {
        HueColorsApp()
    }
}