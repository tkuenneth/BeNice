package de.thomaskuenneth.benice

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuItem(
    enabled: Boolean = true, onClick: () -> Unit, imageVector: ImageVector, @StringRes textRes: Int
) {
    MenuItem(
        enabled = enabled,
        onClick = onClick,
        textRes = textRes,
    ) @Composable {
        Image(
            imageVector = imageVector,
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun MenuItem(
    enabled: Boolean = true,
    onClick: () -> Unit,
    @StringRes textRes: Int,
    content: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) {
                Modifier.clickable {
                    onClick()
                }
            } else {
                Modifier
            })
            .padding(all = 16.dp)
            .alpha(if (enabled) 1.0F else 0.6F)) {
        content()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = textRes), color = MaterialTheme.colorScheme.primary
        )
    }
}
