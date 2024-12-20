package de.thomaskuenneth.benice

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextModalBottomSheet(
    contextMenuAppInfo: AppInfo,
    sheetState: SheetState,
    closeSheet: (() -> Unit) -> Unit,
    onLaunchClicked: (AppInfo) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit,
    onOpenAppInfoClicked: (AppInfo) -> Unit,
    onCreateAppPairClicked: (AppInfo) -> Unit,
    onCopyNamesClicked: (AppInfo) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        MenuItem(
            onClick = { closeSheet { onLaunchClicked(contextMenuAppInfo) } },
            imageVector = Icons.AutoMirrored.Filled.Launch,
            textRes = R.string.launch
        )
        MenuItem(
            onClick = { closeSheet { onOpenAppInfoClicked(contextMenuAppInfo) } },
            imageVector = Icons.Default.Info,
            textRes = R.string.open_app_info
        )
        MenuItem(
            onClick = { closeSheet { onAddLinkClicked(contextMenuAppInfo) } },
            imageVector = Icons.Default.AddLink,
            textRes = R.string.add_link
        )
        MenuItem(
            onClick = { closeSheet { onCreateAppPairClicked(contextMenuAppInfo) } },
            imageVector = Icons.Default.Create,
            textRes = R.string.create_app_pair
        )
        MenuItem(
            onClick = { closeSheet { onCopyNamesClicked(contextMenuAppInfo) } },
            imageVector = Icons.Default.CopyAll,
            textRes = R.string.copy_names
        )
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
