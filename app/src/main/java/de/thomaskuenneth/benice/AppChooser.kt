package de.thomaskuenneth.benice

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppChooser(
    installedApps: List<AppInfo>,
    columns: Int,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongClick: (AppInfo) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    if (installedApps.isEmpty()) {
        Text(
            text = stringResource(R.string.no_apps),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )
    } else {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(), columns = GridCells.Fixed(count = columns)
        ) {
            var last = ""
            installedApps.forEach { appInfo ->
                appInfo.label.substring((0 until 1)).let { current ->
                    if (current != last) {
                        last = current
                        header {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                text = current,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                item {
                    AppChooserItem(
                        appInfo = appInfo,
                        modifier = Modifier.combinedClickable(
                            onClick = { onClick(appInfo, false) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick(appInfo)
                            },
                            onLongClickLabel = stringResource(id = R.string.open_context_menu)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AppChooserItem(
    appInfo: AppInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
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

@Composable
fun AppChooserDialog(
    installedApps: List<AppInfo>,
    onClick: (AppInfo, Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(size = 12.dp)
        ) {
            AppChooser(installedApps = installedApps,
                columns = 1,
                onClick = onClick,
                onLongClick = {})
        }
    }
}

@Composable
fun CompactAppChooser(
    installedApps: List<AppInfo>,
    selectedApp: AppInfo?,
    @StringRes hint: Int,
    onItemClicked: (AppInfo) -> Unit
) {
    var appChooserDialogOpen by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        if (selectedApp != null) {
            AppChooserItem(
                appInfo = selectedApp,
                modifier = Modifier.clickable { appChooserDialogOpen = true }
            )
        } else {
            TextButton(onClick = {
                appChooserDialogOpen = true
            }) {
                Text(
                    text = stringResource(id = hint)
                )
            }
        }
    }
    if (appChooserDialogOpen) {
        AppChooserDialog(
            installedApps = installedApps,
            onClick = { appInfo, _ ->
                appChooserDialogOpen = false
                onItemClicked(appInfo)
            },
            onDismissRequest = {
                appChooserDialogOpen = false
            }
        )
    }
}
