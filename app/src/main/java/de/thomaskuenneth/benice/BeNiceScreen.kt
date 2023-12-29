package de.thomaskuenneth.benice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BeNiceScreen(
    installedAppsFlow: StateFlow<List<AppInfo>>,
    onClick: (AppInfo) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit,
    modifier: Modifier
) {
    var contextMenuAppInfo by rememberSaveable { mutableStateOf<AppInfo?>(null) }
    val haptics = LocalHapticFeedback.current
    val state by installedAppsFlow.collectAsState()
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    )
    {
        if (state.isEmpty()) {
            CircularProgressIndicator()
        }
        LazyColumn {
            state.forEach { appInfo ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onClick(appInfo) },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    contextMenuAppInfo = appInfo
                                },
                                onLongClickLabel = stringResource(id = R.string.open_context_menu)
                            )
                            .padding(all = 16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconImage(drawable = appInfo.icon)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = appInfo.label, style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
        if (contextMenuAppInfo != null) {
            ModalBottomSheet(
                onDismissRequest = { contextMenuAppInfo = null },
                windowInsets = WindowInsets(0),
            ) {
                MenuItem(
                    onClick = {
                        contextMenuAppInfo?.let {
                            onClick(it)
                        }
                        contextMenuAppInfo = null
                    },
                    imageRes = R.drawable.baseline_launch_24,
                    textRes = R.string.launch
                )
                MenuItem(
                    onClick = {
                        contextMenuAppInfo?.let {
                            onAddLinkClicked(it)
                        }
                        contextMenuAppInfo = null
                    },
                    imageRes = R.drawable.baseline_add_link_24,
                    textRes = R.string.add_link
                )
            }
        }
    }
}
