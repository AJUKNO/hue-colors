package nl.hva.huecolors.repository

import android.content.Context
import nl.hva.huecolors.data.model.BridgeInfo
import nl.hva.huecolors.db.BridgeDao
import nl.hva.huecolors.db.HueRoomDatabase

class BridgeRepository(context: Context) {

    private val bridgeDao: BridgeDao

    init {
        val database = HueRoomDatabase.getDatabase(context)
        bridgeDao = database!!.bridgeDao()
    }

    fun getBridges() = bridgeDao.getBridges()

    suspend fun getBridge(hostname: String) = bridgeDao.getBridgeByHostname(hostname)

    suspend fun insertOrUpdate(bridgeInfo: BridgeInfo) {
        val existing = bridgeDao.getBridgeByHostname(bridgeInfo.hostname)

        if (existing == null) {
            bridgeDao.insertOrUpdate(bridgeInfo)
        } else {
            val updated = existing.copy(
                hostname = bridgeInfo.hostname,
                bridgeId = bridgeInfo.bridgeId,
                appKey = bridgeInfo.appKey,
                clientKey = bridgeInfo.clientKey,
                port = bridgeInfo.port
            )

            bridgeDao.insertOrUpdate(updated)
        }
    }

    suspend fun getCredentialsBridge() = bridgeDao.getBridgeWithCredentials()
}