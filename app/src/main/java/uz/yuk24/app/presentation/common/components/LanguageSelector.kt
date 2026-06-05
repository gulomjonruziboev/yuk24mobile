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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.UnloadingNoBg
import uz.yuk24.app.presentation.common.theme.UnloadingNoBorder
import uz.yuk24.app.presentation.common.theme.UnloadingYesBg
import uz.yuk24.app.presentation.common.theme.UnloadingYesBorder
import uz.yuk24.app.util.AppLanguage

@Composable
fun LanguageSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        AppLanguage.UZ to stringResource(R.string.language_uz),
        AppLanguage.RU to stringResource(R.string.language_ru),
        AppLanguage.EN to stringResource(R.string.language_en)
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (code, label) ->
            val isSelected = selected == code
            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(shape)
                    .background(if (isSelected) UnloadingYesBg else SurfaceWhite)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) UnloadingYesBorder else UnloadingNoBorder
                        ),
                        shape = shape
                    )
                    .clickable { onSelect(code) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) UnloadingYesBorder else TextPrimary
                )
            }
        }
    }
}
