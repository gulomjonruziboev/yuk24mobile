package uz.yuk24.app.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import uz.yuk24.app.domain.model.OrderStatus
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.GreenAccent
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.RedAccent
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary

private data class StepDef(val status: OrderStatus, val labelRes: Int)

private val orderSteps = listOf(
    StepDef(OrderStatus.QUEUE, R.string.status_queue),
    StepDef(OrderStatus.PROCESS, R.string.status_process),
    StepDef(OrderStatus.PICKED_UP, R.string.status_picked_up),
    StepDef(OrderStatus.DELIVERED, R.string.status_delivered)
)

@Composable
fun OrderStatusTimeline(
    status: OrderStatus,
    cancelReason: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (status == OrderStatus.CANCELLED) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(RedAccent.copy(alpha = 0.12f))
                    .border(1.dp, RedAccent, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.status_cancelled),
                    color = RedAccent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!cancelReason.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = cancelReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            return@Column
        }

        val currentIndex = orderSteps.indexOfFirst { it.status == status }
            .let { if (it == -1) 0 else it }

        orderSteps.forEachIndexed { i, step ->
            val reached = i <= currentIndex
            val isLast = i == orderSteps.lastIndex
            val isDelivered = step.status == OrderStatus.DELIVERED && reached

            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDelivered -> GreenAccent
                                    reached -> Primary
                                    else -> SurfaceWhite
                                }
                            )
                            .border(
                                1.5.dp,
                                if (reached) {
                                    if (isDelivered) GreenAccent else Primary
                                } else BorderColor,
                                CircleShape
                            )
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(34.dp)
                                .background(if (i < currentIndex) Primary else BorderColor)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(top = 0.dp)) {
                    Text(
                        text = stringResource(id = step.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reached) TextPrimary else TextSecondary,
                        fontWeight = if (i == currentIndex) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(Modifier.height(if (isLast) 0.dp else 18.dp))
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (color, labelRes) = when (status) {
        OrderStatus.QUEUE -> TextSecondary to R.string.status_queue
        OrderStatus.PROCESS -> Primary to R.string.status_process
        OrderStatus.PICKED_UP -> Primary to R.string.status_picked_up
        OrderStatus.DELIVERED -> GreenAccent to R.string.status_delivered
        OrderStatus.CANCELLED -> RedAccent to R.string.status_cancelled
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(id = labelRes),
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

