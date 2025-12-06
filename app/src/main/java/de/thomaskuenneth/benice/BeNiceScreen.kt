package de.thomaskuenneth.benice

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch

private const val launchDelayMin = 500F
private const val launchDelayMax = 5000F

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeNiceScreen(
    canAddPinnedShortcut: Boolean,
    canAddDynamicShortcut: Boolean,
    windowSizeClass: WindowSizeClass,
    state: BeNiceScreenUiState,
    onClick: (AppInfo, Boolean) -> Unit,
    onShortcutClicked: (ShortcutInfo) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit,
    onOpenAppInfoClicked: (AppInfo) -> Unit,
    onCopyNamesClicked: (AppInfo) -> Unit,
    onAppsForAppPairSelected: (AppInfo, AppInfo, Long, String, Boolean, Boolean, AppPairIconLayout) -> Unit,
    selectBitmap: () -> Unit,
    queryInstalledApps: () -> Unit,
    createShortcutIcon: (ShortcutInfo) -> Drawable,
    modifier: Modifier,
) {
    var contextMenuAppInfo by remember { mutableStateOf<AppInfo?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var firstApp: AppInfo? by remember { mutableStateOf<AppInfo?>(null) }
    var secondApp: AppInfo? by remember { mutableStateOf<AppInfo?>(null) }
    val closeSheet: (() -> Unit) -> Unit = { callback ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            callback()
            if (!sheetState.isVisible) {
                contextMenuAppInfo = null
            }
        }
    }
    var showAppPairDialog by remember { mutableStateOf(false) }
    val hideAppPairDialog: () -> Unit = {
        showAppPairDialog = false
        firstApp = null
        secondApp = null
    }
    LaunchedEffect(Unit) {
        if (state.installedApps.isEmpty()) {
            queryInstalledApps()
        }
    }
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            AppChooser(
                dynamicShortcuts = state.dynamicShortcuts,
                createShortcutIcon = createShortcutIcon,
                onShortcutClicked = onShortcutClicked,
                installedApps = state.installedApps,
                columns = when (windowSizeClass.minWidthDp) {
                    in (WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND..<WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
                        if (state.threeColumnsOnMediumScreens) 3 else 2
                    }

                    in (WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND..Int.MAX_VALUE) -> {
                        if (state.twoColumnsOnLargeScreens) 2 else 3
                    }

                    in (0..<WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                        if (state.twoColumnsOnSmallScreens) 2 else 1
                    }

                    else -> 1
                },
                letterPosition = state.letterPosition,
                showSpecials = false,
                shouldAddEmptySpace = true,
                onClick = onClick,
                onLongClick = { appInfo ->
                    contextMenuAppInfo = appInfo
                },
                selectBitmap = selectBitmap
            )
        }
        FloatingActionButton(
            onClick = {
                showAppPairDialog = true
                firstApp = null
                secondApp = null
            },
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(all = 16.dp)
                .navigationBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.create_app_pair)
            )
        }
    }
    contextMenuAppInfo?.let { appInfo ->
        ContextModalBottomSheet(
            contextMenuAppInfo = appInfo,
            sheetState = sheetState,
            closeSheet = { callback -> closeSheet(callback) },
            onLaunchClicked = onClick,
            onAddLinkClicked = onAddLinkClicked,
            onOpenAppInfoClicked = onOpenAppInfoClicked,
            onCreateAppPairClicked = {
                firstApp = contextMenuAppInfo
                showAppPairDialog = true
            },
            onCopyNamesClicked = onCopyNamesClicked,
            onDismissRequest = { contextMenuAppInfo = null })
    }
    if (showAppPairDialog) {
        AppPairDialog(
            canAddPinnedShortcut = canAddPinnedShortcut,
            canAddDynamicShortcut = canAddDynamicShortcut,
            state = state,
            firstApp = firstApp,
            onFirstAppChanged = { firstApp = it },
            secondApp = secondApp,
            onSecondAppChanged = { secondApp = it },
            onDismissRequest = hideAppPairDialog,
            onFinished = { first, second, delay, label, createPinnedShortcut, addDynamicShortcut, layout ->
                onAppsForAppPairSelected(
                    first,
                    second,
                    delay,
                    label,
                    createPinnedShortcut,
                    addDynamicShortcut,
                    layout
                )
                hideAppPairDialog()
            },
            selectBitmap = selectBitmap
        )
    }
}

