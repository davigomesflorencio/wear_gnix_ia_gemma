package com.davi.dev.gnixia.presentation.history

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.davi.dev.gnixia.R
import com.davi.dev.gnixia.domain.model.HistoryEntry
import com.davi.dev.gnixia.presentation.theme.GnixiaTheme
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    onEntryClick: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()

    HistoryScreenContent(
        entries = entries,
        onEntryClick = onEntryClick,
        onClear = viewModel::clear
    )
}

@Composable
private fun HistoryScreenContent(
    entries: List<HistoryEntry>,
    onEntryClick: (Long) -> Unit,
    onClear: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    var showClearDialog by remember { mutableStateOf(false) }

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = { showClearDialog = true },
                enabled = entries.isNotEmpty()
            ) {
                Text(stringResource(R.string.history_clear))
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(stringResource(R.string.history_title))
                }
            }

            if (entries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(entries, key = { it.id }) { entry ->
                TitleCard(
                    onClick = { onEntryClick(entry.id) },
                    title = {
                        Text(entry.question, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    },
                    time = { Text(formatTime(entry.createdAt)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(entry.answer, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    AlertDialog(
        visible = showClearDialog,
        onDismissRequest = { showClearDialog = false },
        title = { Text(stringResource(R.string.history_clear_confirm)) },
        confirmButton = {
            AlertDialogDefaults.ConfirmButton(
                onClick = {
                    onClear()
                    showClearDialog = false
                }
            )
        },
        dismissButton = {
            AlertDialogDefaults.DismissButton(onClick = { showClearDialog = false })
        }
    )
}

internal fun formatTime(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))

private val previewEntries = listOf(
    HistoryEntry(1, "O que é uma coroutine?", "Uma forma leve de executar código assíncrono.", System.currentTimeMillis()),
    HistoryEntry(2, "Qual a capital do Brasil?", "Brasília.", System.currentTimeMillis())
)

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun HistoryScreenPreview() {
    GnixiaTheme {
        AppScaffold {
            HistoryScreenContent(entries = previewEntries, onEntryClick = {}, onClear = {})
        }
    }
}

@WearPreviewDevices
@Composable
private fun HistoryScreenEmptyPreview() {
    GnixiaTheme {
        AppScaffold {
            HistoryScreenContent(entries = emptyList(), onEntryClick = {}, onClear = {})
        }
    }
}
