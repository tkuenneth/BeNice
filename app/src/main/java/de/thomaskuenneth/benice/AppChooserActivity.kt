package de.thomaskuenneth.benice

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowMetricsCalculator

private const val PREFS_FILTER_ON = "filterOn"
private const val PREFS_LAUNCH_ADJACENT = "launchAdjacent"

class AppChooserActivity : ComponentActivity() {

    private lateinit var shortcutManager: ShortcutManager
    private lateinit var prefs: SharedPreferences
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortcutManager = getSystemService(ShortcutManager::class.java)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        enableEdgeToEdge()
        val viewModel by viewModels<BeNiceViewModel>()
        viewModel.setFilterOn(prefs.getBoolean(PREFS_FILTER_ON, true))
        viewModel.setLaunchAdjacent(prefs.getBoolean(PREFS_LAUNCH_ADJACENT, true))
        windowSizeClass = computeWindowSizeClass()
        setContent {
            MaterialTheme(
                colorScheme = defaultColorScheme()
            ) {
                val state by viewModel.uiState.collectAsState()
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                Scaffold(
                    topBar = {
                        var moreOpen by remember { mutableStateOf(false) }
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.app_name)) },
                            scrollBehavior = scrollBehavior,
                            actions = {
                                IconButtonWithTooltip(
                                    onClick = {
                                        val newValue = !state.filterOn
                                        viewModel.setFilterOn(newValue)
                                        prefs.edit().putBoolean(PREFS_FILTER_ON, newValue).apply()
                                    },
                                    imageVector = if (state.filterOn) {
                                        Icons.Default.FilterAlt
                                    } else {
                                        Icons.Default.FilterAltOff
                                    },
                                    contentDescription = stringResource(
                                        id = if (state.filterOn) {
                                            R.string.filter_on
                                        } else {
                                            R.string.filter_off
                                        }
                                    )
                                )
                                val menuItems = mutableListOf<@Composable () -> Unit>()
                                if (!windowSizeClass.hasExpandedScreen()) {
                                    menuItems.add {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(id = R.string.launch_adjacent)
                                                )
                                            },
                                            leadingIcon = {
                                                val checked = state.launchAdjacent
                                                Icon(
                                                    imageVector = if (checked) {
                                                        Icons.Default.CheckBox
                                                    } else {
                                                        Icons.Default.CheckBoxOutlineBlank
                                                    },
                                                    contentDescription = stringResource(
                                                        id = if (checked) {
                                                            R.string.checked
                                                        } else {
                                                            R.string.not_checked
                                                        }
                                                    )
                                                )
                                            },
                                            onClick = {
                                                val newValue = !state.launchAdjacent
                                                viewModel.setLaunchAdjacent(newValue)
                                                prefs.edit()
                                                    .putBoolean(
                                                        PREFS_LAUNCH_ADJACENT,
                                                        newValue
                                                    )
                                                    .apply()
                                                moreOpen = false
                                            })
                                    }
                                }
                                if (menuItems.isNotEmpty()) {
                                    IconButtonWithTooltip(
                                        onClick = { moreOpen = true },
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(id = R.string.more_vert)
                                    )
                                    DropdownMenu(
                                        expanded = moreOpen,
                                        onDismissRequest = { moreOpen = false }
                                    ) {
                                        menuItems.forEach { item -> item() }
                                    }
                                }
                            }
                        )
                    },
                    // currently necessary to achieve edge to edge at the bottom
                    bottomBar = { Spacer(modifier = Modifier.height(0.dp)) },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { paddingValues ->
                    BeNiceScreen(
                        windowSizeClass = windowSizeClass,
                        state = state,
                        onClick = ::onClick,
                        onAddLinkClicked = ::onAddLinkClicked,
                        onOpenAppInfoClicked = ::onOpenAppInfoClicked,
                        onAppsForAppPairSelected = ::onAppsForAppPairSelected,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues)
                            .background(color = MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
        viewModel.queryInstalledApps(packageManager)
    }

    private fun onClick(appInfo: AppInfo, forceLaunchAdjacent: Boolean) {
        with(appInfo) {
            launchApp(
                packageName = packageName,
                className = className,
                launchAdjacent = forceLaunchAdjacent || shouldLaunchAdjacent(
                    prefs = prefs,
                    windowSizeClass = windowSizeClass
                )
            )
        }
    }

    private fun onAddLinkClicked(appInfo: AppInfo) {
        if (shortcutManager.isRequestPinShortcutSupported) {
            val shortcutInfo = ShortcutInfo.Builder(this, appInfo.className)
                .setIcon(Icon.createWithAdaptiveBitmap(appInfo.icon.toBitmap()))
                .setShortLabel(appInfo.label)
                .setIntent(createBeNiceLaunchIntent(appInfo))
                .build()
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    private fun onOpenAppInfoClicked(appInfo: AppInfo) {
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).run {
            data = Uri.parse("package:${appInfo.packageName}")
            addFlags(
                FLAG_ACTIVITY_LAUNCH_ADJACENT or
                        FLAG_ACTIVITY_NEW_TASK or
                        FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivity(this)
        }
    }

    private fun onAppsForAppPairSelected(firstApp: AppInfo, secondApp: AppInfo) {
        if (shortcutManager.isRequestPinShortcutSupported) {
            val id = "${firstApp.className}|${secondApp.className}"
            val label = "${firstApp.label} \u2011 ${secondApp.label}"
            val shortcutInfo = ShortcutInfo.Builder(this, id)
                .setIcon(
                    Icon.createWithBitmap(
                        createAppPairBitmap(
                            firstApp = firstApp,
                            secondApp = secondApp
                        )
                    )
                )
                .setShortLabel(label)
                .setIntent(createLaunchAppPairIntent(firstApp, secondApp))
                .build()
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }
}

fun createAppPairBitmap(
    firstApp: AppInfo,
    secondApp: AppInfo
): Bitmap {
    val bigWidth = 512
    val bigHeight = 512
    val smallWidth = bigWidth / 2
    val smallHeight = bigHeight / 2
    val y = smallHeight / 2F
    return Bitmap.createBitmap(bigWidth, bigHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
        val bitmapPaint = Paint().also { paint ->
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
        }
        Canvas(bitmap).run {
            drawPaint(Paint().also {
                it.color = Color.TRANSPARENT
            })
            drawBitmap(firstApp.icon.toBitmap(smallWidth, smallHeight), 0F, y, bitmapPaint)
            drawBitmap(
                secondApp.icon.toBitmap(smallWidth, smallHeight),
                smallWidth.toFloat(),
                y,
                bitmapPaint
            )
        }
    }
}

fun Activity.computeWindowSizeClass(): WindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    val width = metrics.bounds.width()
    val height = metrics.bounds.height()
    val density = resources.displayMetrics.density
    return WindowSizeClass.compute(width / density, height / density)
}

fun shouldLaunchAdjacent(
    prefs: SharedPreferences,
    windowSizeClass: WindowSizeClass
) = prefs.getBoolean(PREFS_LAUNCH_ADJACENT, true) ||
        windowSizeClass.hasExpandedScreen()

fun WindowSizeClass.hasExpandedScreen() =
    windowWidthSizeClass == WindowWidthSizeClass.EXPANDED || windowHeightSizeClass == WindowHeightSizeClass.EXPANDED
