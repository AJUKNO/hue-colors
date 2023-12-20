package nl.hva.huecolors.ui.screens.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import nl.hva.huecolors.viewmodel.BridgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightsScreen(navController: NavHostController? = null, viewModel: BridgeViewModel? = null) {
//    val hue = viewModel?.hue?.observeAsState()
    val coroutineScope = rememberCoroutineScope()
    
    Text(text = "AUthorized")

//    LaunchedEffect(true) {
//        coroutineScope.launch {
//            viewModel?.init()
//            hue?.value?.data?.shade?.value?.data?.configuration?.apply {
//                setHostname("192.168.1.27")
//                setSecurityStrategy(SecurityStrategy.Insecure("192.168.1.27"))
//                setAuthToken(AuthToken("5ycNHzDs6YNW43VZmC0vM1DB0ElkeP63dof7aGQ2", "50C12A164F128B4090E1292224FAB498"))
//            }
//        }
//    }
//
//    var shade = hue?.value?.data?.shade?.observeAsState()
//    var lights = remember { mutableStateOf<List<Light>?>(null) }

//    LaunchedEffect(true) {
//        coroutineScope.launch {
//            lights.value = shade?.value?.data?.lights?.listLights()
//
//            Log.i("HUE", lights.value?.toString() ?: "")
//            shade?.value?.data?.lights?.updateLight(
//                id = ResourceId("9ca61e83-4241-4bef-af89-3cc59475cb4c"),
//                parameters = LightUpdateParameters(dimming = DimmingParameters(
//                    brightness = 10.percent
//                )
//                )
//            )
//        }
//    }
//
//    Scaffold { innerPadding ->
//        Column(
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            Text(text = lights.value.toString())
//        }
//    }
}