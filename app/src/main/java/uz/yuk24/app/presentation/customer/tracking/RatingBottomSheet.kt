package uz.yuk24.app.presentation.customer.tracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.components.PrimaryCtaButton
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.RedAccent
import uz.yuk24.app.presentation.common.theme.SurfaceLight
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit,
    submitState: ReviewSubmitState
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.rate),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val filled = i <= rating
                    Icon(
                        imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (filled) androidx.compose.ui.graphics.Color(0xFFEAB308) else TextSecondary,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { rating = i }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.rate_comment_hint), color = TextSecondary) },
                minLines = 3,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = Primary,
                    unfocusedContainerColor = SurfaceLight,
                    focusedContainerColor = SurfaceLight
                )
            )

            if (submitState is ReviewSubmitState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(text = submitState.message, color = RedAccent, style = MaterialTheme.typography.bodySmall)
            }
            if (submitState is ReviewSubmitState.Submitted) {
                Spacer(Modifier.height(8.dp))
                Text(text = stringResource(R.string.rate_thanks), color = Primary, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))
            PrimaryCtaButton(
                text = stringResource(R.string.rate_submit),
                onClick = { onSubmit(rating, comment.takeIf { it.isNotBlank() }) },
                loading = submitState is ReviewSubmitState.Submitting,
                enabled = submitState !is ReviewSubmitState.Submitting && submitState !is ReviewSubmitState.Submitted
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
