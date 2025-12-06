package de.thomaskuenneth.benice

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun LearnMore(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(id = R.string.tkuenneth_dev_benice_shortcuts_url)
    val annotatedString = buildAnnotatedString {
        val str = stringResource(id = R.string.read_more_on, url)
        val startIndex = str.indexOf(url)
        val endIndex = startIndex + url.length
        append(str)
        addStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline
            ),
            start = startIndex,
            end = endIndex
        )
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = startIndex,
            end = endIndex
        )
    }
    Text(
        text = annotatedString,
        modifier = modifier.clickable {
            uriHandler.openUri(url)
        },
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            hyphens = Hyphens.Auto,
            lineBreak = LineBreak.Paragraph.copy(strictness = LineBreak.Strictness.Loose),
            textAlign = TextAlign.Center
        ),
    )
}
