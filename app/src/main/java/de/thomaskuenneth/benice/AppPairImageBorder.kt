package de.thomaskuenneth.benice

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.appPairImageBorder(selected: Boolean): Modifier = then(
    if (selected) Modifier.border(
        width = 1.dp, color = MaterialTheme.colorScheme.primary
    ) else Modifier
)
