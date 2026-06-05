package uz.yuk24.app.presentation.customer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.yuk24.app.BuildConfig
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.components.LanguageSelector
import uz.yuk24.app.presentation.common.components.YukLogo
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.util.PhoneUtils

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.profile_title)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceWhite,
                titleContentColor = TextPrimary
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                YukLogo(fontSizeSp = 36)
            }
            Text(
                text = stringResource(R.string.profile_about),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.profile_phone),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.phone.isBlank()) "—"
                        else PhoneUtils.displayPretty(PhoneUtils.stripLocal(state.phone)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.profile_language),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    LanguageSelector(
                        selected = state.language,
                        onSelect = viewModel::setLanguage
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.profile_version)} ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
