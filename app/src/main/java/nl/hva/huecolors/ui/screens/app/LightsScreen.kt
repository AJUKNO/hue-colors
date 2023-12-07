package nl.hva.huecolors.ui.screens.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import nl.hva.huecolors.viewmodel.HueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightsScreen(navController: NavHostController? = null, viewModel: HueViewModel? = null) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

        }
    }
}