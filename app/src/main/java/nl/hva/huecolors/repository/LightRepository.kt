package nl.hva.huecolors.repository

import android.content.Context
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
                isHue = light.isHue
            )

            lightDao.insertOrUpdate(updated)
        }
    }
}