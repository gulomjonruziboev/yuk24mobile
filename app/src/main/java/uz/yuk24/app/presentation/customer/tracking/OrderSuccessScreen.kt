package uz.yuk24.app.presentation.customer.tracking

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.components.OutlinedCtaButton
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.SelectedTruckFill
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onTrack: () -> Unit,
    onHome: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check-scale"
    )
    LaunchedEffect(Unit) { startAnim = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_check_circle),
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .scale(scale)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.order_success),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SelectedTruckFill)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.order_number, orderId),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                ),
                color = Primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.finding_driver),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(40.dp))
        PrimaryCtaButton(
            text = stringResource(R.string.track_order),
            onClick = onTrack,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedCtaButton(
            text = stringResource(R.string.go_home),
            onClick = onHome,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
