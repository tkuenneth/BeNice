package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ShowImageMenuItem(
    selectBitmap: () -> Unit,
    bitmapSelected: (Bitmap?, Uri?) -> Unit,
    viewModel: ShowImageMenuItemViewModel = viewModel(ShowImageMenuItemViewModel::class.java)
) {
    MenuItem(
        onClick = {
            viewModel.setBitmap(null)
            selectBitmap()
            viewModel.awaitBitmap { bitmapSelected(it, viewModel.uri.value) }
        },
        imageVector = Icons.Default.Image,
        textRes = R.string.show_image
    )
}
