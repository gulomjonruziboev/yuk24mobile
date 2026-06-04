package uz.yuk24.app.presentation.customer.orders

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.components.EmptyState
import uz.yuk24.app.presentation.common.components.OrderListItemCard
import uz.yuk24.app.presentation.common.components.ShimmerBox
import uz.yuk24.app.presentation.common.theme.BorderColor
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.RedAccent
import uz.yuk24.app.presentation.common.theme.SurfaceLight
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextPrimary
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.util.PhoneUtils

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onOrderClick: (orderId: String, phone: String) -> Unit,
    viewModel: MyOrdersViewModel = hiltViewModel()
) {
    val phone by viewModel.phone.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.my_orders)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceWhite,
                titleContentColor = TextPrimary
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = PhoneUtils.formatMask(phone),
                onValueChange = { viewModel.setPhone(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.phone_hint), color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = Primary,
                    unfocusedContainerColor = SurfaceLight,
                    focusedContainerColor = SurfaceLight
                )
            )
            Button(
                onClick = { viewModel.load() },
                enabled = PhoneUtils.isComplete(phone),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(stringResource(R.string.load_orders))
            }
        }

        when (val s = state) {
            MyOrdersUiState.Idle -> {
                EmptyState(
                    title = stringResource(R.string.enter_phone_to_load),
                    emoji = "📱"
                )
            }
            MyOrdersUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4) {
                        ShimmerBox(modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp))
                    }
                }
            }
            is MyOrdersUiState.Loaded -> {
                if (s.orders.isEmpty()) {
                    EmptyState(title = stringResource(R.string.empty_orders))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.orders, key = { it.id }) { order ->
                            OrderListItemCard(
                                order = order,
                                onClick = { onOrderClick(order.id, order.customerPhone) }
                            )
                        }
                    }
                }
            }
            is MyOrdersUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = s.message,
                        color = RedAccent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
