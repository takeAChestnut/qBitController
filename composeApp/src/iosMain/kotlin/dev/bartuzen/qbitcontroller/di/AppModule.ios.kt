package dev.bartuzen.qbitcontroller.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import dev.bartuzen.qbitcontroller.data.repositories.NoOpTorrentQueueManager
import dev.bartuzen.qbitcontroller.data.repositories.TorrentQueueManager
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule = module {
    listOf("settings", "servers").forEach { name ->
        single<Settings>(named(name)) {
            NSUserDefaultsSettings(NSUserDefaults(suiteName = name))
        }
    }

    single<TorrentQueueManager> { NoOpTorrentQueueManager } bind TorrentQueueManager::class
}
