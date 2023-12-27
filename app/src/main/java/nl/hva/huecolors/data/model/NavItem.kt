package nl.hva.huecolors.data.model

import nl.hva.huecolors.ui.screens.Screens

/**
 * Nav item used to display navigation items in the bottom nav bar
 *
 * @constructor Create Nav item
 * @property route Route string
 * @property painter Icon used to display in the nav item
 * @property label Nav item label
 */
data class NavItem(
    val route: Screens, val painter: Int, val label: String
)
