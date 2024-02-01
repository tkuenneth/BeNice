package de.thomaskuenneth.benice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeNiceScreen(
    windowSizeClass: WindowSizeClass,
    state: BeNiceScreenUiState,
    onClick: (AppInfo, Boolean) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit,
    onOpenAppInfoClicked: (AppInfo) -> Unit,
    modifier: Modifier
) {
    var contextMenuAppInfo by rememberSaveable { mutableStateOf<AppInfo?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val closeSheet: (() -> Unit) -> Unit = { callback ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                contextMenuAppInfo = null
            }
            callback()
        }
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    )
    {
        when (state.isLoading) {
            true -> CircularProgressIndicator()
            false -> AppChooser(
                state = state,
                columns = when (windowSizeClass.windowWidthSizeClass) {
                    WindowWidthSizeClass.MEDIUM -> 2
                    WindowWidthSizeClass.EXPANDED -> 3
                    else -> 1
                },
                onClick = onClick,
                onLongClick = { appInfo ->
                    contextMenuAppInfo = appInfo
                }
            )
        }
        contextMenuAppInfo?.let {
            ModalBottomSheet(
                onDismissRequest = { contextMenuAppInfo = null },
                sheetState = sheetState,
                windowInsets = WindowInsets(0),
            ) {
                MenuItem(
                    onClick = { closeSheet { onClick(it, false) } },
                    imageVector = Icons.Default.Launch,
                    textRes = R.string.launch
                )
                if (!state.launchAdjacent) {
                    MenuItem(
                        onClick = { closeSheet { onClick(it, true) } },
                        imageVector = ImageVector.vectorResource(id = R.drawable.launch_adjacent),
                        textRes = R.string.launch_adjacent
                    )
                }
                MenuItem(
                    onClick = { closeSheet { onOpenAppInfoClicked(it) } },
                    imageVector = Icons.Default.Info,
                    textRes = R.string.open_app_info
                )
                MenuItem(
                    onClick = { closeSheet { onAddLinkClicked(it) } },
                    imageVector = Icons.Default.AddLink,
                    textRes = R.string.add_link
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppChooser(
    state: BeNiceScreenUiState,
    columns: Int,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongClick: (AppInfo) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    if (state.installedApps.isEmpty()) {
        Text(
            text = stringResource(R.string.no_apps),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )
    } else {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(count = columns)
        ) {
            state.installedApps.forEach { appInfo ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onClick(appInfo, false) },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick(appInfo)
                                },
                                onLongClickLabel = stringResource(id = R.string.open_context_menu)
                            )
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconImage(drawable = appInfo.icon)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = appInfo.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
