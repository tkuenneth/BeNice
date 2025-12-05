package de.thomaskuenneth.benice

import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class BeNiceScreenUiState(
    val isLoading: Boolean = false,
    val installedApps: List<AppInfo> = emptyList(),
    val letterPosition: Int = 1,
    val twoColumnsOnSmallScreens: Boolean = false,
    val threeColumnsOnMediumScreens: Boolean = false,
    val twoColumnsOnLargeScreens: Boolean = false,
    val appVersionString: String = "",
    val dynamicShortcuts: List<ShortcutInfo> = emptyList()
)

class BeNiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BeNiceScreenUiState())
    val uiState: StateFlow<BeNiceScreenUiState> = _uiState.asStateFlow()

    fun queryInstalledApps(packageManager: PackageManager) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { currentState ->
                currentState.copy(installedApps = installedApps(
                    packageManager = packageManager
                ).sortedBy { it.label.lowercase() })
            }
            setLoading(false)
        }
    }

    fun setDynamicShortcuts(shortcuts: List<ShortcutInfo>) {
        _uiState.update { currentState ->
            currentState.copy(dynamicShortcuts = shortcuts)
        }
    }

    fun setLetterPosition(position: Int) {
        _uiState.update { currentState -> currentState.copy(letterPosition = position) }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = isLoading
            )
        }
    }

    fun setAppVersionString(appVersionString: String) {
        _uiState.update { currentState ->
            currentState.copy(
                appVersionString = appVersionString
            )
        }
    }

    fun setTwoColumnsOnSmallScreens(twoColumnsOnSmallScreens: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                twoColumnsOnSmallScreens = twoColumnsOnSmallScreens
            )
        }
    }

    fun setThreeColumnsOnMediumScreens(threeColumnsOnMediumScreens: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                threeColumnsOnMediumScreens = threeColumnsOnMediumScreens
            )
        }
    }

    fun setTwoColumnsOnLargeScreens(twoColumnsOnLargeScreens: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                twoColumnsOnLargeScreens = twoColumnsOnLargeScreens
            )
        }
    }
}
