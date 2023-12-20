package nl.hva.huecolors.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import inkapplications.shade.discover.structures.BridgeId

@Entity("bridge_info")
data class BridgeInfo(

    @ColumnInfo("hostname")
    var hostname: String,

    @ColumnInfo("bridge_id")
    var bridgeId: String,

    @ColumnInfo("app_key")
    var appKey: String,

    @ColumnInfo("client_key")
    var clientKey: String,

    @ColumnInfo("port")
    var port: Int,

    @PrimaryKey(true)
    @ColumnInfo("id")
    var id: Long = 0
)
