package nl.hva.huecolors.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import inkapplications.shade.auth.structures.AppId
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.SecurityStrategy
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.Hue
import kotlin.time.ExperimentalTime

class HueViewModel(application: Application) : AndroidViewModel(application) {

    private var _hue: MutableLiveData<Resource<Hue>> = MutableLiveData()
    val hue: LiveData<Resource<Hue>>
        get() = _hue

    fun init() {
        if (_hue.value == null) {
            _hue.value = Resource.Loading()
            try {
                _hue.value = Resource.Success(Hue())
            } catch (error: Exception) {
                handleHueError(error)
                _hue.value = Resource.Error(error.message ?: "An unknown error occurred.")
            } finally {
                Log.i("HUE", "Shade successfully initialized")
            }
        } else {
            Log.i("HUE", "Shade already initialized")
        }
    }

    suspend fun selectBridge(bridge: Bridge) {
        try {
            _hue.value?.data?.shade?.value?.data?.configuration?.apply {
                setHostname(bridge.localIp)
                setSecurityStrategy(
                    SecurityStrategy.Insecure(
                        hostname = bridge.localIp
                    )
                )
            }
        } catch (error: Exception) {
            handleHueError(error)
        } finally {
            Log.i(
                "HUE",
                "Selected bridge: ${_hue.value?.data?.shade?.value?.data?.configuration?.hostname?.value}"
            )
        }
    }

    suspend fun searchBridges() {
        _hue.value?.data?.bridges?.value = Resource.Loading()

        try {
            _hue.value?.data?.bridges?.value =
                Resource.Success(_hue.value?.data?.shade?.value?.data?.onlineDiscovery?.getDevices())
        } catch (error: Exception) {
            handleHueError(error)
            _hue.value?.data?.bridges?.value =
                Resource.Error(
                    error.message ?: "An unknown error occurred."
                )
        } finally {
            logBridgesInfo()
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun authorizeBridge() {
        _hue.value?.data?.token?.value = Resource.Loading()

        try {
            _hue.value?.data?.token?.value = (Resource.Success(
                _hue.value?.data?.shade?.value?.data?.auth?.awaitToken(
                    appId = AppId(
                        appName = "HueColors", instanceName = "android"
                    )
                )
            ))
        } catch (error: Exception) {
            handleHueError(error)
            _hue.value?.data?.token?.value =
                Resource.Error(
                    error.message ?: "An unknown error occurred."
                )

        } finally {
            logTokenInfo()
        }
    }

    private fun logBridgesInfo() {
        with(_hue.value?.data?.bridges?.value?.data) {
            Log.i("HUE", "${this?.size ?: 0} bridges found: $this")
        }
    }

    private fun logTokenInfo() {
        with(_hue.value?.data?.token?.value?.data) {
            Log.i("HUE", "Application key: ${this?.applicationKey ?: "N/A"}")
            Log.i("HUE", "Client key: ${this?.clientKey ?: "N/A"}")
        }
    }

    private fun handleHueError(error: Exception) {
        Log.e("HUE", error.message ?: "An unknown error occurred.")
    }
}
