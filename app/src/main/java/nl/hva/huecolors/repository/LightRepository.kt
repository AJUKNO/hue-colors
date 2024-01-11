package nl.hva.huecolors.repository

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import nl.hva.huecolors.data.model.LightInfo
import nl.hva.huecolors.db.HueRoomDatabase
import nl.hva.huecolors.db.LightDao

class LightRepository(context: Context) {

    private val lightDao: LightDao

    init {
        val database = HueRoomDatabase.getDatabase(context)
        lightDao = database!!.lightDao()
    }

    suspend fun getLights() = lightDao.getLights()

    suspend fun getHueLights() = lightDao.getHueLights()

    suspend fun getLight(id: String) = lightDao.getLightById(id)

    suspend fun insertOrUpdate(light: LightInfo) {
        val existing = lightDao.getLightById(light.id)

        if (existing == null) {
            lightDao.insertOrUpdate(light)
        } else {
            val updated = existing.copy(
                color = light.color,
                label = light.label,
                owner = light.owner,
                power = light.power,
                v1Id = light.v1Id,
                id = light.id,
                isHue = light.isHue,
                brightness = light.brightness
            )

            lightDao.insertOrUpdate(updated)
        }
    }

    suspend fun insertOrUpdateAll(updatedLights: List<LightInfo>) {
        coroutineScope {
            val deferredList = updatedLights.map { light ->
                async {
                    insertOrUpdate(light)
                }
            }

            deferredList.awaitAll()
        }
    }
}