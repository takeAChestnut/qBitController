package dev.bartuzen.qbitcontroller.ui.pendingtorrents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentEntity
import dev.bartuzen.qbitcontroller.ui.components.DropdownMenuItem
import dev.bartuzen.qbitcontroller.ui.components.EmptyListMessage
import dev.bartuzen.qbitcontroller.ui.components.SwipeableSnackbarHost
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.TopAppBarDefaults
import dev.bartuzen.qbitcontroller.utils.EventEffect
import dev.bartuzen.qbitcontroller.utils.getString
import dev.bartuzen.qbitcontroller.utils.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import qbitcontroller.composeapp.generated.resources.Res
import qbitcontroller.composeapp.generated.resources.pending_torrents_completed_deleted
import qbitcontroller.composeapp.generated.resources.pending_torrents_delete
import qbitcontroller.composeapp.generated.resources.pending_torrents_delete_completed
import qbitcontroller.composeapp.generated.resources.pending_torrents_empty
import qbitcontroller.composeapp.generated.resources.pending_torrents_retry
import qbitcontroller.composeapp.generated.resources.pending_torrents_retry_count
import qbitcontroller.composeapp.generated.resources.pending_torrents_status_error
import qbitcontroller.composeapp.generated.resources.pending_torrents_status_exists
import qbitcontroller.composeapp.generated.resources.pending_torrents_status_failed
import qbitcontroller.composeapp.generated.resources.pending_torrents_status_pending
import qbitcontroller.composeapp.generated.resources.pending_torrents_status_success
import qbitcontroller.composeapp.generated.resources.pending_torrents_status_uploading
import qbitcontroller.composeapp.generated.resources.pending_torrents_title

@Composable
fun PendingTorrentsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PendingTorrentsViewModel = koinViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    EventEffect(viewModel.eventFlow) { event ->
        when (event) {
            PendingTorrentsViewModel.Event.CompletedDeleted -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.pending_torrents_completed_deleted))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            var showMenu by remember { mutableStateOf(false) }
            TopAppBar(
                title = { Text(stringResource(Res.string.pending_torrents_title)) },
                colors = TopAppBarDefaults.topAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.pending_torrents_delete_completed)) },
                            onClick = {
                                showMenu = false
                                viewModel.deleteCompleted()
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = {
            SwipeableSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
    ) { paddingValues ->
        if (items.isEmpty()) {
            EmptyListMessage(
                icon = Icons.Filled.HourglassEmpty,
                title = stringResource(Res.string.pending_torrents_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items, key = { it.entity.id }) { uiItem ->
                    PendingTorrentItem(
                        item = uiItem,
                        onDelete = { viewModel.deleteItem(uiItem.entity.id) },
                        onRetry = { viewModel.retryItem(uiItem.entity.id) },
                    )
                }
                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            }
        }
    }
}

@Composable
private fun PendingTorrentItem(
    item: PendingTorrentUiItem,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entity = item.entity
    val statusText = when (entity.status) {
        PendingTorrentEntity.STATUS_PENDING -> stringResource(Res.string.pending_torrents_status_pending)
        PendingTorrentEntity.STATUS_UPLOADING -> stringResource(Res.string.pending_torrents_status_uploading)
        PendingTorrentEntity.STATUS_SUCCESS -> stringResource(Res.string.pending_torrents_status_success)
        PendingTorrentEntity.STATUS_EXISTS -> stringResource(Res.string.pending_torrents_status_exists)
        PendingTorrentEntity.STATUS_FAILED -> stringResource(Res.string.pending_torrents_status_failed)
        PendingTorrentEntity.STATUS_ERROR -> stringResource(Res.string.pending_torrents_status_error)
        else -> entity.status
    }

    val statusColor = when (entity.status) {
        PendingTorrentEntity.STATUS_SUCCESS, PendingTorrentEntity.STATUS_EXISTS ->
            MaterialTheme.colorScheme.primary
        PendingTorrentEntity.STATUS_FAILED ->
            MaterialTheme.colorScheme.error
        PendingTorrentEntity.STATUS_ERROR ->
            MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val timeText = remember(entity.addedAt) {
        val addedTime = java.util.Calendar.getInstance().apply { timeInMillis = entity.addedAt }
        val now = java.util.Calendar.getInstance()
        val isToday = addedTime.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
            addedTime.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
        if (isToday) {
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(entity.addedAt)
        } else {
            java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(entity.addedAt)
        }
    }

    val displayName = when (entity.type) {
        PendingTorrentEntity.TYPE_FILE -> entity.fileName ?: entity.torrentName ?: "-"
        else -> entity.torrentName ?: entity.magnetUri?.take(80)?.let { "$it…" } ?: "-"
    }

    val canRetry = entity.status in listOf(
        PendingTorrentEntity.STATUS_ERROR,
        PendingTorrentEntity.STATUS_FAILED,
    )

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            // top row: server name + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.serverName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // torrent name
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            // error message if any
            if (entity.lastError != null) {
                Text(
                    text = entity.lastError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }

            // bottom row: status + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // left: status (+ retry count)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                    )
                    if (entity.retryCount > 0) {
                        Text(
                            text = stringResource(Res.string.pending_torrents_retry_count, entity.retryCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // right: retry + delete
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    if (canRetry) {
                        androidx.compose.material3.TextButton(
                            onClick = onRetry,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.pending_torrents_retry),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    androidx.compose.material3.TextButton(
                        onClick = onDelete,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.pending_torrents_delete),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
