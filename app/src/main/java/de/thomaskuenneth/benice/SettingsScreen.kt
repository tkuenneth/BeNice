package de.thomaskuenneth.benice

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    removeDynamicShortcutsEnabled: Boolean,
    isOpen: Boolean,
    viewModel: BeNiceViewModel,
    sheetClosed: () -> Unit,
    removeAllDynamicShortcutsCallback: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()
    val options = listOf(
        stringResource(id = R.string.left),
        stringResource(id = R.string.center),
        stringResource(id = R.string.right)
    )
    val closeSheet: (() -> Unit) -> Unit = { callback ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            callback()
            sheetClosed()
        }
    }
    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = sheetClosed,
            sheetState = sheetState,
            windowInsets = WindowInsets(0),
        ) {
            BeNiceLabel(
                text = R.string.letter_position,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            viewModel.setLetterPosition(index)
                        },
                        selected = index == state.letterPosition
                    ) {
                        Text(label)
                    }
                }
            }
            Button(
                enabled = removeDynamicShortcutsEnabled,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp),
                onClick = {
                    closeSheet {
                        removeAllDynamicShortcutsCallback()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.remove_all_dynamic_shortcuts))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = state.appVersionString,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(
                modifier = Modifier
                    .padding(
                        WindowInsets.navigationBars
                            .union(WindowInsets(bottom = 16.dp))
                            .asPaddingValues()
                    )
            )
        }
    }
}
