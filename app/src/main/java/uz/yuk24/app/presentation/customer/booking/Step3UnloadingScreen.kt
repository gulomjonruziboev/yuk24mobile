package uz.yuk24.app.presentation.customer.booking

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.components.StepProgressHeader
import uz.yuk24.app.presentation.common.components.ToggleChoicePair
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.SurfaceLight
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.util.PhoneUtils

@Composable
fun Step3UnloadingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        StepProgressHeader(
            step = 3,
            totalSteps = 5,
            titleRes = R.string.step_info_title,
            onBack = onBack
        )
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Unloading section
            Text(
                text = stringResource(R.string.unloading_question),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            ToggleChoicePair(
                leftText = "✕ ${stringResource(R.string.unloading_no)}",
                rightText = "🖐 ${stringResource(R.string.unloading_yes)}",
                rightSelected = state.unloading,
                onLeftClick = { viewModel.setUnloading(false) },
                onRightClick = { viewModel.setUnloading(true) }
            )

            Spacer(Modifier.height(24.dp))

            // Phone section
            Text(
                text = stringResource(R.string.phone_label),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceLight)
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.country_code_uz),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
                Spacer(Modifier.padding(horizontal = 4.dp))
                OutlinedTextField(
                    value = state.customerPhone,
                    onValueChange = { input ->
                        viewModel.setPhone(input.filter { it.isDigit() }.take(9))
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.phone_hint), color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneMaskTransformation,
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = SurfaceLight,
                        focusedContainerColor = SurfaceLight
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            // Name section
            Text(
                text = stringResource(R.string.name_label),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.customerName,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.name_optional), color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = Primary,
                    unfocusedContainerColor = SurfaceLight,
                    focusedContainerColor = SurfaceLight
                )
            )
        }

        Spacer(Modifier.height(12.dp))
        PrimaryCtaButton(
            text = stringResource(R.string.continue_action),
            onClick = onContinue,
            enabled = PhoneUtils.isComplete(state.customerPhone)
        )
        Spacer(Modifier.height(12.dp))
    }
}

/**
 * Keeps the underlying value as raw 9-digit local body and renders the
 * "XX XXX XX XX" mask via visual transformation. This avoids the cursor-jump
 * and broken-backspace behavior of reformatting the value on every change.
 */
private object PhoneMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val masked = PhoneUtils.formatMask(text.text)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var spaces = 0
                if (offset > 2) spaces++
                if (offset > 5) spaces++
                if (offset > 7) spaces++
                return (offset + spaces).coerceAtMost(masked.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var spaces = 0
                if (offset > 2) spaces++
                if (offset > 6) spaces++
                if (offset > 9) spaces++
                return (offset - spaces).coerceIn(0, text.text.length)
            }
        }
        return TransformedText(AnnotatedString(masked), mapping)
    }
}
