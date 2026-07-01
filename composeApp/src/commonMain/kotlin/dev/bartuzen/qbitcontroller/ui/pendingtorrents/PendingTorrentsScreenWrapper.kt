package dev.bartuzen.qbitcontroller.ui.pendingtorrents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PendingTorrentsScreenWrapper(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
)
