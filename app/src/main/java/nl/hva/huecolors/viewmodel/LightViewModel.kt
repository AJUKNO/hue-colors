package nl.hva.huecolors.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.github.ajalt.colormath.extensions.android.composecolor.toColormathColor
import inkapplications.shade.core.Shade
import inkapplications.shade.lights.parameters.ColorParameters
import inkapplications.shade.lights.parameters.DimmingParameters
import inkapplications.shade.lights.parameters.LightUpdateParameters
import inkapplications.shade.lights.structures.Light
import inkapplications.shade.structures.AuthToken
import inkapplications.shade.structures.ResourceId
import inkapplications.shade.structures.SecurityStrategy
import inkapplications.shade.structures.parameters.PowerParameters
import inkapplications.spondee.scalar.percent
import inkapplications.spondee.scalar.toWholePercentage
import inkapplications.spondee.structure.toFloat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.hva.huecolors.R
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.LightInfo
import nl.hva.huecolors.repository.BridgeRepository
import nl.hva.huecolors.repository.LightRepository
import nl.hva.huecolors.utils.Utils

class LightViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val TAG = context.getString(R.string.lights_model)
    private val lightRepo = LightRepository(application.applicationContext)
    private val bridgeRepo = BridgeRepository(application.applicationContext)

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private val _shade = MutableLiveData<Resource<Shade?>>(Resource.Empty())
    val shade: LiveData<Resource<Shade?>> = _shade

    private val _isBridgeAuthorized = MutableLiveData<Resource<Boolean?>>(Resource.Empty())
    val isBridgeAuthorized: LiveData<Resource<Boolean?>> = _isBridgeAuthorized

    private val _lights = MutableLiveData<Resource<List<LightInfo>?>>(Resource.Empty())
    val lights: LiveData<Resource<List<LightInfo>?>> = _lights

    private val _images = MutableLiveData<Resource<List<Uri>?>>(Resource.Empty())
    val images: LiveData<Resource<List<Uri>?>> = _images

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initShade()
        }
    }

    /** Initialize Shade object */
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
     * Check if bridge is authorized by checking if a bridge with credentials
     * is present in the database
     *
     * @return Boolean
     */
    private suspend fun isBridgeAuthorized(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val isAuthorized = bridgeRepo.getCredentialsBridge() != null
                _isBridgeAuthorized.postValue(Resource.Success(isAuthorized))
                isAuthorized
            }
        } catch (error: Exception) {
            _isBridgeAuthorized.postValue(
                Resource.Error(
                    error.message ?: context.getString(R.string.an_unknown_error_occurred)
                )
            )
            handleError(error)
            false
        }
    }


    /** Get lights. Loads an array of lights as LiveData in _lights.value */
    suspend fun getLights() {
        try {
            withContext(Dispatchers.IO) {
                _lights.postValue(Resource.Loading())

                val roomLights = lightRepo.getLights()

                if (roomLights.isEmpty()) {
                    updateLightsWithHue()
                } else {
                    updateRoomLights(roomLights)
                }

                _lights.postValue(Resource.Success(lightRepo.getLights()))

                showToast(context.getString(R.string.loaded_lights))
            }
        } catch (error: Exception) {
            _lights.postValue(
                Resource.Error(
                    error.message ?: context.getString(R.string.an_unknown_error_occurred)
                )
            )
            handleError(error)
        }
    }

    private suspend fun updateRoomLights(roomLights: List<LightInfo>) {
        coroutineScope {
            val deferredUpdates = roomLights.map { light ->
                async {
                    val hueLight = getHueLightById(light.id)
                    val (color, powerState) = getLightColorAndPower(hueLight)

                    lightRepo.insertOrUpdate(light.copy(color = color, power = powerState))
                }
            }

            deferredUpdates.awaitAll()
        }
    }

    private suspend fun updateLightsWithHue() {
        _shade.value?.data?.lights?.listLights()?.let { hueLights ->
            coroutineScope {
                val deferredInserts = hueLights.map { light ->
                    async {
                        val (color, powerState) = getLightColorAndPower(light)

                        lightRepo.insertOrUpdate(
                            LightInfo(
                                color = color,
                                id = light.id.value,
                                label = "Lamp ${light.v1Id?.split("/")?.get(2) ?: "Unknown"}",
                                owner = light.owner.id.value,
                                power = powerState,
                                v1Id = light.v1Id ?: "Lamp",
                                isHue = light.colorInfo != null,
                                brightness = light.dimmingInfo?.brightness?.toWholePercentage()
                                    ?.toFloat() ?: 0F
                            )
                        )
                    }
                }

                deferredInserts.awaitAll()
            }
        }
    }

    private fun getLightColorAndPower(light: Light?): Pair<Int?, Boolean> {
        return light?.let {
            val color =
                it.colorInfo?.color?.toSRGB()?.toHex(true)?.let { hex ->
                    Color(android.graphics.Color.parseColor(hex))
                }?.toArgb()
            val powerState = it.powerInfo.on

            color to powerState
        } ?: (null to false)
    }

    private suspend fun getHueLightById(id: String): Light? {
        return _shade.value?.data?.lights?.getLight(ResourceId(id))
    }

    /**
     * Toggle power state of a light
     *
     * @param id Identifier of a light
     * @param power Boolean state of the light
     */
    suspend fun toggleLight(id: String, power: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                shade.value?.data?.lights?.updateLight(
                    id = ResourceId(id), parameters = LightUpdateParameters(
                        power = PowerParameters(power)
                    )
                )
                lightRepo.getLight(id)?.let { light ->
                    lightRepo.insertOrUpdate(light.copy(power = power))
                }
            }
        } catch (error: Exception) {
            handleError(error)
        }
    }

    suspend fun setBrightness(brightness: Float, lightId: String) {
        try {
            withContext(Dispatchers.IO) {
                lightRepo.getLight(lightId)?.let { light ->
                    if (light.power) {
                        _shade.value?.data?.lights?.updateLight(
                            id = ResourceId(lightId),
                            parameters = LightUpdateParameters(
                                dimming = DimmingParameters(
                                    brightness = brightness.percent
                                )
                            )
                        )
                    }

                    lightRepo.insertOrUpdate(light.copy(brightness = brightness))
                }
            }
        } catch (error: Exception) {
            handleError(error)
        }
    }

    /**
     * Identify light by triggering a visual identification sequence
     *
     * @param id Identifier of a light
     */
    suspend fun identifyLight(id: String) {
        try {
            withContext(Dispatchers.IO) {
                shade.value?.data?.devices?.listDevices()?.find { it.v1Id == id }?.let { device ->
                    shade.value?.data?.devices?.identifyDevice(deviceId = device.id)
                }
            }
        } catch (error: Exception) {
            handleError(error)
        }
    }

    /**
     * Get images from media folder
     *
     * @param context
     */
    fun getImagesFromMedia(context: Context) {
        viewModelScope.launch {
            try {
                _images.postValue(Resource.Loading())

                val images = withContext(Dispatchers.IO) {
                    val imagesList = mutableListOf<Uri>()
                    val picturesFolderPath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/palette").path

                    val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
                    val selectionArgs = arrayOf("$picturesFolderPath%")

                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Images.Media._ID),
                        selection,
                        selectionArgs,
                        null
                    )?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                            )
                            imagesList.add(contentUri)
                        }
                    }

                    imagesList
                }

                _images.postValue(Resource.Success(images))
            } catch (error: Exception) {
                _images.postValue(
                    Resource.Error(
                        error.message ?: context.getString(R.string.an_unknown_error_occurred)
                    )
                )
                handleError(error)
            }
        }
    }

    /** Clear images LiveData to free resources */
    fun clearImages() {
        _images.postValue(Resource.Empty())
    }

    /**
     * Function to apply the palette to the lights
     *
     * @param palette
     */
    suspend fun paletteToLights(palette: Palette?) {
        try {
            // TODO: Fix bug, lights not updating correctly concurrently
//            val roomLights = lightRepo.getHueLights()
//
//            if (palette != null) {
//                val swatches = palette.swatches
//
//                coroutineScope {
//                    val deferredList = roomLights.mapIndexed { index, light ->
//                        async {
//                            val swatchIndex = index % swatches.size
//                            val swatchColor = swatches[swatchIndex].rgb
//
//                            shade.value?.data?.lights?.updateLight(
//                                id = ResourceId(light.id),
//                                parameters = LightUpdateParameters(
//                                    color = ColorParameters(
//                                        color = Color(swatchColor).toColormathColor()
//                                    ),
//                                    power = PowerParameters(
//                                        on = true
//                                    )
//                                )
//                            )
//
//                            light.copy(color = swatchColor, power = true)
//                        }
//                    }
//
//                    val updatedLights = deferredList.awaitAll()
//                    lightRepo.insertOrUpdateAll(updatedLights)
//                }
//            }
            withContext(Dispatchers.IO) {
                val roomLights = lightRepo.getHueLights()

                if (palette != null) {
                    val swatches = palette.swatches

                    for ((index, light) in roomLights.withIndex()) {
                        val swatchIndex = index % swatches.size

                        val swatchColor = swatches[swatchIndex].rgb

                        shade.value?.data?.lights?.updateLight(
                            id = ResourceId(light.id), parameters = LightUpdateParameters(
                                color = ColorParameters(
                                    color = Color(swatchColor).toColormathColor()
                                ), power = PowerParameters(
                                    on = true
                                )
                            )
                        )
                        Log.i(TAG, light.v1Id)

                        lightRepo.insertOrUpdate(light.copy(color = swatchColor, power = true))
                    }

                    showToast(context.getString(R.string.applied_swatches, palette.swatches.size))
                }
            }
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