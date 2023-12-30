package de.thomaskuenneth.benice

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButtonWithTooltip(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String
) {
    PlainTooltipBox(tooltip = {
        Text(text = contentDescription)
    }) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.tooltipAnchor()
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    }
}
