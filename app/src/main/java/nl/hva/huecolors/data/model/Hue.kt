package nl.hva.huecolors.data.model

import androidx.lifecycle.MutableLiveData
import inkapplications.shade.core.Shade
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.AuthToken
import nl.hva.huecolors.data.Resource

data class Hue(
    var shade: MutableLiveData<Resource<Shade>?> = MutableLiveData(),
    var bridges: MutableLiveData<Resource<List<Bridge>>?> = MutableLiveData(),
    var token: MutableLiveData<Resource<AuthToken>> = MutableLiveData()
) {
    init {
        this.shade = MutableLiveData(Resource.Success(Shade()))
    }
}