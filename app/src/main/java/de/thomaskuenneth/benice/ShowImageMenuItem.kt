package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.DrawableCompat

@Composable
fun ShowImageMenuItem(
    selectImage: ((Bitmap?) -> Unit) -> Unit,
    onDone: (Drawable, String) -> Unit
) {
    val resources = LocalContext.current.resources
    Column {
        MenuItem(
            onClick = {
                selectImage { it ?.let { bitmap ->
                    println("===> Hello")
                    onDone(
                        BitmapDrawable(resources, bitmap),
                        "Hello"
                    )
                }
                }
            },
            imageVector = Icons.Default.Image,
            textRes = R.string.show_image
        )
    }
}
