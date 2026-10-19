package com.davi.dev.gnixia.di

import android.content.Context
import com.davi.dev.gnixia.data.local.AppDatabase
import com.davi.dev.gnixia.data.repository.ChatRepositoryImpl
import com.davi.dev.gnixia.data.repository.HistoryRepositoryImpl
import com.davi.dev.gnixia.domain.repository.ChatRepository
import com.davi.dev.gnixia.domain.repository.HistoryRepository
import com.davi.dev.gnixia.domain.usecase.ClearHistoryUseCase
import com.davi.dev.gnixia.domain.usecase.InitializeModelUseCase
import com.davi.dev.gnixia.domain.usecase.ObserveHistoryEntryUseCase
import com.davi.dev.gnixia.domain.usecase.ObserveHistoryUseCase
import com.davi.dev.gnixia.domain.usecase.SendMessageUseCase

/** Manual dependency injection: creates and holds the app-wide instances. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val chatRepository: ChatRepository by lazy { ChatRepositoryImpl(appContext) }

    private val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(AppDatabase.getInstance(appContext).chatHistoryDao())
    }

    val initializeModel by lazy { InitializeModelUseCase(chatRepository) }
    val sendMessage by lazy { SendMessageUseCase(chatRepository, historyRepository) }
    val observeHistory by lazy { ObserveHistoryUseCase(historyRepository) }
    val observeHistoryEntry by lazy { ObserveHistoryEntryUseCase(historyRepository) }
    val clearHistory by lazy { ClearHistoryUseCase(historyRepository) }
}
