package de.thomaskuenneth.benice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun OpenInBrowserMenuItem(onDone: (String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }
    val isTextValid by remember { derivedStateOf { url.isValidUrl() } }
    val focusRequester = remember { FocusRequester() }
    val done = {
        isEditing = false
        onDone(url)
    }
    Column {
        MenuItem(
            enabled = !isEditing,
            onClick = {
                url = ""
                isEditing = true
            },
            imageVector = Icons.Default.OpenInBrowser,
            textRes = R.string.open_url
        )
        AnimatedVisibility(visible = isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    singleLine = true,
                    placeholder = { Text(text = stringResource(id = R.string.hint_https)) },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    onValueChange = {
                        url = it
                    },
                    keyboardActions = KeyboardActions(onAny = {
                        if (isTextValid) done()
                    }),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { isEditing = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(
                        enabled = isTextValid,
                        onClick = {
                            done()
                        }
                    ) {
                        Text(text = stringResource(id = R.string.done))
                    }
                }
            }
        }
        LaunchedEffect(key1 = isEditing) {
            if (isEditing) focusRequester.requestFocus()
        }
    }
}
