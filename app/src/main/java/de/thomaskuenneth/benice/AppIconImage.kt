package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

val APP_ICON_IMAGE_SIZE = 48.dp

@Composable
fun AppIconImage(drawable: Drawable) {
    val normalize: (Int) -> Int = { if (it < 108 || it > 512) 108 else it }
    val bitmap = Bitmap.createBitmap(
        normalize(drawable.intrinsicWidth), normalize(drawable.intrinsicHeight),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    Box(
        modifier = Modifier.size(APP_ICON_IMAGE_SIZE),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null
        )
    }
}