@Composable
fun AppPairDialog(
    canAddPinnedShortcut: Boolean,
    canAddDynamicShortcut: Boolean,
    state: BeNiceScreenUiState,
    firstApp: AppInfo?,
    onFirstAppChanged: (AppInfo?) -> Unit,
    secondApp: AppInfo?,
    onSecondAppChanged: (AppInfo?) -> Unit,
    onDismissRequest: () -> Unit,
    onFinished: (AppInfo, AppInfo, Long, String, Boolean, Boolean, AppPairIconLayout) -> Unit,
    selectBitmap: () -> Unit
) {
    val sameApp: Boolean by remember(firstApp, secondApp) {
        mutableStateOf(
            sameApp(
                firstApp, secondApp
            )
        )
    }
    val bothAppsChosen: Boolean by remember(
        firstApp, secondApp
    ) { mutableStateOf(firstApp != null && secondApp != null) }
    var delay by remember { mutableFloatStateOf(launchDelayMin) }
    var label by remember(firstApp, secondApp) {
        mutableStateOf(
            label(
                firstApp = firstApp, secondApp = secondApp
            )
        )
    }
    var createPinnedShortcut by remember { mutableStateOf(canAddPinnedShortcut) }
    var addDynamicShortcut by remember { mutableStateOf(canAddDynamicShortcut) }
    var layout: AppPairIconLayout by remember { mutableStateOf(AppPairIconLayout.Horizontal) }
    var customImage by remember { mutableStateOf<Bitmap?>(null) }
    val updateLayout = {
        layout = AppPairIconLayout.CustomImage(customImage)
    }
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        Button(
            enabled = label.isNotBlank() &&
                    !label.isTooLong() &&
                    bothAppsChosen &&
                    !sameApp &&
                    ((addDynamicShortcut && canAddDynamicShortcut) || (createPinnedShortcut && canAddPinnedShortcut)),
            onClick = {
                onFinished(
                    firstApp!!,
                    secondApp!!,
                    delay.toLong(),
                    label,
                    createPinnedShortcut,
                    addDynamicShortcut,
                    layout
                )
            }) {
            Text(text = stringResource(id = R.string.create))
        }
    }, dismissButton = {
        OutlinedButton(onClick = { onDismissRequest() }) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }, title = { Text(text = stringResource(id = R.string.create_app_pair)) }, text = {
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
                        onFirstAppChanged(selectedApp)
                        label(firstApp = firstApp, secondApp = secondApp)
                    },
                    selectImage = selectBitmap
                )
                CompactAppChooser(
                    installedApps = state.installedApps,
                    letterPosition = state.letterPosition,
                    selectedApp = secondApp,
                    hint = R.string.select_second_app,
                    onItemClicked = { selectedApp ->
                        onSecondAppChanged(selectedApp)
                        label(firstApp = firstApp, secondApp = secondApp)
                    },
                    selectImage = selectBitmap
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
                    BeNiceLabel(text = stringResource(id = R.string.n_ms, launchDelayMin.toInt()))
                    Slider(
                        value = delay,
                        onValueChange = { delay = it },
                        valueRange = (launchDelayMin..launchDelayMax),
                        steps = 14,
                        modifier = Modifier
                            .weight(1.0F)
                            .padding(vertical = 8.dp)
                    )
                    BeNiceLabel(
                        text = stringResource(
                            id = R.string.n_secs,
                            launchDelayMax.toInt() / 1000
                        )
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
                        onValueChange = { label = it })
                }
                if (bothAppsChosen) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppPairImage(
                                firstApp = firstApp!!,
                                secondApp = secondApp!!,
                                layout = AppPairIconLayout.Horizontal,
                                selected = layout == AppPairIconLayout.Horizontal
                            ) {
                                layout = AppPairIconLayout.Horizontal
                            }
                            AppPairImage(
                                firstApp = firstApp,
                                secondApp = secondApp,
                                layout = AppPairIconLayout.Diagonal,
                                selected = layout is AppPairIconLayout.Diagonal
                            ) {
                                layout = AppPairIconLayout.Diagonal
                            }
                            SelectableImage(
                                firstApp = firstApp,
                                secondApp = secondApp,
                                layout = AppPairIconLayout.CustomImage(customImage),
                                selected = layout is AppPairIconLayout.CustomImage,
                                selectBitmap = selectBitmap,
                                bitmapSelected = { bitmap, _ ->
                                    bitmap?.run {
                                        customImage = bitmap
                                        updateLayout()
                                    }
                                }) {
                                updateLayout()
                            }
                        }
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = stringResource(id = R.string.launchers_may_add_artwork),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (canAddPinnedShortcut) {
                    Row(
                        modifier = Modifier.clickable {
                            createPinnedShortcut = !createPinnedShortcut
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = stringResource(id = R.string.create_pinned_shortcut))
                        Checkbox(
                            checked = createPinnedShortcut,
                            onCheckedChange = { createPinnedShortcut = it })
                    }
                } else {
                    ErrorText(text = stringResource(R.string.cannot_create_pinned_shortcuts))
                }
                if (canAddDynamicShortcut) {
                    Row(
                        modifier = Modifier.clickable {
                            addDynamicShortcut = !addDynamicShortcut
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = stringResource(id = R.string.add_dynamic_shortcut))
                        Checkbox(
                            checked = addDynamicShortcut,
                            onCheckedChange = { addDynamicShortcut = it })
                    }
                } else {
                    ErrorText(text = stringResource(R.string.cannot_create_dynamic_shortcuts))
                }
                LearnMore(modifier = Modifier.fillMaxWidth())
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
    })
}

fun Activity.startActivityCatchExceptions(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
    }
}

private const val UNSPECIFIED = "???"
private fun label(firstApp: AppInfo?, secondApp: AppInfo?) =
    "${firstApp?.label ?: UNSPECIFIED} \u2011 ${secondApp?.label ?: UNSPECIFIED}"

private fun String.isTooLong() = length > 64
