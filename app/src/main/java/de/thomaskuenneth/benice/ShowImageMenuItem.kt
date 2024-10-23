package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun ShowImageMenuItem(
    selectBitmap: () -> Unit,
    bitmapSelected: (Bitmap?, Uri?) -> Unit,
    viewModel: ShowImageMenuItemViewModel = viewModel(ShowImageMenuItemViewModel::class.java)
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit) {
        scope.launch {
            viewModel.bitmap.collect {
                bitmapSelected(it, viewModel.uri.value)
            }
        }
    }
    MenuItem(
        onClick = {
            viewModel.setBitmap(null)
            selectBitmap()
        },
        imageVector = Icons.Default.Image,
        textRes = R.string.show_image
    )
}
