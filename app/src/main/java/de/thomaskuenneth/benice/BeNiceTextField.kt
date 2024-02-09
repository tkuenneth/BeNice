package de.thomaskuenneth.benice

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun BeNiceTextField(
    value: String,
    @StringRes resId: Int,
    onValueChange: (String) -> Unit,
    message: String = "",
    keyboardType: KeyboardType = KeyboardType.Number
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        label = {
            Text(text = stringResource(id = resId))
        },
        isError = message.isNotEmpty(),
        supportingText = { Text(text = message) },
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
