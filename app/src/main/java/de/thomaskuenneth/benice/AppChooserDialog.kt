package de.thomaskuenneth.benice

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AppChooserDialog(
    isVisible: Boolean,
    installedApps: List<AppInfo>,
    letterPosition: Int,
    onClick: (AppInfo) -> Unit,
    onDismissRequest: () -> Unit,
    selectImage: () -> Unit
) {
    if (isVisible) {
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
                    showSpecials = true,
                    onClick = onClick,
                    onLongClick = {},
                    selectBitmap = selectImage
                )
            }
        }
    }
}
