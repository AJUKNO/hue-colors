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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nl.hva.huecolors.ui.screens.Screens
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
    HueColorsNavHost(navController = navController)
}

@Composable
fun HueColorsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    context: Context? = null
) {
    val viewModel: HueViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screens.Bridge.Scan.route,
        modifier = modifier
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
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HueColorsPreview() {
    HueColorsTheme(darkTheme = true) {
        HueColorsApp()
    }
}