package de.thomaskuenneth.benice

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun BeNiceLabel(@StringRes text: Int, modifier: Modifier = Modifier) {
    BeNiceLabel(text = stringResource(id = text), modifier = modifier)
}

@Composable
fun BeNiceLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text, style = MaterialTheme.typography.labelMedium, modifier = modifier
    )
}
