package com.topdon.lib.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.db.dao.HouseDetectDao
import com.topdon.lib.core.db.dao.HouseReportDao
import com.topdon.lib.core.db.dao.ThermalDao
import com.topdon.lib.core.db.entity.*

@Database(
    entities = [
        ThermalEntity::class,
        HouseDetect::class,
        HouseReport::class,
        DirDetect::class,
        DirReport::class,
        ItemDetect::class,
        ItemReport::class,
    ], version = 5
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun thermalDao(): ThermalDao

    abstract fun houseDetectDao(): HouseDetectDao

    abstract fun houseReportDao(): HouseReportDao




    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context = Utils.getApp()): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "TopInfrared.db")
                .addMigrations(object : Migration(4, 5) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("DROP TABLE file")
                        database.execSQL("DROP TABLE tc001_file")
                        database.execSQL("DROP TABLE thermal_minute")
                        database.execSQL("DROP TABLE thermal_hour")
                        database.execSQL("DROP TABLE thermal_day")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `thermal` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `thermal_id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `thermal` REAL NOT NULL, `thermal_max` REAL NOT NULL, `thermal_min` REAL NOT NULL, `sn` TEXT NOT NULL, `info` TEXT NOT NULL, `type` TEXT NOT NULL, `start_time` INTEGER NOT NULL, `create_time` INTEGER NOT NULL, `update_time` INTEGER NOT NULL)")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `HouseDetect` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `inspectorName` TEXT NOT NULL, `address` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `year` INTEGER, `houseSpace` TEXT NOT NULL, `houseSpaceUnit` INTEGER NOT NULL, `cost` TEXT NOT NULL, `costUnit` INTEGER NOT NULL, `detectTime` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL)")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `HouseReport` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `inspectorName` TEXT NOT NULL, `address` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `year` INTEGER, `houseSpace` TEXT NOT NULL, `houseSpaceUnit` INTEGER NOT NULL, `cost` TEXT NOT NULL, `costUnit` INTEGER NOT NULL, `detectTime` INTEGER NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL, `inspectorWhitePath` TEXT NOT NULL, `inspectorBlackPath` TEXT NOT NULL, `houseOwnerWhitePath` TEXT NOT NULL, `houseOwnerBlackPath` TEXT NOT NULL)")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `DirDetect` (`parentId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `position` INTEGER NOT NULL, `dirName` TEXT NOT NULL, `goodCount` INTEGER NOT NULL, `warnCount` INTEGER NOT NULL, `dangerCount` INTEGER NOT NULL, FOREIGN KEY(`parentId`) REFERENCES `HouseDetect`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `DirReport` (`parentId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `position` INTEGER NOT NULL, `dirName` TEXT NOT NULL, `goodCount` INTEGER NOT NULL, `warnCount` INTEGER NOT NULL, `dangerCount` INTEGER NOT NULL, FOREIGN KEY(`parentId`) REFERENCES `HouseReport`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `ItemDetect` (`parentId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `position` INTEGER NOT NULL, `itemName` TEXT NOT NULL, `state` INTEGER NOT NULL, `inputText` TEXT NOT NULL, `image1` TEXT NOT NULL, `image2` TEXT NOT NULL, `image3` TEXT NOT NULL, `image4` TEXT NOT NULL, FOREIGN KEY(`parentId`) REFERENCES `DirDetect`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
                        database.execSQL("CREATE TABLE IF NOT EXISTS `ItemReport` (`parentId` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `position` INTEGER NOT NULL, `itemName` TEXT NOT NULL, `state` INTEGER NOT NULL, `inputText` TEXT NOT NULL, `image1` TEXT NOT NULL, `image2` TEXT NOT NULL, `image3` TEXT NOT NULL, `image4` TEXT NOT NULL, FOREIGN KEY(`parentId`) REFERENCES `DirReport`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_DirDetect_parentId` ON `DirDetect` (`parentId`)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_DirReport_parentId` ON `DirReport` (`parentId`)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_ItemDetect_parentId` ON `ItemDetect` (`parentId`)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS `index_ItemReport_parentId` ON `ItemReport` (`parentId`)")
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
    }
}