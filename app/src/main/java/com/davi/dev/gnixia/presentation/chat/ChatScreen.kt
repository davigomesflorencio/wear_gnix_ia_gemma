package com.davi.dev.gnixia.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.davi.dev.gnixia.R
import com.davi.dev.gnixia.presentation.common.rememberTextInputLauncher
import com.davi.dev.gnixia.presentation.theme.GnixiaStyles
import com.davi.dev.gnixia.presentation.theme.GnixiaTheme

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val modelState by viewModel.modelState.collectAsStateWithLifecycle()

    ChatScreenContent(
        modifier = modifier,
        messages = messages,
        isGenerating = isGenerating,
        modelState = modelState,
        onSend = viewModel::send
    )
}

@Composable
private fun ChatScreenContent(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    modelState: ModelState,
    onSend: (String) -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    val isModelReady = modelState == ModelState.Ready
    // Header and status row (when visible) come before the messages
    val leadingItems = if (isModelReady) 1 else 2

    val launchInput = rememberTextInputLauncher(onResult = onSend)

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(leadingItems + messages.lastIndex)
        }
    }

    ScreenScaffold(
        modifier = modifier,
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = launchInput,
                enabled = isModelReady && !isGenerating
            ) {
                Text(stringResource(R.string.chat_send))
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                ) {
                    Text(stringResource(R.string.chat_title))
                }
            }

            if (!isModelReady) {
                item {
                    ModelStatusRow(modelState)
                }
            }

            items(messages) { message ->
                ChatBubble(
                    message = message,
                    modifier = Modifier.transformedHeight(this, transformationSpec)
                )
            }
        }
    }
}

@Composable
private fun ModelStatusRow(modelState: ModelState) {
    val errorText = when (modelState) {
        is ModelState.Failed -> stringResource(R.string.chat_model_error, modelState.reason)
        else -> null
    }
    if (errorText != null) {
        Text(
            text = errorText,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    val text = when (modelState) {
        ModelState.Loading -> stringResource(R.string.chat_model_loading)
        else -> stringResource(R.string.chat_model_preparing)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    style: Style = Style
) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleStyle = if (message.isUser) GnixiaStyles.userBubble else GnixiaStyles.modelBubble
    val textColor = when {
        message.isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        message.isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val text = when {
        message.isError -> stringResource(R.string.chat_reply_error, message.text)
        message.text.isEmpty() -> stringResource(R.string.chat_typing_placeholder)
        else -> message.text
    }
    // The bubble is not interactive, so its style state has no interaction source
    val styleState = remember { MutableStyleState(null) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(modifier = Modifier.styleable(styleState, bubbleStyle, style)) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private val previewMessages = listOf(
    ChatMessage("Oi, tudo bem?", isUser = true),
    ChatMessage("Tudo ótimo! Como posso ajudar você hoje?", isUser = false),
    ChatMessage("Me explica o que é uma coroutine em Kotlin.", isUser = true),
    ChatMessage("Uma coroutine é uma forma leve de executar código assíncrono...", isUser = false)
)

@Composable
private fun ChatPreviewContainer(content: @Composable () -> Unit) {
    GnixiaTheme {
        AppScaffold {
            content()
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun ChatScreenContentPreview() {
    ChatPreviewContainer {
        ChatScreenContent(
            messages = previewMessages,
            isGenerating = false,
            modelState = ModelState.Ready,
            onSend = {}
        )
    }
}

@WearPreviewDevices
@Composable
private fun ChatScreenContentGeneratingPreview() {
    ChatPreviewContainer {
        ChatScreenContent(
            messages = previewMessages + ChatMessage("", isUser = false),
            isGenerating = true,
            modelState = ModelState.Ready,
            onSend = {}
        )
    }
}

@WearPreviewDevices
@Composable
private fun ChatScreenContentLoadingPreview() {
    ChatPreviewContainer {
        ChatScreenContent(
            messages = emptyList(),
            isGenerating = false,
            modelState = ModelState.Loading,
            onSend = {}
        )
    }
}

@WearPreviewDevices
@Composable
private fun ChatScreenContentErrorPreview() {
    ChatPreviewContainer {
        ChatScreenContent(
            messages = previewMessages.take(1) +
                ChatMessage("Tempo esgotado", isUser = false, isError = true),
            isGenerating = false,
            modelState = ModelState.Ready,
            onSend = {}
        )
    }
}
