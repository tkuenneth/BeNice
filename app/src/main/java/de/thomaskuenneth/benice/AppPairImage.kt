package de.thomaskuenneth.benice

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

val BOX_SIZE = 96.dp

@Composable
fun AppPairImage(
    firstApp: AppInfo,
    secondApp: AppInfo, layout: AppPairIconLayout,
    selected: Boolean,
    onClick: () -> Unit
) {
    val size = with(LocalDensity.current) { 64.dp.toPx() }.toInt()
    Box(
        modifier = Modifier
            .size(BOX_SIZE, BOX_SIZE)
            .appPairImageBorder(selected)
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
            ).asImageBitmap(), contentDescription = stringResource(
                when (layout) {
                    is AppPairIconLayout.Horizontal -> R.string.arrange_horizontally
                    is AppPairIconLayout.Diagonal -> R.string.arrange_diagonally
                    is AppPairIconLayout.CustomImage -> R.string.show_custom_icon
                }
            ), modifier = Modifier
        )
    }
}
