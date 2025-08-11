package de.thomaskuenneth.benice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun ShortcutInfoScreen(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent), contentAlignment = Alignment.BottomStart

        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium)
                    .safeContentPadding()
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.did_you_know),
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            append(stringResource(R.string.app_name))
                        }
                        append(stringResource(R.string.shortcut_info_01))
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            append(stringResource(R.string.shortcut_info_02))
                        }
                        append(stringResource(R.string.shortcut_info_03))
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            append(stringResource(R.string.shortcut_info_04))
                        }
                        append(stringResource(R.string.shortcut_info_05))
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            append(stringResource(R.string.shortcut_info_06))
                        }
                        append(stringResource(R.string.shortcut_info_07))
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            append(stringResource(R.string.app_name))
                        }
                        append(stringResource(R.string.shortcut_info_08))
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(id = R.string.got_it))
                    }
                }
            }
        }
    }
}
