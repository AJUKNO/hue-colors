package nl.hva.huecolors.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import nl.hva.huecolors.data.model.BridgeInfo
import nl.hva.huecolors.data.model.LightInfo

@Database(
    entities = [BridgeInfo::class, LightInfo::class], version = 1, exportSchema = false
)
abstract class HueRoomDatabase : RoomDatabase() {

    abstract fun bridgeDao(): BridgeDao
    abstract fun lightDao(): LightDao

    companion object {
        private const val DATABASE_NAME = "HUE_DATABASE"

        @Volatile
        private var INSTANCE: HueRoomDatabase? = null

        fun getDatabase(context: Context): HueRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(HueRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext, HueRoomDatabase::class.java, DATABASE_NAME
                        ).fallbackToDestructiveMigration().build()
                    }
                }
            }

            INSTANCE?.openHelper?.writableDatabase

            return INSTANCE
        }
    }
}