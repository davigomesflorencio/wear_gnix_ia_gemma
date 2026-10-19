package com.davi.dev.gnixia.presentation.history

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.davi.dev.gnixia.R
import com.davi.dev.gnixia.domain.model.HistoryEntry
import com.davi.dev.gnixia.presentation.theme.GnixiaTheme

@Composable
fun HistoryDetailScreen(
    entryId: Long,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val entryFlow = remember(entryId) { viewModel.entry(entryId) }
    val entry by entryFlow.collectAsStateWithLifecycle(initialValue = null)

    HistoryDetailContent(entry)
}

@Composable
private fun HistoryDetailContent(entry: HistoryEntry?) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
            if (entry == null) return@TransformingLazyColumn

            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(formatTime(entry.createdAt))
                }
            }
            item {
                ListSubHeader(modifier = Modifier.transformedHeight(this, transformationSpec)) {
                    Text(stringResource(R.string.history_question))
                }
            }
            item {
                Text(
                    text = entry.question,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
            item {
                ListSubHeader(modifier = Modifier.transformedHeight(this, transformationSpec)) {
                    Text(stringResource(R.string.history_answer))
                }
            }
            item {
                Text(
                    text = entry.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun HistoryDetailPreview() {
    GnixiaTheme {
        AppScaffold {
            HistoryDetailContent(
                HistoryEntry(
                    id = 1,
                    question = "O que é uma coroutine?",
                    answer = "Uma coroutine é uma forma leve de executar código assíncrono em Kotlin.",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
