package de.thomaskuenneth.benice

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun CompactAppChooser(
    installedApps: List<AppInfo>,
    letterPosition: Int,
    selectedApp: AppInfo?,
    @StringRes hint: Int,
    onItemClicked: (AppInfo) -> Unit,
    selectImage: () -> Unit,
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
    AppChooserDialog(
        isVisible = appChooserDialogOpen,
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
