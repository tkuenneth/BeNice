package de.thomaskuenneth.benice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
    onAppsForAppPairSelected: (AppInfo, AppInfo, Long) -> Unit,
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
    var showAppPairDialog by remember { mutableStateOf(false) }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    )
    {
        when (state.isLoading) {
            true -> CircularProgressIndicator()
            false -> {
                AppChooser(
                    installedApps = state.installedApps,
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
                    onClick = { showAppPairDialog = true },
                    modifier = Modifier
                        .align(alignment = Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_app_pair)
                    )
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
        AppChooserDialog(
            installedApps = state.installedApps,
            onClick = { secondApp, _ ->
                showSelectSecondAppDialog = false
                firstApp?.let { onAppsForAppPairSelected(it, secondApp, 500L) }
            },
            onDismissRequest = {
                showSelectSecondAppDialog = false
            }
        )
    }
    if (showAppPairDialog) {
        AppPairDialog(
            state = state,
            onDismissRequest = { showAppPairDialog = false },
            onFinished = { first, second, delay ->
                onAppsForAppPairSelected(first, second, delay)
                showAppPairDialog = false
            }
        )
    }
}

@Composable
fun AppPairDialog(
    state: BeNiceScreenUiState,
    onDismissRequest: () -> Unit,
    onFinished: (AppInfo, AppInfo, Long) -> Unit
) {
    var firstApp: AppInfo? by remember { mutableStateOf(null) }
    var secondApp: AppInfo? by remember { mutableStateOf(null) }
    var delay by remember { mutableFloatStateOf(500F) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                enabled = firstApp != null && secondApp != null,
                onClick = { onFinished(firstApp!!, secondApp!!, delay.toLong()) }) {
                Text(text = stringResource(id = R.string.create))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = { Text(text = stringResource(id = R.string.create_app_pair)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CompactAppChooser(
                    installedApps = state.installedApps,
                    selectedApp = firstApp,
                    hint = R.string.first_app,
                    onItemClicked = { selectedApp -> firstApp = selectedApp }
                )
                CompactAppChooser(
                    installedApps = state.installedApps,
                    selectedApp = secondApp,
                    hint = R.string.second_app,
                    onItemClicked = { selectedApp -> secondApp = selectedApp }
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(id = R.string.launch_after),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.five_hundred_ms),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = delay,
                        onValueChange = { delay = it },
                        valueRange = (500F..2000F),
                        steps = 14,
                        modifier = Modifier.weight(1.0F)
                    )
                    Text(
                        text = stringResource(id = R.string.two_secs),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    )
}
