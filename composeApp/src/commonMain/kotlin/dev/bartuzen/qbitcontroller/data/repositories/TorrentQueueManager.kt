package dev.bartuzen.qbitcontroller.data.repositories

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface TorrentQueueManager {
    val pendingCountFlow: Flow<Int>

    suspend fun enqueueMagnet(
        serverId: Int,
        magnetUri: String,
        savePath: String?,
        category: String?,
        tags: List<String>,
        stopCondition: String?,
        contentLayout: String?,
        torrentName: String?,
        downloadSpeedLimit: Int?,
        uploadSpeedLimit: Int?,
        ratioLimit: Double?,
        seedingTimeLimit: Int?,
        isPaused: Boolean,
        skipHashChecking: Boolean,
        isAutoTorrentManagementEnabled: Boolean?,
        isSequentialDownloadEnabled: Boolean,
        isFirstLastPiecePrioritized: Boolean,
    ): Boolean

    suspend fun enqueueFile(
        serverId: Int,
        file: PlatformFile,
        savePath: String?,
        category: String?,
        tags: List<String>,
        stopCondition: String?,
        contentLayout: String?,
        torrentName: String?,
        downloadSpeedLimit: Int?,
        uploadSpeedLimit: Int?,
        ratioLimit: Double?,
        seedingTimeLimit: Int?,
        isPaused: Boolean,
        skipHashChecking: Boolean,
        isAutoTorrentManagementEnabled: Boolean?,
        isSequentialDownloadEnabled: Boolean,
        isFirstLastPiecePrioritized: Boolean,
    ): Boolean

    suspend fun flushQueue(serverId: Int)
}

object NoOpTorrentQueueManager : TorrentQueueManager {
    override val pendingCountFlow: Flow<Int> = flowOf(0)

    override suspend fun enqueueMagnet(
        serverId: Int,
        magnetUri: String,
        savePath: String?,
        category: String?,
        tags: List<String>,
        stopCondition: String?,
        contentLayout: String?,
        torrentName: String?,
        downloadSpeedLimit: Int?,
        uploadSpeedLimit: Int?,
        ratioLimit: Double?,
        seedingTimeLimit: Int?,
        isPaused: Boolean,
        skipHashChecking: Boolean,
        isAutoTorrentManagementEnabled: Boolean?,
        isSequentialDownloadEnabled: Boolean,
        isFirstLastPiecePrioritized: Boolean,
    ): Boolean = false

    override suspend fun enqueueFile(
        serverId: Int,
        file: PlatformFile,
        savePath: String?,
        category: String?,
        tags: List<String>,
        stopCondition: String?,
        contentLayout: String?,
        torrentName: String?,
        downloadSpeedLimit: Int?,
        uploadSpeedLimit: Int?,
        ratioLimit: Double?,
        seedingTimeLimit: Int?,
        isPaused: Boolean,
        skipHashChecking: Boolean,
        isAutoTorrentManagementEnabled: Boolean?,
        isSequentialDownloadEnabled: Boolean,
        isFirstLastPiecePrioritized: Boolean,
    ): Boolean = false

    override suspend fun flushQueue(serverId: Int) = Unit
}
