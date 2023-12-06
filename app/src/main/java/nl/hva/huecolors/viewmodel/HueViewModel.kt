package nl.hva.huecolors.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import inkapplications.shade.core.Shade
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.SecurityStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.hva.huecolors.data.model.Hue

class HueViewModel(application: Application) : AndroidViewModel(application) {

    private var _hue: MutableLiveData<Hue> = MutableLiveData()
    val hue: LiveData<Hue>
        get() = _hue

    suspend fun init() {
        if (_hue.value == null) {
            withContext(Dispatchers.IO) {
                try {
                    _hue.postValue(Hue())
                } catch (error: Exception) {
                    Log.e("HUE", error.message ?: "An unknown error occurred.")
                }
            }
        } else {
            Log.i("HUE", "Shade already initialized")
        }
    }

    suspend fun selectBridge(bridge: Bridge) {
        withContext(Dispatchers.IO) {
            try {
                val shadeConfig = _hue.value?.shade?.configuration

                shadeConfig?.setHostname(bridge.localIp)
                shadeConfig?.setSecurityStrategy(
                    SecurityStrategy.Insecure(
                        hostname = bridge.localIp
                    )
                )

                Log.i("HUE", "Selected bridge: ${bridge.localIp}")
            } catch (error: Exception) {
                Log.e("HUE", error.message ?: "An unknown error occurred.")
            }
        }
    }
}
