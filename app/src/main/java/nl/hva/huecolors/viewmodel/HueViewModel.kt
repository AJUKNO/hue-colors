package nl.hva.huecolors.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.SecurityStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.hva.huecolors.data.Status
import nl.hva.huecolors.data.model.Hue

class HueViewModel(application: Application) : AndroidViewModel(application) {

    private var _hue: MutableLiveData<Hue> = MutableLiveData()
    val hue: LiveData<Hue>
        get() = _hue

    private var _status: MutableLiveData<Status<Any>> = MutableLiveData(Status.Empty())
    val status: LiveData<Status<Any>>
        get() = _status

    suspend fun init() {
        _status.postValue(Status.Loading())

        if (_hue.value == null) {
            withContext(Dispatchers.IO) {
                try {
                    _hue.postValue(Hue())
                } catch (error: Exception) {
                    _status.postValue(Status.Error(error.message ?: "An unknown error occurred."))
                    Log.e("HUE", error.message ?: "An unknown error occurred.")
                } finally {
                    _status.postValue(Status.Success())
                    Log.i("HUE", "Shade successfully initialized")
                }
            }
        } else {
            Log.i("HUE", "Shade already initialized")
        }

        _status.postValue(Status.Empty())
    }

    suspend fun selectBridge(bridge: Bridge) {
        _status.postValue(Status.Loading())

        withContext(Dispatchers.IO) {
            try {
                val shadeConfig = _hue.value?.shade?.configuration

                shadeConfig?.setHostname(bridge.localIp)
                shadeConfig?.setSecurityStrategy(
                    SecurityStrategy.Insecure(
                        hostname = bridge.localIp
                    )
                )
            } catch (error: Exception) {
                _status.postValue(Status.Error(error.message ?: "An unknown error occurred."))
                Log.e("HUE", error.message ?: "An unknown error occurred.")
            } finally {
                _status.postValue(Status.Success())
                Log.i("HUE", "Selected bridge: ${bridge.localIp}")
            }
        }
    }

    suspend fun searchBridges() {
        _status.postValue(Status.Loading())

        withContext(Dispatchers.IO) {
            try {
                val hue = _hue.value
                hue?.bridges = hue?.shade?.onlineDiscovery?.getDevices()

            } catch (error: Exception) {
                _status.postValue(Status.Error(error.message ?: "An unknown error occurred."))
                Log.e("HUE", error.message ?: "An unknown error occurred.")
            } finally {
                val hue = _hue.value
                _status.postValue(Status.Success())
                Log.i("BRIDGES", "${hue?.bridges?.size} Bridges found: ${hue?.bridges}")
            }
        }
    }
}
