package dev.bartuzen.qbitcontroller.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_torrents")
data class PendingTorrentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverId: Int,
    val type: String,
    val magnetUri: String? = null,
    val fileName: String? = null,
    val fileDataBase64: String? = null,
    val savePath: String? = null,
    val category: String? = null,
    val tags: String = "",
    val stopCondition: String? = null,
    val contentLayout: String? = null,
    val torrentName: String? = null,
    val downloadSpeedLimit: Int? = null,
    val uploadSpeedLimit: Int? = null,
    val ratioLimit: Double? = null,
    val seedingTimeLimit: Int? = null,
    val isPaused: Boolean = false,
    val skipHashChecking: Boolean = false,
    val isAutoTorrentManagementEnabled: Boolean? = null,
    val isSequentialDownloadEnabled: Boolean = false,
    val isFirstLastPiecePrioritized: Boolean = false,
    val status: String = STATUS_PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE_MAGNET = "MAGNET"
        const val TYPE_FILE = "FILE"

        const val STATUS_PENDING = "PENDING"
        const val STATUS_UPLOADING = "UPLOADING"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_EXISTS = "EXISTS"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_ERROR = "ERROR"
    }
}
