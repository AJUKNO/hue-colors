package nl.hva.huecolors.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bridge info class that holds data of a bridge
 *
 * @constructor Create Bridge info
 * @property hostname IP address
 * @property bridgeId Bridge ID
 * @property appKey Bridge application key
 * @property clientKey Bridge client key
 * @property port Port used to connect
 * @property id Primary identifier
 */
@Entity("bridge_info")
data class BridgeInfo(

    @ColumnInfo("hostname") var hostname: String,

    @ColumnInfo("bridge_id") var bridgeId: String,

    @ColumnInfo("app_key") var appKey: String,

    @ColumnInfo("client_key") var clientKey: String,

    @ColumnInfo("port") var port: Int,

    @PrimaryKey(true) @ColumnInfo("id") var id: Long = 0
)
