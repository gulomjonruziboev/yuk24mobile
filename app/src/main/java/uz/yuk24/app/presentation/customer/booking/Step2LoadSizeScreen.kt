package uz.yuk24.app.presentation.customer.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.yuk24.app.R
import uz.yuk24.app.domain.model.LoadSize
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.components.StepProgressHeader
import uz.yuk24.app.presentation.common.components.TruckSelectorCard
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.util.LoadSizeLabels

@Composable
fun Step2LoadSizeScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        StepProgressHeader(
            step = 2,
            totalSteps = 5,
            titleRes = R.string.step_load_title,
            onBack = onBack
        )
        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.load_weight),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LoadSize.entries) { size ->
                TruckSelectorCard(
                    loadSize = size,
                    label = LoadSizeLabels.label(size),
                    minPriceText = LoadSizeLabels.minPriceText(size),
                    selected = state.loadSize == size,
                    onClick = { viewModel.setLoadSize(size) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.min_price),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = LoadSizeLabels.minPriceText(state.loadSize),
                style = MaterialTheme.typography.bodyMedium,
                color = Primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.weight(1f))

        PrimaryCtaButton(
            text = stringResource(R.string.continue_action),
            onClick = onContinue,
            enabled = state.loadSize != null
        )
        Spacer(Modifier.height(12.dp))
    }
}
