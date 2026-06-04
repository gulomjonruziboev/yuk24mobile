package uz.yuk24.app.presentation.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.UnloadingNoBg
import uz.yuk24.app.presentation.common.theme.UnloadingNoBorder
import uz.yuk24.app.presentation.common.theme.UnloadingYesBg
import uz.yuk24.app.presentation.common.theme.UnloadingYesBorder

@Composable
fun ToggleChoicePair(
    leftText: String,
    rightText: String,
    rightSelected: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleItem(
            text = leftText,
            selected = !rightSelected,
            selectedFill = UnloadingNoBg,
            selectedBorder = UnloadingNoBorder,
            modifier = Modifier.weight(1f),
            onClick = onLeftClick
        )
        ToggleItem(
            text = rightText,
            selected = rightSelected,
            selectedFill = UnloadingYesBg,
            selectedBorder = UnloadingYesBorder,
            modifier = Modifier.weight(1f),
            onClick = onRightClick
        )
    }
}

@Composable
private fun ToggleItem(
    text: String,
    selected: Boolean,
    selectedFill: androidx.compose.ui.graphics.Color,
    selectedBorder: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(if (selected) selectedFill else SurfaceWhite)
            .border(
                BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) selectedBorder else UnloadingNoBorder
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) selectedBorder else TextPrimary
        )
    }
}
