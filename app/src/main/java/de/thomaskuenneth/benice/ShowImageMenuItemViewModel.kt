package de.thomaskuenneth.benice

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowImageMenuItemViewModel : ViewModel() {

    private val channel = Channel<Bitmap?>()

    private val _uri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val uri: StateFlow<Uri?> = _uri.asStateFlow()

    fun awaitBitmap(callback: (Bitmap?) -> Unit) {
        viewModelScope.launch {
            callback(channel.receive())
        }
    }

    fun setBitmap(bitmap: Bitmap?) {
        channel.trySend(bitmap)
    }

    fun setUri(uri: Uri?) {
        _uri.update { uri }
    }
}
