package de.thomaskuenneth.benice

import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppChooser(
    installedApps: List<AppInfo>,
    columns: Int,
    letterPosition: Int,
    showSpecials: Boolean,
    dynamicShortcuts: List<ShortcutInfo>,
    createShortcutIcon: (ShortcutInfo) -> Drawable,
    onShortcutClicked: (ShortcutInfo) -> Unit,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongClick: (AppInfo) -> Unit,
    selectBitmap: () -> Unit,
    shouldAddEmptySpace: Boolean = false
) {
    val iconColor = MaterialTheme.colorScheme.primary.toArgb()
    val context = LocalContext.current
    val resources = LocalResources.current
    val strOpenUrl = stringResource(R.string.open_url)
    val imageLabel = stringResource(R.string.image)
    val onDoneUrl: (String) -> Unit = { url ->
        onClick(
            AppInfo(
                packageName = MIME_TYPE_URL,
                className = url,
                label = url.createLabelFromURL(strOpenUrl),
                icon = ResourcesCompat.getDrawable(
                    resources, R.drawable.baseline_open_in_browser_24, context.theme
                )!!.also {
                    it.setTint(iconColor)
                }), true
        )
    }
    val haptics = LocalHapticFeedback.current
    val displayCutoutPadding = WindowInsets.displayCutout.asPaddingValues()
    val left = displayCutoutPadding.calculateLeftPadding(LocalLayoutDirection.current)
    val right = displayCutoutPadding.calculateRightPadding(LocalLayoutDirection.current)
    if (installedApps.isEmpty() && dynamicShortcuts.isEmpty()) {
        Text(
            text = stringResource(R.string.no_apps),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )
    } else {
        val bottomPadding =
            with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
        val lazyGridState = rememberLazyGridState()
        LaunchedEffect(dynamicShortcuts) {
            lazyGridState.animateScrollToItem(0)
        }
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(count = columns),
            state = lazyGridState
        ) {
            if (dynamicShortcuts.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val chunkedShortcuts = remember(dynamicShortcuts, columns) {
                        dynamicShortcuts.chunked(columns)
                    }
                    Column(
                        modifier = Modifier
                            .padding(
                                start = maxOf(left, 16.dp),
                                end = maxOf(right, 16.dp),
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.shortcut_info_06),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        chunkedShortcuts.forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { shortcut ->
                                    key(shortcut.id) {
                                        ShortcutLauncher(
                                            icon = createShortcutIcon(shortcut),
                                            label = shortcut.shortLabel.toString(),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(shape = MaterialTheme.shapes.small)
                                                .clickable { onShortcutClicked(shortcut) }
                                        )
                                    }
                                }
                                repeat(columns - rowItems.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            if (showSpecials) {
                item {
                    OpenInBrowserMenuItem(onDone = onDoneUrl)
                }
                item {
                    ShowImageMenuItem(selectBitmap = selectBitmap, bitmapSelected = { bitmap, uri ->
                        bitmap?.run {
                            onClick(
                                AppInfo(
                                    packageName = MIME_TYPE_IMAGE,
                                    className = uri.toString(),
                                    label = imageLabel,
                                    icon = bitmap.toDrawable(resources)
                                ), true
                            )
                        }
                    })
                }
            }
            val itemsWithHeaders = mutableListOf<Any>()
            var last = ""
            installedApps.forEach { appInfo ->
                val current =
                    appInfo.label.ifEmpty { "?" }.substring((0 until 1)).uppercase()
                if (current != last) {
                    last = current
                    itemsWithHeaders.add(current)
                }
                itemsWithHeaders.add(appInfo)
            }
            items(
                items = itemsWithHeaders,
                key = { item -> if (item is AppInfo) "${item.packageName}/${item.className}" else item },
                span = { item ->
                    GridItemSpan(if (item is String) maxLineSpan else 1)
                }) { item ->
                if (item is String) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = maxOf(left, 16.dp),
                                end = maxOf(right, 16.dp),
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .clip(shape = MaterialTheme.shapes.small)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = when (letterPosition) {
                            0 -> Alignment.CenterStart
                            1 -> Alignment.Center
                            else -> Alignment.CenterEnd
                        }
                    ) {
                        Text(
                            modifier = Modifier.width(APP_ICON_IMAGE_SIZE),
                            text = item,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (item is AppInfo) {
                    AppChooserItem(
                        appInfo = item, modifier = Modifier
                            .padding(
                                start = maxOf(left, 16.dp),
                                end = maxOf(right, 16.dp),
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .clip(shape = MaterialTheme.shapes.small)
                            .combinedClickable(
                                onClick = { onClick(item, true) },
                                onDoubleClick = { onClick(item, false) },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick(item)
                                },
                                onLongClickLabel = stringResource(id = R.string.open_context_menu)
                            )
                    )
                }
            }
            if (shouldAddEmptySpace) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(
                        modifier = Modifier.padding(
                            bottom = bottomPadding
                        )
                    )
                }
            }
        }
    }
}
