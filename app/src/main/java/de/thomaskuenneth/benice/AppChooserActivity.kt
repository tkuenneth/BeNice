package de.thomaskuenneth.benice

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
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
import android.os.Bundle
import android.widget.Toast
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
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withSave
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.WindowMetricsCalculator

private const val KEY_LETTER_POSITION = "letterPosition"
private const val KEY_TWO_COLUMNS_ON_SMALL_SCREENS = "twoColumnsOnSmallScreens"
private const val KEY_THREE_COLUMNS_ON_MEDIUM_SCREENS = "threeColumnsOnMediumScreens"
private const val KEY_TWO_COLUMNS_ON_LARGE_SCREENS = "twoColumnsOnLargeScreens"

class AppChooserActivity : ComponentActivity() {

    private lateinit var shortcutManager: ShortcutManager
    private lateinit var prefs: SharedPreferences
    private lateinit var windowSizeClass: WindowSizeClass
    private lateinit var viewModel: ShowImageMenuItemViewModel
    private lateinit var clipboardManager: ClipboardManager

    private val launcher: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.setUri(uri)
            viewModel.setBitmap(
                if (uri == null) {
                null
            } else {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, FLAG_GRANT_READ_URI_PERMISSION
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
                } catch (_: SecurityException) {
                    Toast.makeText(this, R.string.could_not_obtain_picture, Toast.LENGTH_LONG)
                        .show()
                    null
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
        with(viewModel) {
            setLetterPosition(prefs.getInt(KEY_LETTER_POSITION, 1))
            setAppVersionString(appVersionString = appVersion())
            setTwoColumnsOnSmallScreens(
                prefs.getBoolean(
                    KEY_TWO_COLUMNS_ON_SMALL_SCREENS, false
                )
            )
            setThreeColumnsOnMediumScreens(
                prefs.getBoolean(
                    KEY_THREE_COLUMNS_ON_MEDIUM_SCREENS, false
                )
            )
            setTwoColumnsOnLargeScreens(
                prefs.getBoolean(
                    KEY_TWO_COLUMNS_ON_LARGE_SCREENS, false
                )
            )
        }
        windowSizeClass = computeWindowSizeClass()
        clipboardManager = getSystemService(ClipboardManager::class.java)
        setContent {
            MaterialTheme(
                colorScheme = defaultColorScheme()
            ) {
                val state by viewModel.uiState.collectAsState()
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                var settingsOpen by remember { mutableStateOf(false) }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = stringResource(id = R.string.app_name)) },
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
                        canAddDynamicShortcut = shortcutManager.maxShortcutCountPerActivity > shortcutManager.dynamicShortcuts.size,
                        windowSizeClass = windowSizeClass,
                        state = state,
                        onClick = ::onClick,
                        onAddLinkClicked = ::onAddLinkClicked,
                        onOpenAppInfoClicked = ::onOpenAppInfoClicked,
                        onAppsForAppPairSelected = ::onAppsForAppPairSelected,
                        onCopyNamesClicked = ::onCopyNamesClicked,
                        selectBitmap = ::selectBitmap,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues)
                            .background(color = MaterialTheme.colorScheme.background)
                    )
                    SettingsScreen(
                        removeDynamicShortcutsEnabled = shortcutManager.dynamicShortcuts.isNotEmpty(),
                        isOpen = settingsOpen,
                        viewModel = viewModel,
                        sheetClosed = {
                            settingsOpen = false
                            with(state) {
                                prefs.edit {
                                    putInt(KEY_LETTER_POSITION, letterPosition).putBoolean(
                                        KEY_TWO_COLUMNS_ON_SMALL_SCREENS, twoColumnsOnSmallScreens
                                    ).putBoolean(
                                        KEY_THREE_COLUMNS_ON_MEDIUM_SCREENS,
                                        threeColumnsOnMediumScreens
                                    ).putBoolean(
                                        KEY_TWO_COLUMNS_ON_LARGE_SCREENS, twoColumnsOnLargeScreens
                                    )
                                }
                            }
                        },
                        removeAllDynamicShortcutsCallback = {
                            shortcutManager.removeDynamicShortcuts(shortcutManager.dynamicShortcuts.map { it.id })
                        })
                }
            }
        }
        viewModel.queryInstalledApps(packageManager)
    }

    private fun onClick(appInfo: AppInfo, launchAdjacent: Boolean) {
        with(appInfo) {
            launchApp(
                packageName = packageName, className = className, launchAdjacent = launchAdjacent
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
            data = "package:${appInfo.packageName}".toUri()
            addFlags(
                FLAG_ACTIVITY_LAUNCH_ADJACENT or FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivityCatchExceptions(this)
        }
    }

    private fun onAppsForAppPairSelected(
        firstApp: AppInfo,
        secondApp: AppInfo,
        delay: Long,
        label: String,
        addDynamicShortcut: Boolean,
        layout: AppPairIconLayout
    ) {
        if (shortcutManager.isRequestPinShortcutSupported) {
            val id = "${firstApp.className}|${secondApp.className}"
            val shortcutInfo = ShortcutInfo.Builder(this, id).setIcon(
                Icon.createWithBitmap(
                    createAppPairBitmap(
                        firstApp = firstApp,
                        secondApp = secondApp,
                        bigWidth = shortcutManager.iconMaxWidth,
                        bigHeight = shortcutManager.iconMaxHeight,
                        layout = layout
                    )
                )
            ).setShortLabel(label).setIntent(createLaunchAppPairIntent(firstApp, secondApp, delay))
                .build()
            shortcutManager.requestPinShortcut(shortcutInfo, null)
            shortcutManager.maxShortcutCountPerActivity
            if (addDynamicShortcut) {
                shortcutManager.addDynamicShortcuts(listOf(shortcutInfo))
            }
        }
    }

    private fun selectBitmap() {
        viewModel.setBitmap(null)
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun onCopyNamesClicked(appInfo: AppInfo) {
        val text = "${appInfo.packageName}\n${appInfo.className}"
        clipboardManager.setPrimaryClip(ClipData.newPlainText(text, text))
    }
}

fun createAppPairBitmap(
    firstApp: AppInfo, secondApp: AppInfo, bigWidth: Int, bigHeight: Int, layout: AppPairIconLayout
): Bitmap {
    return createBitmap(bigWidth, bigHeight).also { bitmap ->
        val bitmapPaint = Paint().also { paint ->
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
        }
        Canvas(bitmap).run {
            if (layout !is AppPairIconLayout.CustomImage) {
                val smallWidth = bigWidth / 2
                val smallHeight = bigHeight / 2
                val verticalMargin = smallHeight / 2F
                val topFirstBitmap =
                    if (layout == AppPairIconLayout.Diagonal) smallHeight.toFloat() else verticalMargin
                val topSecondBitmap =
                    if (layout == AppPairIconLayout.Diagonal) 0F else verticalMargin
                val firstBitmap = firstApp.icon.toBitmap(smallWidth, smallHeight)
                val secondBitmap = secondApp.icon.toBitmap(smallWidth, smallHeight)
                drawPaint(Paint().also {
                    it.color = Color.TRANSPARENT
                })
                withSave {
                    density = firstBitmap.density
                    drawBitmap(firstBitmap, 0F, topFirstBitmap, bitmapPaint)
                }
                density = secondBitmap.density
                drawBitmap(
                    secondBitmap, smallWidth.toFloat(), topSecondBitmap, bitmapPaint
                )
            } else {
                layout.bitmap?.let { bitmap ->
                    val scaled = bitmap.scale(bigWidth, bigHeight)
                    drawBitmap(scaled, 0F, 0F, bitmapPaint)
                    scaled.recycle()
                }
            }
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
