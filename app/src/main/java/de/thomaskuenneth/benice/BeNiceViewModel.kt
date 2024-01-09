package de.thomaskuenneth.benice

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class BeNiceScreenUiState(
    val filterOn: Boolean = false,
    val alwaysOpenAdjacent: Boolean = false,
    val isLoading: Boolean = false,
    val installedApps: List<AppInfo> = emptyList()
)

class BeNiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BeNiceScreenUiState())
    val uiState: StateFlow<BeNiceScreenUiState> = _uiState.asStateFlow()

    private val installedAppsFlow: MutableStateFlow<List<AppInfo>> = MutableStateFlow(emptyList())

    fun setFilterOn(filterOn: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                filterOn = filterOn
            )
        }
        updateInstalledApps()
    }

    fun setAlwaysOpenAdjacent(alwaysOpenAdjacent: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                alwaysOpenAdjacent = alwaysOpenAdjacent
            )
        }
    }

    fun queryInstalledApps(packageManager: PackageManager) {
        setLoading(true)
        viewModelScope.launch {
            installedAppsFlow.value = installedApps(packageManager)
            updateInstalledApps()
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = isLoading
            )
        }
    }

    private fun updateInstalledApps() {
        _uiState.update { currentState ->
            currentState.copy(
                installedApps = installedAppsFlow.value.filter { appInfo -> !currentState.filterOn || appInfo.screenOrientationPortrait }
                    .sortedBy { it.label }
            )
        }
    }
}
