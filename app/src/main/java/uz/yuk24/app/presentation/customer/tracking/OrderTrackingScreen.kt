package uz.yuk24.app.presentation.customer.tracking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.yuk24.app.R
import uz.yuk24.app.domain.model.Order
import uz.yuk24.app.domain.model.OrderStatus
import uz.yuk24.app.presentation.common.components.OrderStatusTimeline
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.RedAccent
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.util.PricingUtils

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    phone: String,
    poll: Boolean = true,
    onBack: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val reviewState by viewModel.review.collectAsState()
    var showRating by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(orderId, phone, poll) {
        viewModel.start(orderId, phone.ifBlank { null }, poll)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.track_order)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = SurfaceWhite
    ) { padding ->
        when (val s = state) {
            TrackingUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is TrackingUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = s.message,
                        color = RedAccent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is TrackingUiState.Success -> {
                val scroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scroll)
                        .padding(16.dp)
                ) {
                    OrderRouteCard(order = s.order, onCallDriver = { number ->
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                            )
                        }
                    })
                    Spacer(Modifier.height(12.dp))
                    StatusCard(order = s.order)
                    Spacer(Modifier.height(12.dp))

                    if (s.order.status == OrderStatus.DELIVERED && s.order.rating == null) {
                        PrimaryCtaButton(
                            text = stringResource(R.string.rate),
                            onClick = { showRating = true }
                        )
                    }
                }
            }
        }
    }

    if (showRating) {
        val order = (state as? TrackingUiState.Success)?.order
        if (order != null) {
            RatingBottomSheet(
                onDismiss = { showRating = false },
                onSubmit = { rating, comment ->
                    viewModel.submitOrderReview(order.id, rating, comment)
                },
                submitState = reviewState
            )
            LaunchedEffect(reviewState) {
                if (reviewState is ReviewSubmitState.Submitted) {
                    showRating = false
                }
            }
        }
    }
}

@Composable
private fun OrderRouteCard(
    order: Order,
    onCallDriver: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = order.orderId,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "🔴 ${order.pickup.label}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = "🟢 ${order.delivery.label}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.km_min_format,
                        "%.2f".format(order.distanceKm),
                        "%.0f".format(order.durationMin)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = PricingUtils.format(order.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!order.driverName.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = order.driverName, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        order.driverPhone?.let {
                            Text(text = it, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    order.driverPhone?.let { phone ->
                        IconButton(onClick = { onCallDriver(phone) }) {
                            Icon(Icons.Filled.Call, contentDescription = stringResource(R.string.call_driver), tint = Primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.status_title),
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(12.dp))
            OrderStatusTimeline(
                status = order.status,
                cancelReason = order.cancelReason
            )
        }
    }
}
