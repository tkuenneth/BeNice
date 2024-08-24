package de.thomaskuenneth.benice

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun AppPairImage(
    firstApp: AppInfo,
    secondApp: AppInfo,
    layout: AppPairIconLayout,
    selected: Boolean,
    onClick: () -> Unit
) {
    val size = with(LocalDensity.current) { 64.dp.toPx() }.toInt()
    Box(
        modifier = Modifier
            .size(96.dp, 96.dp)
            .then(
                if (selected) Modifier.border(
                    width = 1.dp, color = MaterialTheme.colorScheme.primary
                ) else Modifier
            )
            .clickable {
                onClick()
            }, contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = createAppPairBitmap(
                firstApp = firstApp,
                secondApp = secondApp,
                bigWidth = size,
                bigHeight = size,
                layout = layout
            ).asImageBitmap(),
            contentDescription = stringResource(id = if (layout == AppPairIconLayout.HORIZONTAL) R.string.arrange_horizontally else R.string.arrange_vertically),
            modifier = Modifier
        )
    }
}
