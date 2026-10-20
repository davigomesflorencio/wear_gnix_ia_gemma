package com.davi.dev.gnixia.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.davi.dev.gnixia.GnixiaApplication
import com.davi.dev.gnixia.domain.model.ModelStatus
import com.davi.dev.gnixia.domain.usecase.InitializeModelUseCase
import com.davi.dev.gnixia.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    /** When true, [text] holds the error detail instead of an answer */
    val isError: Boolean = false
)

sealed interface ModelState {
    data object Preparing : ModelState
    data object Loading : ModelState
    data object Ready : ModelState
    data class Failed(val reason: String) : ModelState
}

class ChatViewModel(
    private val initializeModel: InitializeModelUseCase,
    private val sendMessage: SendMessageUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Preparing)
    val modelState = _modelState.asStateFlow()

    // Question asked (e.g. from Home) before the model finished loading
    private var pendingInput: String? = null

    init {
        viewModelScope.launch {
            try {
                initializeModel().collect { status ->
                    _modelState.value = when (status) {
                        ModelStatus.Preparing -> ModelState.Preparing
                        ModelStatus.Loading -> ModelState.Loading
                        ModelStatus.Ready -> ModelState.Ready
                    }
                }
                pendingInput?.let {
                    pendingInput = null
                    send(it)
                }
            } catch (e: Throwable) {
                // Throwable: native load failures (UnsatisfiedLinkError) are Errors, not Exceptions
                _modelState.value = ModelState.Failed(e.message.orEmpty())
                Log.d("ChatViewModel", "Error loading model", e)
            }
        }
    }

    fun send(userInput: String) {
        if (_isGenerating.value) return
        if (_modelState.value != ModelState.Ready) {
            if (_modelState.value !is ModelState.Failed) pendingInput = userInput
            return
        }

        viewModelScope.launch {

            _messages.value += ChatMessage(userInput, true)
            _messages.value += ChatMessage("", false)

            _isGenerating.value = true

            try {
                sendMessage(userInput).collect {
                    val currentMessages = _messages.value.toMutableList()
                    val lastMessage = currentMessages.last()

                    currentMessages[currentMessages.lastIndex] = lastMessage.copy(text = lastMessage.text + it)
                    _messages.value = currentMessages
                }
            } catch (e: Exception) {
                val currentMessages = _messages.value.toMutableList()
                val lastMessage = currentMessages.last()

                currentMessages[currentMessages.lastIndex] = lastMessage.copy(text = e.message.orEmpty(), isError = true)
                _messages.value = currentMessages
            } finally {
                _isGenerating.value = false
            }

        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as GnixiaApplication).container
                ChatViewModel(container.initializeModel, container.sendMessage)
            }
        }
    }
}
