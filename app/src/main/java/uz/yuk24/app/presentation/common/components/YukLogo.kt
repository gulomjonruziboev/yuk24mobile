package uz.yuk24.app.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.TextPrimary

@Composable
fun YukLogo(
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 36
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "YUK",
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSizeSp.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "24",
            color = Primary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSizeSp.sp,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
        )
    }
}
