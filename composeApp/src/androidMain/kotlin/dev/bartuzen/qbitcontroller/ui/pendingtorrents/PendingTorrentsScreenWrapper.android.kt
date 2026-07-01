package dev.bartuzen.qbitcontroller.ui.pendingtorrents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PendingTorrentsScreenWrapper(
    onNavigateBack: () -> Unit,
    modifier: Modifier,
) {
    PendingTorrentsScreen(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
