package dev.bartuzen.qbitcontroller.data.repositories

import android.util.Base64
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentDao
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentEntity
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentEntity.Companion.STATUS_PENDING
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentEntity.Companion.TYPE_FILE
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentEntity.Companion.TYPE_MAGNET
import dev.bartuzen.qbitcontroller.network.RequestResult
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow

class PendingTorrentRepository(
    private val dao: PendingTorrentDao,
    private val addTorrentRepository: AddTorrentRepository,
) : TorrentQueueManager {
    fun getAllFlow(): Flow<List<PendingTorrentEntity>> = dao.getAllFlow()

    override val pendingCountFlow: Flow<Int> = dao.getPendingCountFlow()

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
    ): Boolean {
        if (dao.countPendingByMagnet(serverId, magnetUri) > 0) return false
        dao.insert(
            PendingTorrentEntity(
                serverId = serverId,
            type = TYPE_MAGNET,
            magnetUri = magnetUri,
            savePath = savePath,
            category = category,
            tags = tags.joinToString(","),
            stopCondition = stopCondition,
            contentLayout = contentLayout,
            torrentName = torrentName,
            downloadSpeedLimit = downloadSpeedLimit,
            uploadSpeedLimit = uploadSpeedLimit,
            ratioLimit = ratioLimit,
            seedingTimeLimit = seedingTimeLimit,
            isPaused = isPaused,
            skipHashChecking = skipHashChecking,
            isAutoTorrentManagementEnabled = isAutoTorrentManagementEnabled,
            isSequentialDownloadEnabled = isSequentialDownloadEnabled,
            isFirstLastPiecePrioritized = isFirstLastPiecePrioritized,
            status = STATUS_PENDING,
        ),
    )
        return true
    }

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
    ): Boolean {
        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        if (dao.countPendingByFileName(serverId, file.name) > 0) return false
        dao.insert(
            PendingTorrentEntity(
                serverId = serverId,
                type = TYPE_FILE,
                fileName = file.name,
                fileDataBase64 = base64,
                savePath = savePath,
                category = category,
                tags = tags.joinToString(","),
                stopCondition = stopCondition,
                contentLayout = contentLayout,
                torrentName = torrentName,
                downloadSpeedLimit = downloadSpeedLimit,
                uploadSpeedLimit = uploadSpeedLimit,
                ratioLimit = ratioLimit,
                seedingTimeLimit = seedingTimeLimit,
                isPaused = isPaused,
                skipHashChecking = skipHashChecking,
                isAutoTorrentManagementEnabled = isAutoTorrentManagementEnabled,
                isSequentialDownloadEnabled = isSequentialDownloadEnabled,
                isFirstLastPiecePrioritized = isFirstLastPiecePrioritized,
                status = STATUS_PENDING,
            ),
        )
        return true
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteByServerId(serverId: Int) = dao.deleteByServerId(serverId)

    suspend fun deleteCompleted() = dao.deleteCompleted()

    suspend fun resetToRetry(id: Long) = dao.resetToRetry(id)

    override suspend fun flushQueue(serverId: Int) {
        val pending = dao.getPendingByServerId(serverId)
        if (pending.isEmpty()) return

        for (entity in pending) {
            dao.markUploading(entity.id)

            when (entity.type) {
                TYPE_MAGNET -> flushMagnet(entity)
                TYPE_FILE -> flushFile(entity)
            }
        }
    }

    private suspend fun flushMagnet(entity: PendingTorrentEntity) {
        val magnetUri = entity.magnetUri ?: run {
            dao.markFailed(entity.id, "missing magnetUri")
            return
        }

        val result = addTorrentRepository.addTorrent(
            serverId = entity.serverId,
            links = listOf(magnetUri),
            files = null,
            savePath = entity.savePath,
            category = entity.category,
            tags = entity.tags.split(",").filter { it.isNotEmpty() },
            stopCondition = entity.stopCondition,
            contentLayout = entity.contentLayout,
            torrentName = entity.torrentName,
            downloadSpeedLimit = entity.downloadSpeedLimit,
            uploadSpeedLimit = entity.uploadSpeedLimit,
            ratioLimit = entity.ratioLimit,
            seedingTimeLimit = entity.seedingTimeLimit,
            isPaused = entity.isPaused,
            skipHashChecking = entity.skipHashChecking,
            isAutoTorrentManagementEnabled = entity.isAutoTorrentManagementEnabled,
            isSequentialDownloadEnabled = entity.isSequentialDownloadEnabled,
            isFirstLastPiecePrioritized = entity.isFirstLastPiecePrioritized,
        )
        handleResult(entity.id, result)
    }

    private suspend fun flushFile(entity: PendingTorrentEntity) {
        val fileName = entity.fileName ?: run {
            dao.markFailed(entity.id, "missing fileName")
            return
        }
        val base64 = entity.fileDataBase64 ?: run {
            dao.markFailed(entity.id, "missing fileData")
            return
        }

        val bytes = try {
            Base64.decode(base64, Base64.NO_WRAP)
        } catch (e: Exception) {
            dao.markFailed(entity.id, "base64 decode error: ${e.message}")
            return
        }

        val result = addTorrentRepository.addTorrent(
            serverId = entity.serverId,
            links = null,
            files = listOf(fileName to bytes),
            savePath = entity.savePath,
            category = entity.category,
            tags = entity.tags.split(",").filter { it.isNotEmpty() },
            stopCondition = entity.stopCondition,
            contentLayout = entity.contentLayout,
            torrentName = entity.torrentName,
            downloadSpeedLimit = entity.downloadSpeedLimit,
            uploadSpeedLimit = entity.uploadSpeedLimit,
            ratioLimit = entity.ratioLimit,
            seedingTimeLimit = entity.seedingTimeLimit,
            isPaused = entity.isPaused,
            skipHashChecking = entity.skipHashChecking,
            isAutoTorrentManagementEnabled = entity.isAutoTorrentManagementEnabled,
            isSequentialDownloadEnabled = entity.isSequentialDownloadEnabled,
            isFirstLastPiecePrioritized = entity.isFirstLastPiecePrioritized,
        )
        handleResult(entity.id, result)
    }

    private suspend fun handleResult(id: Long, result: RequestResult<String>) {
        when {
            result is RequestResult.Success && result.data != "Fails." -> dao.markSuccess(id)
            result is RequestResult.Error.ApiError && result.code == 409 -> dao.markExists(id)
            result is RequestResult.Error.ApiError && result.code in listOf(400, 415) -> {
                dao.markFailed(id, "HTTP ${(result as RequestResult.Error.ApiError).code}")
            }
            result is RequestResult.Error -> {
                dao.markError(id, result.toString())
            }
            else -> dao.markFailed(id, "unexpected response: ${(result as RequestResult.Success).data}")
        }
    }
}
