package de.thomaskuenneth.benice

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeNiceScreen(
    windowSizeClass: WindowSizeClass,
    state: BeNiceScreenUiState, onClick: (AppInfo) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit,
    onOpenAppInfoClicked: (AppInfo) -> Unit,
    onAppsForAppPairSelected: (AppInfo, AppInfo, Long, String) -> Unit,
    selectImage: () -> Unit,
    modifier: Modifier
) {
    var contextMenuAppInfo by remember { mutableStateOf<AppInfo?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var firstApp: AppInfo? by remember { mutableStateOf(null) }
    var secondApp: AppInfo? by remember { mutableStateOf(null) }
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
        if (!state.isLoading) {
            AppChooser(
                installedApps = state.installedApps,
                columns = when (windowSizeClass.windowWidthSizeClass) {
                    WindowWidthSizeClass.MEDIUM -> 2
                    WindowWidthSizeClass.EXPANDED -> 3
                    else -> 1
                },
                letterPosition = state.letterPosition,
                showSpecials = false,
                onClick = onClick,
                onLongClick = { appInfo ->
                    contextMenuAppInfo = appInfo
                },
                selectImage = selectImage
            )
            FloatingActionButton(
                onClick = {
                    showAppPairDialog = true
                    firstApp = null
                    secondApp = null
                },
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd)
                    .padding(
                        WindowInsets.navigationBars
                            .union(WindowInsets(16.dp, 16.dp, 16.dp, 16.dp))
                            .asPaddingValues()
                    )
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
                onClick = { closeSheet { onClick(it) } },
                imageVector = Icons.AutoMirrored.Filled.Launch,
                textRes = R.string.launch
            )
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
                        showAppPairDialog = true
                    }
                },
                imageVector = Icons.Default.Create,
                textRes = R.string.create_app_pair
            )
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
    if (showAppPairDialog) {
        AppPairDialog(
            state = state,
            firstApp = firstApp,
            onFirstAoppChanged = { firstApp = it },
            secondApp = secondApp,
            onSecondAoppChanged = { secondApp = it },
            onDismissRequest = { showAppPairDialog = false },
            onFinished = { first, second, delay, label ->
                onAppsForAppPairSelected(first, second, delay, label)
                showAppPairDialog = false
            },
            selectImage = selectImage
        )
    }
}

@Composable
fun AppPairDialog(
    state: BeNiceScreenUiState,
    firstApp: AppInfo?,
    onFirstAoppChanged: (AppInfo?) -> Unit,
    secondApp: AppInfo?,
    onSecondAoppChanged: (AppInfo?) -> Unit,
    onDismissRequest: () -> Unit,
    onFinished: (AppInfo, AppInfo, Long, String) -> Unit,
    selectImage: () -> Unit
) {
    val sameApp: Boolean by remember(firstApp, secondApp) {
        mutableStateOf(
            sameApp(
                firstApp,
                secondApp
            )
        )
    }
    val bothAppsChosen: Boolean by remember(
        firstApp,
        secondApp
    ) { mutableStateOf(firstApp != null && secondApp != null) }
    var delay by remember { mutableFloatStateOf(500F) }
    var label by remember(firstApp, secondApp) {
        mutableStateOf(
            label(
                firstApp = firstApp,
                secondApp = secondApp
            )
        )
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                enabled = label.isNotBlank() && !label.isTooLong() && bothAppsChosen && !sameApp,
                onClick = { onFinished(firstApp!!, secondApp!!, delay.toLong(), label) }) {
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
            Box {
                val scrollState = rememberScrollState()
                val coroutineScope = rememberCoroutineScope()
                val showDownButton by remember { derivedStateOf { scrollState.value == 0 && scrollState.canScrollForward } }
                val showUpButton by remember { derivedStateOf { scrollState.value == scrollState.maxValue && scrollState.canScrollBackward } }
                Column(
                    modifier = Modifier.verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CompactAppChooser(
                        installedApps = state.installedApps,
                        letterPosition = state.letterPosition,
                        selectedApp = firstApp,
                        hint = R.string.select_first_app,
                        onItemClicked = { selectedApp ->
                            onFirstAoppChanged(selectedApp)
                            label(firstApp = firstApp, secondApp = secondApp)
                        },
                        selectImage = selectImage
                    )
                    CompactAppChooser(
                        installedApps = state.installedApps,
                        letterPosition = state.letterPosition,
                        selectedApp = secondApp,
                        hint = R.string.select_second_app,
                        onItemClicked = { selectedApp ->
                            onSecondAoppChanged(selectedApp)
                            label(firstApp = firstApp, secondApp = secondApp)
                        },
                        selectImage = selectImage
                    )
                    if (bothAppsChosen && sameApp) {
                        Text(
                            modifier = Modifier.align(Alignment.Start),
                            text = stringResource(id = R.string.apps_must_be_different),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
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
                        BeNiceLabel(text = R.string.five_hundred_ms)
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
                    if (firstApp != null && secondApp != null) {
                        BeNiceTextField(
                            value = label,
                            resId = R.string.app_pair_label,
                            message = if (label.isBlank()) {
                                stringResource(id = R.string.label_cannot_be_blank)
                            } else {
                                if (label.isTooLong()) {
                                    stringResource(id = R.string.too_long)
                                } else {
                                    ""
                                }
                            },
                            keyboardType = KeyboardType.Text,
                            onValueChange = { label = it }
                        )
                    }
                }
                AnimatedUpOrDownButton(
                    isUpButton = true,
                    shouldBeVisible = showUpButton,
                    coroutineScope = coroutineScope,
                    scrollState = scrollState
                )
                AnimatedUpOrDownButton(
                    isUpButton = false,
                    shouldBeVisible = showDownButton,
                    coroutineScope = coroutineScope,
                    scrollState = scrollState
                )
            }
        }
    )
}

fun Activity.startActivityCatchExceptions(intent: Intent) {
    try {
        startActivity(intent)
    } catch (ex: Exception) {
        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
    }
}

private const val UNSPECIFIED = "???"
private fun label(firstApp: AppInfo?, secondApp: AppInfo?) =
    "${firstApp?.label ?: UNSPECIFIED} \u2011 ${secondApp?.label ?: UNSPECIFIED}"

private fun String.isTooLong() = length > 64
