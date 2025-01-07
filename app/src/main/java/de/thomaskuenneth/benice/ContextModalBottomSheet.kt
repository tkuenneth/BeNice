package de.thomaskuenneth.benice

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextModalBottomSheet(
    contextMenuAppInfo: AppInfo,
    sheetState: SheetState,
    closeSheet: (() -> Unit) -> Unit,
    onLaunchClicked: (AppInfo, Boolean) -> Unit,
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
            onClick = { closeSheet { onLaunchClicked(contextMenuAppInfo, true) } },
            textRes = R.string.launch_adjacent
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
            )
        }
        MenuItem(
            onClick = { closeSheet { onLaunchClicked(contextMenuAppInfo, false) } },
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
