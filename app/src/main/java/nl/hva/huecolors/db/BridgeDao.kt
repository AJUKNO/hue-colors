package nl.hva.huecolors.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.hva.huecolors.data.model.BridgeInfo

@Dao
interface BridgeDao {

    @Query("SELECT * FROM bridge_info")
    fun getBridges(): LiveData<List<BridgeInfo>>

    @Query("SELECT * FROM bridge_info WHERE hostname = :hostname")
    suspend fun getBridgeByHostname(hostname: String): BridgeInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(hue: BridgeInfo)

    @Query("SELECT * FROM bridge_info WHERE app_key IS NOT NULL AND client_key IS NOT NULL")
    suspend fun getBridgeWithCredentials(): BridgeInfo?
}