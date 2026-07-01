package dev.bartuzen.qbitcontroller.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTorrentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingTorrentEntity): Long

    @Update
    suspend fun update(entity: PendingTorrentEntity)

    @Query(
        "SELECT * FROM pending_torrents WHERE serverId = :serverId AND status IN ('PENDING', 'ERROR') " +
            "ORDER BY addedAt ASC",
    )
    suspend fun getPendingByServerId(serverId: Int): List<PendingTorrentEntity>

    @Query("SELECT * FROM pending_torrents ORDER BY serverId ASC, addedAt DESC")
    fun getAllFlow(): Flow<List<PendingTorrentEntity>>

    @Query("SELECT COUNT(*) FROM pending_torrents WHERE status IN ('PENDING', 'ERROR', 'UPLOADING')")
    fun getPendingCountFlow(): Flow<Int>

    @Query("DELETE FROM pending_torrents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_torrents WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Int)

    @Query("DELETE FROM pending_torrents WHERE status IN ('SUCCESS', 'EXISTS')")
    suspend fun deleteCompleted()

    @Query("UPDATE pending_torrents SET status = 'UPLOADING', updatedAt = :now WHERE id = :id")
    suspend fun markUploading(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE pending_torrents SET status = 'SUCCESS', updatedAt = :now WHERE id = :id")
    suspend fun markSuccess(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE pending_torrents SET status = 'EXISTS', updatedAt = :now WHERE id = :id")
    suspend fun markExists(id: Long, now: Long = System.currentTimeMillis())

    @Query(
        "UPDATE pending_torrents SET status = 'FAILED', lastError = :error, updatedAt = :now WHERE id = :id",
    )
    suspend fun markFailed(id: Long, error: String, now: Long = System.currentTimeMillis())

    @Query(
        "UPDATE pending_torrents SET status = 'ERROR', retryCount = retryCount + 1, " +
            "lastError = :error, updatedAt = :now WHERE id = :id",
    )
    suspend fun markError(id: Long, error: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE pending_torrents SET status = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun resetToRetry(id: Long, now: Long = System.currentTimeMillis())

    @Query(
        "SELECT COUNT(*) FROM pending_torrents WHERE serverId = :serverId AND magnetUri = :magnetUri " +
            "AND status NOT IN ('SUCCESS', 'EXISTS', 'FAILED')",
    )
    suspend fun countPendingByMagnet(serverId: Int, magnetUri: String): Int

    @Query(
        "SELECT COUNT(*) FROM pending_torrents WHERE serverId = :serverId AND fileName = :fileName " +
            "AND status NOT IN ('SUCCESS', 'EXISTS', 'FAILED')",
    )
    suspend fun countPendingByFileName(serverId: Int, fileName: String): Int
}
