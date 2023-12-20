package nl.hva.huecolors.data.model

import nl.hva.huecolors.R
import nl.hva.huecolors.ui.screens.Screens

data class NavItem(
    val route: Screens,
    val painter: Int,
    val label: String
)
