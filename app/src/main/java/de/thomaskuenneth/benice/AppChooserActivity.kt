package de.thomaskuenneth.benice

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
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

    private fun onClick(appInfo: AppInfo) {
        with(appInfo) {
            launchApp(
                packageName = packageName,
                className = className,
                launchAdjacent = shouldLaunchAdjacent(
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

private fun WindowSizeClass.hasExpandedScreen() =
    windowWidthSizeClass == WindowWidthSizeClass.EXPANDED || windowHeightSizeClass == WindowHeightSizeClass.EXPANDED
