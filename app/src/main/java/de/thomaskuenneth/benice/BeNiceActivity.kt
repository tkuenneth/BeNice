package de.thomaskuenneth.benice

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp


class AppPickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val intent = Intent(Intent.ACTION_MAIN).also {
            it.addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val list = mutableListOf<AppInfo>()
        val activities = packageManager.queryIntentActivities(intent, 0)
        activities.forEach { info ->
            if (info.activityInfo.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                list.add(
                    AppInfo(
                        icon = info.activityInfo.loadIcon(packageManager),
                        label = info.activityInfo.loadLabel(packageManager).toString(),
                        packageName = info.activityInfo.packageName,
                        className = info.activityInfo.name
                    )
                )
            }
        }
        setContent {
            MaterialTheme(
                colorScheme = defaultColorScheme()
            ) {
                Box(Modifier.statusBarsPadding()) {
                    TwoApps(
                        appInfoList = list.sortedBy { it.label },
                        onClick = ::onClick
                    )
                }
            }
        }
    }

    private fun onClick(appInfo: AppInfo) {
        with(appInfo) {
            Intent().run {
                component = ComponentName(
                    packageName,
                    className
                )
                addFlags(
                    FLAG_ACTIVITY_LAUNCH_ADJACENT or
                            FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(this)
            }
        }
    }
}

@Composable
fun TwoApps(appInfoList: List<AppInfo>, onClick: (AppInfo) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        appInfoList.forEach { appInfo ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClick(appInfo)
                        }
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconImage(drawable = appInfo.icon)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = appInfo.label, style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun AppIconImage(drawable: Drawable) {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            "An image"
        )
    }
}
