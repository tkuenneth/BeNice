package de.thomaskuenneth.benice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BoxScope.AnimatedUpOrDownButton(
    isUpButton: Boolean,
    shouldBeVisible: Boolean,
    scrollState: ScrollState,
    coroutineScope: CoroutineScope
) {
    var visible by remember(shouldBeVisible) { mutableStateOf(shouldBeVisible) }
    AnimatedVisibility(
        modifier = Modifier.align(alignment = Alignment.BottomCenter),
        visible = visible
    ) {
        ElevatedButton(
            onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(if (isUpButton) 0 else scrollState.maxValue)
                }
            }) {
            Icon(
                imageVector = if (isUpButton) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = stringResource(
                    id = if (isUpButton) R.string.scroll_to_start else R.string.scroll_to_end
                )
            )
        }
    }
    LaunchedEffect(key1 = shouldBeVisible) {
        if (shouldBeVisible) {
            coroutineScope.launch {
                delay(3000L)
                visible = false
            }
        }
    }
}
