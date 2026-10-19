package com.davi.dev.gnixia.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import com.davi.dev.gnixia.presentation.about.AboutScreen
import com.davi.dev.gnixia.presentation.chat.ChatScreen
import com.davi.dev.gnixia.presentation.chat.ChatViewModel
import com.davi.dev.gnixia.presentation.history.HistoryDetailScreen
import com.davi.dev.gnixia.presentation.history.HistoryScreen
import com.davi.dev.gnixia.presentation.home.HomeScreen
import com.davi.dev.gnixia.presentation.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(HomeRoute)
    // Activity-scoped, so it is the same instance ChatScreen uses
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)

    NavDisplay(
        backStack = backStack,
        sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy()),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    onOpenAbout = { backStack.add(AboutRoute) },
                    onOpenHistory = { backStack.add(HistoryRoute) },
                    onOpenSettings = { backStack.add(SettingsRoute) },
                    onSearch = { question ->
                        chatViewModel.send(question)
                        backStack.add(ChatRoute)
                    }
                )
            }
            entry<AboutRoute> {
                AboutScreen()
            }
            entry<SettingsRoute> {
                SettingsScreen()
            }
            entry<ChatRoute> {
                ChatScreen(viewModel = chatViewModel)
            }
            entry<HistoryRoute> {
                HistoryScreen(onEntryClick = { id -> backStack.add(HistoryDetailRoute(id)) })
            }
            entry<HistoryDetailRoute> { route ->
                HistoryDetailScreen(entryId = route.id)
            }
        }
    )
}
