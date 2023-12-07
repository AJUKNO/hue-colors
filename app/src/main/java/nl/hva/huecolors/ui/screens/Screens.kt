package nl.hva.huecolors.ui.screens

sealed class Screens(
    val route: String
) {
    object Bridge : Screens("bridge_graph") {
        object Scan : Screens("scan_screen")
        object Ip : Screens("ip_screen")
        object List : Screens("list_screen")
        object Interact : Screens("interact_screen")
    }

    object App : Screens("app_graph") {
        object Lights : Screens("lights_screen")
    }
}
