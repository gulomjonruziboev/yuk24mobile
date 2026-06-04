package uz.yuk24.app.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.yuk24.app.R
import uz.yuk24.app.domain.model.LoadSize
import uz.yuk24.app.presentation.common.theme.PriceCardBg
import uz.yuk24.app.presentation.common.theme.WhiteAlpha60
import uz.yuk24.app.presentation.common.theme.WhiteAlpha80
import uz.yuk24.app.util.PricingUtils

/**
 * Customer-facing price breakdown only (base, distance, load tier, unloading, total).
 * Does not show platform/driver revenue split.
 */
data class PriceBreakdownData(
    val totalPrice: Double,
    val basePrice: Double,
    val distanceKm: Double,
    val distanceFee: Double,
    val loadSize: LoadSize,
    val loadLabel: String,
    val coefficientSurcharge: Double,
    val unloading: Boolean,
    val unloadingFee: Double,
    val durationMin: Double
) {
    companion object {
        fun fromQuote(
            totalPrice: Double,
            distanceKm: Double,
            durationMin: Double,
            loadSize: LoadSize,
            loadLabel: String,
            unloading: Boolean
        ): PriceBreakdownData = PriceBreakdownData(
            totalPrice = totalPrice,
            basePrice = PricingUtils.BASE_PRICE,
            distanceKm = distanceKm,
            distanceFee = PricingUtils.distanceFee(distanceKm),
            loadSize = loadSize,
            loadLabel = loadLabel,
            coefficientSurcharge = PricingUtils.coefficientSurcharge(distanceKm, loadSize),
            unloading = unloading,
            unloadingFee = PricingUtils.unloadingFee(unloading),
            durationMin = durationMin
        )
    }
}

@Composable
fun PriceBreakdownCard(
    data: PriceBreakdownData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PriceCardBg)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.estimated_price),
                style = MaterialTheme.typography.labelMedium,
                color = WhiteAlpha80
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = PricingUtils.format(data.totalPrice),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.km_min_format,
                    "%.2f".format(data.distanceKm),
                    "%.0f".format(data.durationMin)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = WhiteAlpha80
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.price_details),
                style = MaterialTheme.typography.labelMedium,
                color = WhiteAlpha60
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = WhiteAlpha60.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            PriceRow(
                label = stringResource(R.string.base_price),
                value = PricingUtils.format(data.basePrice)
            )
            PriceRow(
                label = stringResource(
                    R.string.distance_with_rate,
                    "%.2f".format(data.distanceKm)
                ),
                value = PricingUtils.format(data.distanceFee)
            )
            PriceRow(
                label = "${stringResource(R.string.load_coefficient)}  ${data.loadLabel} " +
                    stringResource(
                        R.string.multiplier_format,
                        "%.1f".format(data.loadSize.multiplier)
                    ),
                value = PricingUtils.format(data.coefficientSurcharge)
            )
            if (data.unloading) {
                PriceRow(
                    label = stringResource(R.string.unloading_fee),
                    value = PricingUtils.format(data.unloadingFee)
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = WhiteAlpha60.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = PricingUtils.format(data.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WhiteAlpha80
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = WhiteAlpha80
        )
    }
}
