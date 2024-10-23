package de.thomaskuenneth.benice

import android.graphics.Bitmap

sealed class AppPairIconLayout {
    data object Horizontal : AppPairIconLayout()
    data object Diagonal : AppPairIconLayout()
    data class CustomImage(val bitmap: Bitmap?) : AppPairIconLayout()
}
