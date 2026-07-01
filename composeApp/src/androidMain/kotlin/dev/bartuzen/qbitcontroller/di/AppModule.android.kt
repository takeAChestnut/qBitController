package dev.bartuzen.qbitcontroller.di

import android.content.Context
import androidx.room.Room
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.bartuzen.qbitcontroller.data.db.PendingTorrentDatabase
import dev.bartuzen.qbitcontroller.data.notification.AppNotificationManager
import dev.bartuzen.qbitcontroller.data.notification.TorrentDownloadedWorker
import dev.bartuzen.qbitcontroller.data.repositories.PendingTorrentRepository
import dev.bartuzen.qbitcontroller.data.repositories.TorrentQueueManager
import dev.bartuzen.qbitcontroller.ui.pendingtorrents.PendingTorrentsViewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    listOf("settings", "servers", "torrents").forEach { name ->
        single<Settings>(named(name)) {
            val context: Context = get()
            SharedPreferencesSettings(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        }
    }

    singleOf(::AppNotificationManager)

    workerOf(::TorrentDownloadedWorker)

    single {
        val context: Context = get()
        Room.databaseBuilder(
            context,
            PendingTorrentDatabase::class.java,
            "pending_torrents.db",
        ).build()
    }

    single { get<PendingTorrentDatabase>().pendingTorrentDao() }

    singleOf(::PendingTorrentRepository) bind TorrentQueueManager::class

    viewModelOf(::PendingTorrentsViewModel)
}
