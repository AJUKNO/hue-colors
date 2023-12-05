package nl.hva.huecolors.ui.screens

sealed class Screens(
    val route: String
) {
    object Bridge {
        object Scan: Screens("scan_screen")
    }
}
