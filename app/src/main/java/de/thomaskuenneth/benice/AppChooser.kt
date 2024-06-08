package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.res.ResourcesCompat


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppChooser(
    installedApps: List<AppInfo>,
    columns: Int,
    letterPosition: Int,
    showOpenInBrowser: Boolean,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
    selectImage: ((Bitmap?) -> Unit) -> Unit
) {
    val iconColor = MaterialTheme.colorScheme.primary.toArgb()
    val context = LocalContext.current
    val onDoneUrl: (String) -> Unit = { url ->
        onClick(
            AppInfo(
                packageName = MIME_TYPE_URL,
                className = url,
                label = url.createLabelFromURL(context.getString(R.string.open_url)),
                icon = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.baseline_open_in_browser_24,
                    context.theme
                )!!.also {
                    it.setTint(iconColor)
                }
            )
        )
    }
    val onDoneImage: (Drawable, String) -> Unit = { drawable, image ->
        onClick(
            AppInfo(
                packageName = MIME_TYPE_IMAGE,
                className = image,
                label = context.getString(R.string.image),
                icon = drawable
            )
        )
    }
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
            var counter = 0
            if (showOpenInBrowser) {
                item {
                    OpenInBrowserMenuItem(onDone = onDoneUrl)
                }
                item {
                    ShowImageMenuItem(
                        selectImage = selectImage,
                        onDone = onDoneImage
                    )
                }
            }
            installedApps.forEach { appInfo ->
                appInfo.label.ifEmpty { "?" }.substring((0 until 1)).uppercase().let { current ->
                    if (current != last) {
                        last = current
                        header(key = counter++) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp),
                                text = current,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = when (letterPosition) {
                                    0 -> TextAlign.Left
                                    1 -> TextAlign.Center
                                    else -> TextAlign.Right
                                },
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                item(key = counter++) {
                    AppChooserItem(
                        appInfo = appInfo,
                        modifier = Modifier.combinedClickable(
                            onClick = { onClick(appInfo) },
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
    appInfo: AppInfo, modifier: Modifier = Modifier
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
            text = appInfo.label.ifEmpty { stringResource(id = R.string.unknown) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AppChooserDialog(
    installedApps: List<AppInfo>,
    letterPosition: Int,
    onClick: (AppInfo) -> Unit,
    onDismissRequest: () -> Unit,
    selectImage: ((Bitmap?) -> Unit) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(size = 12.dp)
        ) {
            AppChooser(
                installedApps = installedApps,
                columns = 1,
                letterPosition = letterPosition,
                showOpenInBrowser = true,
                onClick = onClick,
                onLongClick = {},
                selectImage = selectImage
            )
        }
    }
}

@Composable
fun CompactAppChooser(
    installedApps: List<AppInfo>,
    letterPosition: Int,
    selectedApp: AppInfo?,
    @StringRes hint: Int,
    onItemClicked: (AppInfo) -> Unit,
    selectImage: ((Bitmap?) -> Unit) -> Unit,
) {
    var appChooserDialogOpen by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        if (selectedApp != null) {
            AppChooserItem(appInfo = selectedApp,
                modifier = Modifier.clickable { appChooserDialogOpen = true })
        } else {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
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
            letterPosition = letterPosition,
            onClick = { appInfo ->
                appChooserDialogOpen = false
                onItemClicked(appInfo)
            },
            onDismissRequest = {
                appChooserDialogOpen = false
            },
            selectImage = selectImage
        )
    }
}
