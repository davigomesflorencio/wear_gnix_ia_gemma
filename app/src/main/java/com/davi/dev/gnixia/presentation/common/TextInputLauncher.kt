package com.davi.dev.gnixia.presentation.common

import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.wear.input.RemoteInputIntentHelper
import com.davi.dev.gnixia.R

private const val INPUT_KEY = "chat_input"

/**
 * Returns a function that opens the system text input (keyboard, voice or emoji)
 * and delivers the trimmed, non-empty result to [onResult].
 */
@Composable
fun rememberTextInputLauncher(onResult: (String) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    val inputLabel = stringResource(R.string.chat_input_label)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val text = RemoteInput.getResultsFromIntent(data)
            ?.getCharSequence(INPUT_KEY)
            ?.toString()
            ?.trim()
        if (!text.isNullOrEmpty()) currentOnResult(text)
    }

    return {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInput = RemoteInput.Builder(INPUT_KEY)
            .setLabel(inputLabel)
            .build()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        launcher.launch(intent)
    }
}
