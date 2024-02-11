package de.thomaskuenneth.benice

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class BeNiceScreenUiState(
    val isLoading: Boolean = false, val installedApps: List<AppInfo> = emptyList()
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
                ).sortedBy { it.label })
            }
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
}
