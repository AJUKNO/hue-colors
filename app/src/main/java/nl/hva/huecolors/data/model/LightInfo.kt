package nl.hva.huecolors.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("light_info")
data class LightInfo(

    @ColumnInfo("color")
    var color: Int? = null,

    @ColumnInfo("label")
    var label: String,

    @ColumnInfo("owner")
    var owner: String,

    @ColumnInfo("is_hue")
    var isHue: Boolean,

    @ColumnInfo("power")
    var power: Boolean,

    @ColumnInfo("v1_id")
    var v1Id: String,

    @ColumnInfo("id")
    @PrimaryKey
    var id: String
)
