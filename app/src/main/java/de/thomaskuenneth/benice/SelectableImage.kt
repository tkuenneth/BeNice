package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SelectableImage(
    firstApp: AppInfo,
    secondApp: AppInfo,
    layout: AppPairIconLayout.CustomImage,
    selected: Boolean,
    selectBitmap: () -> Unit,
    bitmapSelected: (Bitmap?, Uri?) -> Unit,
    viewModel: ShowImageMenuItemViewModel = viewModel(ShowImageMenuItemViewModel::class.java),
    onClick: () -> Unit
) {
    val hasBitmap = layout.bitmap != null
    val chooseImage = @Composable {
        FilledTonalIconButton(onClick = {
            selectBitmap()
            viewModel.awaitBitmap { bitmapSelected(it, viewModel.uri.value) }
        }) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = stringResource(R.string.choose_image)
            )
        }
    }
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasBitmap) {
            AppPairImage(
                firstApp = firstApp,
                secondApp = secondApp,
                layout = layout,
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(8.dp))
            chooseImage()
        } else {
            Box(modifier = Modifier.size(BOX_SIZE), contentAlignment = Alignment.Center) {
                chooseImage()
            }
        }
    }
}
