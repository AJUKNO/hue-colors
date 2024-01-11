package nl.hva.huecolors.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Light info class that holds data of a light
 *
 * @constructor Create Light info
 * @property color Color saved as Int using .toArgb()
 * @property label Name of light
 * @property owner Owner of the device
 * @property isHue Boolean that decides whether the light is o Hue product
 * @property power Power state
 * @property v1Id Deprecated light identifier
 * @property id Primary identifier
 */
@Entity("light_info")
data class LightInfo(

    @ColumnInfo("color") var color: Int? = null,

    @ColumnInfo("label") var label: String,

    @ColumnInfo("owner") var owner: String,

    @ColumnInfo("is_hue") var isHue: Boolean,

    @ColumnInfo("power") var power: Boolean,

    @ColumnInfo("brightness") var brightness: Float,

    @ColumnInfo("v1_id") var v1Id: String,

    @ColumnInfo("id") @PrimaryKey var id: String
)
