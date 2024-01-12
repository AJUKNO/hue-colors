package nl.hva.huecolors.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import inkapplications.shade.auth.structures.AppId
import inkapplications.shade.core.Shade
import inkapplications.shade.devices.structures.Device
import inkapplications.shade.discover.structures.Bridge
import inkapplications.shade.structures.AuthToken
import inkapplications.shade.structures.SecurityStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.BridgeInfo
import nl.hva.huecolors.repository.BridgeRepository
import nl.hva.huecolors.utils.Utils
import nl.hva.huecolors.utils.Utils.Companion.handleError
import kotlin.time.ExperimentalTime

class BridgeViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val TAG = context.getString(R.string.bridge_model)
    private var bridgeRepo = BridgeRepository(application.applicationContext)

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private val _shade = MutableLiveData<Resource<Shade?>>(Resource.Empty())
    val shade: LiveData<Resource<Shade?>>
        get() = _shade

    private val _bridgeDiscovery = MutableLiveData<Resource<List<Bridge>?>>(Resource.Empty())
    val bridgeDiscovery: LiveData<Resource<List<Bridge>?>>
        get() = _bridgeDiscovery

    private val _isBridgeAuthorized = MutableLiveData<Resource<Boolean?>>(Resource.Empty())
    val isBridgeAuthorized
        get() = _isBridgeAuthorized

    private val _devices = MutableLiveData<Resource<List<Device>?>>(Resource.Empty())
    val devices
        get() = _devices

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initShade()
        }
    }

    /** Initialize shade */
    private suspend fun initShade() {
        _shade.value?.data?.let {
            Log.i(TAG, context.getString(R.string.shade_already_initialized))
            return
        }

        try {
            withContext(Dispatchers.IO) {
                _shade.postValue(Resource.Loading())

                val shadeData = if (isBridgeAuthorized()) {
                    val bridge = bridgeRepo.getCredentialsBridge()!!
                    Log.i(TAG,
                        context.getString(R.string.shade_successfully_initialized_with_credentials))
                    Shade(
                        hostname = bridge.hostname,
                        authToken = AuthToken(
                            bridge.appKey, bridge.clientKey
                        ),
                        securityStrategy = SecurityStrategy.Insecure(bridge.hostname)
                    )
                } else {
                    Log.i(TAG, context.getString(R.string.shade_successfully_initialized))
                    Shade()
                }

                _shade.postValue(Resource.Success(shadeData))
            }
        } catch (error: Exception) {
            _shade.postValue(
                Resource.Error(
                    error.message ?: context.getString(R.string.an_unknown_error_occurred)
                )
            )
            handleError(error)
        }
    }

    /**
     * Check if bridge is authorized by checking if a bridge with credentials is present in the database
     *
     * @return Boolean
     */
    suspend fun isBridgeAuthorized(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val isAuthorized = bridgeRepo.getCredentialsBridge() != null
                _isBridgeAuthorized.postValue(Resource.Success(isAuthorized))
                isAuthorized
            }
        } catch (error: Exception) {
            handleError(error)
            _isBridgeAuthorized.postValue(Resource.Error(error.message ?: "An unknown error occurred."))
            false
        }
    }

    /** Discover bridges, calls the getDevices() function to search for any bridges in the network */
    suspend fun discoverBridges() {
        try {
            _bridgeDiscovery.postValue(Resource.Loading())

            val discoveredDevices = _shade.value?.data?.onlineDiscovery?.getDevices()

            _bridgeDiscovery.postValue(Resource.Success(discoveredDevices))

            discoveredDevices?.let {
                if (it.isNotEmpty()) {
                    Log.i(TAG, "${it.size} bridge(s) found: $it")
                } else {
                    Log.i(TAG, "No bridges found.")
                }
            }
        } catch (error: Exception) {
            handleError(error)
            _bridgeDiscovery.postValue(Resource.Error(error.message ?: "An unknown error occurred."))
        }
    }

    /**
     * Select bridge by saving the selected bridge to the database and setting the Shade configuration
     *
     * @param bridge
     */
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
                hostname = _shade.value?.data?.configuration?.hostname?.value.orEmpty(),
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
            handleError(error)
        }
    }

    /** Function to authorize a bridge.
     * User must press the physical authorize button on the bridge to authorize.
     * The authorized bridge will update in the database with credentials */
    @OptIn(ExperimentalTime::class)
    suspend fun authorizeBridge() {
        try {
            _isBridgeAuthorized.postValue(Resource.Loading())

            val authToken: AuthToken? = _shade.value?.data?.auth?.awaitToken(
                appId = AppId(
                    appName = "HueColors",
                    instanceName = "android"
                )
            )

            _shade.value?.data?.configuration?.hostname?.value?.let { hostname ->
                val bridgeBeingAuthorized = bridgeRepo.getBridge(hostname)

                if (bridgeBeingAuthorized != null && authToken != null) {
                    bridgeRepo.insertOrUpdate(
                        bridgeBeingAuthorized.copy(
                            appKey = authToken.applicationKey,
                            clientKey = authToken.clientKey ?: ""
                        )
                    )
                }
            }

            _isBridgeAuthorized.postValue(Resource.Success(true))

            Log.i(TAG, "Successfully authorized bridge")
        } catch (error: Exception) {
            handleError(error)
            _isBridgeAuthorized.postValue(Resource.Error(error.message ?: "An unknown error occurred."))
        }
    }

    suspend fun getGroupedDevices() {
        try {
            val list = _shade.value?.data?.devices?.listDevices()
            _devices.postValue(Resource.Success(list))
        } catch (error: Exception) {
            handleError(error)
        }
    }

    private fun showToast(message: String) {
        _toastMessage.postValue(message)
    }

    private fun handleError(error: Exception) {
        Utils.handleError(TAG, error)
        val errorMessage = error.message ?: context.getString(R.string.an_unknown_error_occurred)
        showToast(errorMessage)
    }
}
