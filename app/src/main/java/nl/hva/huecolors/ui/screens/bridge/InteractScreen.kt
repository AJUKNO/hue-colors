package nl.hva.huecolors.ui.screens.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import nl.hva.huecolors.ui.theme.HueColorsTheme
import nl.hva.huecolors.viewmodel.HueViewModel

@Composable
fun InteractScreen(navController: NavHostController? = null, viewModel: HueViewModel? = null) {

}

@Preview(showBackground = true)
@Composable
fun InteractScreenPreview() {
    HueColorsTheme(darkTheme = true) {
        InteractScreen()
    }
}