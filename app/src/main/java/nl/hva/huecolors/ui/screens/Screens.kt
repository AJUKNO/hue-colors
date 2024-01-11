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
        object Camera : Screens("camera_screen")
        object Settings : Screens("settings_screen")
        object Library : Screens("library_screen")

        fun getAllRoutes(): List<String> {
            return listOf(
                Lights.route,
                Library.route,
                Camera.route,
                Settings.route
            )
        }
    }
}
