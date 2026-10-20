package com.davi.dev.gnixia.presentation.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.davi.dev.gnixia.R
import com.davi.dev.gnixia.presentation.common.rememberTextInputLauncher
import com.davi.dev.gnixia.presentation.theme.GnixiaTheme

@Composable
fun HomeScreen(
    onOpenAbout: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearch: (String) -> Unit
) {
    val launchInput = rememberTextInputLauncher(onResult = onSearch)

    HomeScreenContent(
        onOpenAbout = onOpenAbout,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
        onSearchClick = launchInput
    )
}

@Composable
private fun HomeScreenContent(
    onOpenAbout: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearchClick: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(onClick = onSearchClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.home_search)
                )
            }
        }
    ) { contentPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Proportional side insets keep the grid's corners inside round displays of any size;
            // at the top, the centered title already pushes the grid away from the curved edge
            val horizontalInset = maxWidth * 0.12f
            val topInset = contentPadding.calculateTopPadding()
            val bottomInset = contentPadding.calculateBottomPadding()
            val contentHeight = (maxHeight - topInset - bottomInset).coerceAtLeast(MinContentHeight)

            // A single-item list lets ScreenScaffold show the EdgeButton; it only scrolls
            // when the content can't fit (MinContentHeight on very small screens).
            // Title and grid share one item so the grid takes exactly the space the title leaves
            TransformingLazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = horizontalInset,
                    end = horizontalInset,
                    top = topInset,
                    bottom = bottomInset
                )
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(contentHeight),
                        verticalArrangement = Arrangement.spacedBy(TileSpacing)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                        HomeGrid(
                            onOpenAbout = onOpenAbout,
                            onOpenHistory = onOpenHistory,
                            onOpenSettings = onOpenSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeGrid(
    onOpenAbout: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TileSpacing)
    ) {
        HomeTile(
            iconRes = R.drawable.ic_home,
            labelRes = R.string.home_about,
            onClick = onOpenAbout,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconSize = 32.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(TileSpacing)
        ) {
            HomeTile(
                iconRes = R.drawable.ic_history,
                labelRes = R.string.home_history,
                onClick = onOpenHistory,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            HomeTile(
                iconRes = R.drawable.ic_settings,
                labelRes = R.string.home_settings,
                onClick = onOpenSettings,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

private val TileSpacing = 4.dp
private val MinContentHeight = 110.dp

@Composable
private fun HomeTile(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 20.dp
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = contentColor,
                // On small screens the icon shrinks so the label always fits:
                // weighted children are measured last, with the space left over
                modifier = Modifier
                    .weight(1f, fill = false)
                    .sizeIn(maxWidth = iconSize, maxHeight = iconSize)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(labelRes),
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun HomeScreenPreview() {
    GnixiaTheme {
        AppScaffold {
            HomeScreenContent(
                onOpenAbout = {},
                onOpenHistory = {},
                onOpenSettings = {},
                onSearchClick = {}
            )
        }
    }
}
