package de.thomaskuenneth.benice

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.WindowMetricsCalculator

private const val KEY_LETTER_POSITION = "letterPosition"

class AppChooserActivity : ComponentActivity() {

    private lateinit var shortcutManager: ShortcutManager
    private lateinit var prefs: SharedPreferences
    private lateinit var windowSizeClass: WindowSizeClass
    private lateinit var viewModel: ShowImageMenuItemViewModel

    private val launcher: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.setUri(uri)
            viewModel.setBitmap(if (uri == null) {
                null
            } else {
                contentResolver.takePersistableUriPermission(
                    uri,
                    FLAG_GRANT_READ_URI_PERMISSION
                )
                val source = ImageDecoder.createSource(
                    contentResolver, uri
                )
                ImageDecoder.decodeBitmap(
                    source
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            })
        }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ShowImageMenuItemViewModel::class.java]
        shortcutManager = getSystemService(ShortcutManager::class.java)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        enableEdgeToEdge()
        val viewModel by viewModels<BeNiceViewModel>()
        viewModel.setLetterPosition(prefs.getInt(KEY_LETTER_POSITION, 1))
        viewModel.setAppVersionString(appVersionString = appVersion())
        windowSizeClass = computeWindowSizeClass()
        setContent {
            MaterialTheme(
                colorScheme = defaultColorScheme()
            ) {
                val state by viewModel.uiState.collectAsState()
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                var settingsOpen by remember { mutableStateOf(false) }
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) },
                            scrollBehavior = scrollBehavior,
                            actions = {
                                IconButtonWithTooltip(
                                    onClick = { settingsOpen = true },
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(id = R.string.settings)
                                )
                            })
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
                        selectImage = ::selectImage,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues)
                            .background(color = MaterialTheme.colorScheme.background)
                    )
                    SettingsScreen(isOpen = settingsOpen, viewModel = viewModel, sheetClosed = {
                        settingsOpen = false
                        prefs.edit().putInt(KEY_LETTER_POSITION, state.letterPosition).apply()
                    })
                }
            }
        }
        viewModel.queryInstalledApps(packageManager)
    }

    private fun onClick(appInfo: AppInfo) {
        with(appInfo) {
            launchApp(
                packageName = packageName, className = className, launchAdjacent = true
            )
        }
    }

    private fun onAddLinkClicked(appInfo: AppInfo) {
        if (shortcutManager.isRequestPinShortcutSupported) {
            val shortcutInfo = ShortcutInfo.Builder(this, appInfo.className)
                .setIcon(Icon.createWithAdaptiveBitmap(appInfo.icon.toBitmap()))
                .setShortLabel(appInfo.label).setIntent(createBeNiceLaunchIntent(appInfo)).build()
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    private fun onOpenAppInfoClicked(appInfo: AppInfo) {
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).run {
            data = Uri.parse("package:${appInfo.packageName}")
            addFlags(
                FLAG_ACTIVITY_LAUNCH_ADJACENT or FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivityCatchExceptions(this)
        }
    }

    private fun onAppsForAppPairSelected(
        firstApp: AppInfo, secondApp: AppInfo, delay: Long, label: String
    ) {
        if (shortcutManager.isRequestPinShortcutSupported) {
            val id = "${firstApp.className}|${secondApp.className}"
            val shortcutInfo = ShortcutInfo.Builder(this, id).setIcon(
                Icon.createWithBitmap(
                    createAppPairBitmap(
                        firstApp = firstApp, secondApp = secondApp
                    )
                )
            ).setShortLabel(label).setIntent(createLaunchAppPairIntent(firstApp, secondApp, delay))
                .build()
            shortcutManager.requestPinShortcut(shortcutInfo, null)
        }
    }

    private fun selectImage() {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

fun createAppPairBitmap(
    firstApp: AppInfo, secondApp: AppInfo
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
