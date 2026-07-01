package dev.bartuzen.qbitcontroller.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PendingTorrentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PendingTorrentDatabase : RoomDatabase() {
    abstract fun pendingTorrentDao(): PendingTorrentDao
}
