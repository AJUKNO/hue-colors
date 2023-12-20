package nl.hva.huecolors.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import inkapplications.shade.auth.structures.AppId
import inkapplications.shade.core.Shade
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.AuthToken
import inkapplications.shade.structures.SecurityStrategy
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.BridgeInfo
import nl.hva.huecolors.repository.BridgeRepository
import nl.hva.huecolors.utils.Utils
import java.util.UUID
import kotlin.time.ExperimentalTime

class BridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "BRIDGE_MODEL"
    private val bridgeRepo = BridgeRepository(application.applicationContext)

    private val _shade = MutableLiveData<Resource<Shade?>>(Resource.Empty())
    val shade: LiveData<Resource<Shade?>>
        get() = _shade

    private val _bridgeDiscovery = MutableLiveData<Resource<List<Bridge>?>>(Resource.Empty())
    val bridgeDiscovery: LiveData<Resource<List<Bridge>?>>
        get() = _bridgeDiscovery

    private val _isBridgeAuthorized = MutableLiveData<Resource<Boolean?>>(Resource.Empty())
    val isBridgeAuthorized
        get() = _isBridgeAuthorized


    suspend fun initShade() {
        if (_shade.value != null) {
            try {
                _shade.value = Resource.Loading()

                if (isBridgeAuthorized()) {
                    val bridge = bridgeRepo.getCredentialsBridge()!!

                    _shade.value = Resource.Success(Shade(
                        hostname = bridge.hostname,
                        authToken = AuthToken(
                            bridge.appKey,
                            bridge.clientKey
                        ),
                        securityStrategy = SecurityStrategy.Insecure(bridge.hostname)
                    ))

                    Log.i(TAG, "Shade successfully initialized with credentials")
                } else {
                    _shade.value = Resource.Success(Shade())

                    Log.i(TAG, "Shade successfully initialized")
                }
            } catch (error: Exception) {
                Utils.handleError(TAG, error)
                _shade.value = Resource.Error(error.message ?: "An unknown error occurred.")
            }
        } else {
            Log.i(TAG, "Shade already initialized")
        }
    }

    suspend fun isBridgeAuthorized(): Boolean {
        return try {
            if (bridgeRepo.getCredentialsBridge() != null) {
                _isBridgeAuthorized.value = Resource.Success(true)
                true
            } else {
                false
            }
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
            false
        }
    }

    suspend fun discoverBridges() {
        try {
            _bridgeDiscovery.value = Resource.Loading()

            _bridgeDiscovery.value = Resource.Success(
                _shade.value?.data?.onlineDiscovery?.getDevices()
            )

            if (_bridgeDiscovery.value?.data != null) {
                Log.i(TAG, "${_bridgeDiscovery.value?.data?.size} bridge(s) found: ${_bridgeDiscovery.value?.data}")
            } else {
                Log.i(TAG, "No bridges found.")
            }
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
            _bridgeDiscovery.value = Resource.Error(error.message ?: "An unknown error occurred.")
        }
    }

    suspend fun selectBridge(bridge: Bridge) {
        try {
            _shade.value?.data?.configuration?.apply {
                setHostname(bridge.localIp)
                setSecurityStrategy(
                    SecurityStrategy.Insecure(
                        hostname = bridge.localIp
                    )
                )
            }

            val bridgeInfo = BridgeInfo(
                hostname = _shade.value?.data?.configuration?.hostname?.value!!,
                bridgeId = bridge.id.value,
                appKey = "",
                clientKey = "",
                port = 443
            )

            bridgeRepo.insertOrUpdate(bridgeInfo)

            Log.i(
                TAG, "Selected bridge: ${_shade.value?.data?.configuration?.hostname?.value}"
            )
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun authorizeBridge() {
        try {
            _isBridgeAuthorized.value = Resource.Loading()

            val authToken: AuthToken? = _shade.value?.data?.auth?.awaitToken(
                appId = AppId(
                    appName = "HueColors",
                    instanceName = "android"
                )
            )

            val bridgeBeingAuthorized = bridgeRepo.getBridge(_shade.value?.data?.configuration?.hostname?.value!!)

            if (bridgeBeingAuthorized != null && authToken != null) {
                bridgeRepo.insertOrUpdate(bridgeBeingAuthorized.copy(
                    appKey = authToken.applicationKey,
                    clientKey = authToken.clientKey!!
                ))
            }

            isBridgeAuthorized.value = Resource.Success()

            Log.i(TAG, "Successfully authorized bridge")
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
            _isBridgeAuthorized.value = Resource.Error(error.message ?: "An unknown error occurred.")
        }
    }
}
