package uz.yuk24.app.presentation.customer.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.components.PriceBreakdownCard
import uz.yuk24.app.presentation.common.components.PriceBreakdownData
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.components.StepProgressHeader
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.RedAccent
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.util.LoadSizeLabels

@Composable
fun Step4PriceScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val priceState by viewModel.priceState.collectAsState()
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        if (priceState !is PriceUiState.Loaded && priceState !is PriceUiState.Loading) {
            viewModel.calculatePrice()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        StepProgressHeader(
            step = 4,
            totalSteps = 5,
            titleRes = R.string.step_price_title,
            onBack = onBack
        )
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            // Route summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    RoutePointRow(emoji = "🔴", text = state.pickup?.label.orEmpty())
                    Spacer(Modifier.height(6.dp))
                    RoutePointRow(emoji = "🟢", text = state.delivery?.label.orEmpty())

                    val pState = priceState
                    if (pState is PriceUiState.Loaded) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(
                                R.string.km_min_format,
                                "%.2f".format(pState.distanceKm),
                                "%.0f".format(pState.durationMin)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when (val pState = priceState) {
                PriceUiState.Idle, PriceUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.calculating_price),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                is PriceUiState.Loaded -> {
                    val loadSize = state.loadSize ?: return@Column
                    PriceBreakdownCard(
                        data = PriceBreakdownData.fromQuote(
                            totalPrice = pState.price,
                            distanceKm = pState.distanceKm,
                            durationMin = pState.durationMin,
                            loadSize = loadSize,
                            loadLabel = LoadSizeLabels.label(loadSize),
                            unloading = state.unloading
                        )
                    )
                }
                is PriceUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, RedAccent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = pState.message,
                                color = RedAccent,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.calculatePrice() }) {
                                Text(
                                    text = stringResource(R.string.retry),
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        PrimaryCtaButton(
            text = stringResource(R.string.continue_action),
            onClick = onContinue,
            enabled = priceState is PriceUiState.Loaded
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RoutePointRow(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji)
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
