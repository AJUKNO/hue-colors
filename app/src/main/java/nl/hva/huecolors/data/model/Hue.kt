package nl.hva.huecolors.data.model

import inkapplications.shade.core.Shade
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.AuthToken

data class Hue(
    var shade: Shade? = null,
    var bridges: List<Bridge>? = null,
    var token: AuthToken? = null
) {
    init {
        this.shade = Shade()
    }
}