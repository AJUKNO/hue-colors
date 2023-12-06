package nl.hva.huecolors.ui.screens

sealed class Screens(
    val route: String
) {
    object Bridge {
        object Scan: Screens("scan_screen")
        object Ip: Screens("ip_screen")
        object List: Screens("list_screen")
        object Interact: Screens("interact_screen")
    }
}
