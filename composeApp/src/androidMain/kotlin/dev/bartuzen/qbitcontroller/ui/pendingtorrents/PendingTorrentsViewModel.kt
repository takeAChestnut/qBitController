package dev.bartuzen.qbitcontroller.ui.pendingtorrents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bartuzen.qbitcontroller.data.ServerManager
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentEntity
import dev.bartuzen.qbitcontroller.data.repositories.PendingTorrentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingTorrentsViewModel(
    private val repository: PendingTorrentRepository,
    private val serverManager: ServerManager,
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    val servers = serverManager.serversFlow

    val items = repository.getAllFlow()
        .map { list ->
            list.map { entity ->
                val serverName = serverManager.getServerOrNull(entity.serverId)?.name
                    ?: "Server #${entity.serverId}"
                PendingTorrentUiItem(entity = entity, serverName = serverName)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteItem(id: Long) = viewModelScope.launch {
        repository.deleteById(id)
    }

    fun deleteCompleted() = viewModelScope.launch {
        repository.deleteCompleted()
        eventChannel.send(Event.CompletedDeleted)
    }

    fun retryItem(id: Long) = viewModelScope.launch {
        repository.resetToRetry(id)
    }

    sealed class Event {
        data object CompletedDeleted : Event()
    }
}

data class PendingTorrentUiItem(
    val entity: PendingTorrentEntity,
    val serverName: String,
)
