package com.davi.dev.gnixia.presentation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.davi.dev.gnixia.R
import com.davi.dev.gnixia.presentation.history.HistoryViewModel
import com.davi.dev.gnixia.presentation.theme.GnixiaTheme

@Composable
fun SettingsScreen(
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }

    SettingsScreenContent(
        hasHistory = entries.isNotEmpty(),
        versionName = versionName,
        onClearHistory = viewModel::clear
    )
}

@Composable
private fun SettingsScreenContent(
    hasHistory: Boolean,
    versionName: String,
    onClearHistory: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    var showClearDialog by remember { mutableStateOf(false) }

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(stringResource(R.string.settings_title))
                }
            }
            item {
                Button(
                    onClick = { showClearDialog = true },
                    enabled = hasHistory,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.settings_clear_history)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.settings_version, versionName),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
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
                    onClearHistory()
                    showClearDialog = false
                }
            )
        },
        dismissButton = {
            AlertDialogDefaults.DismissButton(onClick = { showClearDialog = false })
        }
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun SettingsScreenPreview() {
    GnixiaTheme {
        AppScaffold {
            SettingsScreenContent(hasHistory = true, versionName = "1.0", onClearHistory = {})
        }
    }
}
