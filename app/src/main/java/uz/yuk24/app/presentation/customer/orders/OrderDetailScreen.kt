package uz.yuk24.app.presentation.customer.orders

import androidx.compose.runtime.Composable
import uz.yuk24.app.presentation.customer.tracking.OrderTrackingScreen

@Composable
fun OrderDetailScreen(
    orderId: String,
    phone: String,
    onBack: () -> Unit
) {
    OrderTrackingScreen(
        orderId = orderId,
        phone = phone,
        poll = false,
        onBack = onBack
    )
}
