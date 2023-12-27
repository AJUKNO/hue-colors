package nl.hva.huecolors.viewmodel

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
import inkapplications.shade.lights.parameters.LightUpdateParameters
import inkapplications.shade.lights.structures.Light
import inkapplications.shade.structures.AuthToken
import inkapplications.shade.structures.ResourceId
import inkapplications.shade.structures.SecurityStrategy
import inkapplications.shade.structures.parameters.PowerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.hva.huecolors.data.Resource
import nl.hva.huecolors.data.model.LightInfo
import nl.hva.huecolors.repository.BridgeRepository
import nl.hva.huecolors.repository.LightRepository
import nl.hva.huecolors.utils.Utils

class LightViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "LIGHTS_MODEL"
    private val lightRepo = LightRepository(application.applicationContext)
    private val bridgeRepo = BridgeRepository(application.applicationContext)

    private val _shade = MutableLiveData<Resource<Shade?>>(Resource.Empty())
    val shade: LiveData<Resource<Shade?>>
        get() = _shade

    private val _isBridgeAuthorized = MutableLiveData<Resource<Boolean?>>(Resource.Empty())
    val isBridgeAuthorized
        get() = _isBridgeAuthorized

    private var _lights = MutableLiveData<Resource<List<LightInfo>?>>(Resource.Empty())

    val lights: LiveData<Resource<List<LightInfo>?>>
        get() = _lights

    private var _images = MutableLiveData<Resource<List<Uri>?>>(Resource.Empty())
    val images: LiveData<Resource<List<Uri>?>>
        get() = _images

    suspend fun initShade() {
        if (_shade.value != null) {
            try {
                _shade.value = Resource.Loading()

                if (isBridgeAuthorized()) {
                    val bridge = bridgeRepo.getCredentialsBridge()!!

                    _shade.value = Resource.Success(
                        Shade(
                            hostname = bridge.hostname, authToken = AuthToken(
                                bridge.appKey, bridge.clientKey
                            ), securityStrategy = SecurityStrategy.Insecure(bridge.hostname)
                        )
                    )

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
                _isBridgeAuthorized.value = Resource.Success(false)
                false
            }
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
            _isBridgeAuthorized.value =
                Resource.Error(error.message ?: "An unknown error occurred.")
            false
        }
    }

    suspend fun getLights() {
        try {
            _lights.value = Resource.Loading()

            val roomLights = lightRepo.getLights()

            coroutineScope {
                if (roomLights.isEmpty()) {
                    val hueLights = _shade.value?.data?.lights?.listLights()

                    hueLights?.let {
                        val deferredInserts = hueLights.map { light ->
                            async {
                                lightRepo.insertOrUpdate(
                                    LightInfo(
                                        color = light.colorInfo?.color?.toSRGB()?.toHex(true)
                                            ?.let { Color(android.graphics.Color.parseColor(it)) }?.toArgb(),
                                        id = light.id.value,
                                        label = "Lamp ${light.v1Id.toString().split("/")[2]}",
                                        owner = light.owner.id.value,
                                        power = light.powerInfo.on,
                                        v1Id = light.v1Id ?: "Lamp",
                                        isHue = light.colorInfo != null
                                    )
                                )
                            }
                        }
                        deferredInserts.awaitAll()
                    }
                }

                val deferredUpdates = roomLights.map { light ->
                    async {
                        val hueLight = _shade.value?.data?.lights?.getLight(ResourceId(light.id))
                        val color =
                            hueLight?.colorInfo?.color?.toSRGB()?.toHex(true)?.let {
                                Color(android.graphics.Color.parseColor(it))
                            }?.toArgb()
                        val powerState = hueLight?.powerInfo?.on ?: false

                        lightRepo.insertOrUpdate(light.copy(color = color, power = powerState))
                    }
                }
                deferredUpdates.awaitAll()
            }

            _lights.value = Resource.Success(lightRepo.getLights())
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
            _lights.value = Resource.Error(error.message ?: "An unknown error occurred.")
        }
    }

    suspend fun toggleLight(id: String, power: Boolean) {
        try {
            shade.value?.data?.lights?.updateLight(
                id = ResourceId(id), parameters = LightUpdateParameters(
                    power = PowerParameters(power)
                )
            )
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
        }
    }

    suspend fun identifyLight(id: String) {
        try {
            val device =
                shade.value?.data?.devices?.listDevices()?.find { device -> device.v1Id == id }

            if (device != null) {
                shade.value?.data?.devices?.identifyDevice(
                    deviceId = device.id
                )
            }
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
        }
    }

    fun getImagesFromMedia(context: Context) {
        viewModelScope.launch {
            try {
                _images.value = Resource.Loading()

                val images = withContext(Dispatchers.IO) {
                    val imagesList = mutableListOf<Uri>()

                    val projection = arrayOf(
                        MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME
                    )
                    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                    val picturesFolderPath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/palette").path

                    val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
                    val selectionArgs = arrayOf("$picturesFolderPath%")

                    context.contentResolver.query(
                        uri, projection, selection, selectionArgs, null
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

                _images.value = Resource.Success(images)
            } catch (error: Exception) {
                Utils.handleError(TAG, error)
                _images.value = Resource.Error(error.message ?: "An unknown error occurred.")
            }
        }
    }

    fun clearImages() {
        _images.value = Resource.Empty()
    }

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
            val roomLights = lightRepo.getHueLights()

            if (palette != null) {
                val swatches = palette.swatches

                for ((index, light) in roomLights.withIndex()) {
                    val swatchIndex = index % swatches.size

                    val swatchColor = swatches[swatchIndex].rgb

                    shade?.value?.data?.lights?.updateLight(
                        id = ResourceId(light.id), parameters = LightUpdateParameters(
                            color = ColorParameters(
                                color = Color(swatchColor).toColormathColor()
                            ), power = PowerParameters(
                                on = true
                            )
                        )
                    )

                    lightRepo.insertOrUpdate(light.copy(color = swatchColor, power = true))
                }
            }
        } catch (error: Exception) {
            Utils.handleError(TAG, error)
        }
    }
}