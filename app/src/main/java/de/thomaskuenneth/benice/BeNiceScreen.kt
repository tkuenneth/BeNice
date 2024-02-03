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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
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
    onAppsForAppPairSelected: (AppInfo, AppInfo) -> Unit,
    modifier: Modifier
) {
    var contextMenuAppInfo by remember { mutableStateOf<AppInfo?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSelectSecondAppDialog by remember { mutableStateOf(false) }
    var firstApp by remember { mutableStateOf<AppInfo?>(null) }
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
            false -> {
                AppChooser(
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
                FloatingActionButton(
                        onClick = { },
                        modifier = Modifier.align(alignment = Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, "Floating action button.")
                }
            }
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
                if (!state.launchAdjacent && !windowSizeClass.hasExpandedScreen()) {
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
                MenuItem(
                    onClick = {
                        closeSheet {
                            firstApp = it
                            showSelectSecondAppDialog = true
                        }
                    },
                    imageVector = Icons.Default.Create,
                    textRes = R.string.create_app_pair
                )
            }
        }
    }
    if (showSelectSecondAppDialog) {
        Dialog(
            onDismissRequest = {
                showSelectSecondAppDialog = false
            }
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(size = 12.dp)
            ) {
                AppChooser(
                    state = state,
                    columns = 1,
                    onClick = { secondApp, _ ->
                        showSelectSecondAppDialog = false
                        firstApp?.let {
                            onAppsForAppPairSelected(
                                it,
                                secondApp
                            )
                        }
                    },
                    onLongClick = {}
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
                columns = GridCells.Fixed(count = columns),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var last = ""
            state.installedApps.forEach { appInfo ->
                appInfo.label.substring((0 until 1)).let { current ->
                    if (current != last) {
                        last = current
                        header {
                            Text(modifier = Modifier.fillMaxWidth(),
                                    text = current,
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
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
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
