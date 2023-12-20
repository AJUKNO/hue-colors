package nl.hva.huecolors.ui.screens.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import nl.hva.huecolors.viewmodel.BridgeViewModel

@Composable
fun LibraryScreen(navController: NavHostController, viewModel: BridgeViewModel) {
    Text(text = "LIBRARY")
}