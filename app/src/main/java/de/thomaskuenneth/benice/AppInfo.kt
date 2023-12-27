package de.thomaskuenneth.benice

import android.graphics.drawable.Drawable


data class AppInfo(
    val icon: Drawable,
    val label: String,
    val packageName: String,
    val className: String
)
