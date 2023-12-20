package nl.hva.huecolors.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.hva.huecolors.data.model.LightInfo

@Dao
interface LightDao {

    @Query("SELECT * FROM light_info")
    suspend fun getLights(): List<LightInfo>

    @Query("SELECT * FROM light_info WHERE is_hue = 1")
    suspend fun getHueLights(): List<LightInfo>

    @Query("SELECT * FROM light_info WHERE id = :id")
    suspend fun getLightById(id: String): LightInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(light: LightInfo)
}