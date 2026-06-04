package uz.yuk24.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import uz.yuk24.app.R
import uz.yuk24.app.presentation.common.theme.Primary
import uz.yuk24.app.presentation.common.theme.SurfaceWhite
import uz.yuk24.app.presentation.common.theme.TextSecondary
import uz.yuk24.app.presentation.customer.booking.BookingViewModel
import uz.yuk24.app.presentation.customer.booking.Step1MapScreen
import uz.yuk24.app.presentation.customer.booking.Step2LoadSizeScreen
import uz.yuk24.app.presentation.customer.booking.Step3UnloadingScreen
import uz.yuk24.app.presentation.customer.booking.Step4PriceScreen
import uz.yuk24.app.presentation.customer.booking.Step5PaymentScreen
import uz.yuk24.app.presentation.customer.orders.MyOrdersScreen
import uz.yuk24.app.presentation.customer.orders.OrderDetailScreen
import uz.yuk24.app.presentation.customer.profile.ProfileScreen
import uz.yuk24.app.presentation.customer.tracking.OrderSuccessScreen
import uz.yuk24.app.presentation.customer.tracking.OrderTrackingScreen
import uz.yuk24.app.presentation.splash.SplashScreen

@Composable
fun Yuk24App() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            if (shouldShowBottomNav(currentRoute)) {
                Yuk24BottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
        containerColor = SurfaceWhite
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destinations.SPLASH) {
                SplashScreen(onReady = { _ ->
                    navController.navigate(Destinations.BOOKING_GRAPH) {
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                    }
                })
            }

            navigation(
                startDestination = Destinations.STEP1_MAP,
                route = Destinations.BOOKING_GRAPH
            ) {
                composable(Destinations.STEP1_MAP) { entry ->
                    val bookingVm = entry.sharedBookingViewModel(navController)
                    Step1MapScreen(
                        viewModel = bookingVm,
                        onContinue = { navController.navigate(Destinations.STEP2_LOAD) }
                    )
                }
                composable(Destinations.STEP2_LOAD) { entry ->
                    val bookingVm = entry.sharedBookingViewModel(navController)
                    Step2LoadSizeScreen(
                        viewModel = bookingVm,
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.navigate(Destinations.STEP3_UNLOADING) }
                    )
                }
                composable(Destinations.STEP3_UNLOADING) { entry ->
                    val bookingVm = entry.sharedBookingViewModel(navController)
                    Step3UnloadingScreen(
                        viewModel = bookingVm,
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.navigate(Destinations.STEP4_PRICE) }
                    )
                }
                composable(Destinations.STEP4_PRICE) { entry ->
                    val bookingVm = entry.sharedBookingViewModel(navController)
                    Step4PriceScreen(
                        viewModel = bookingVm,
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.navigate(Destinations.STEP5_PAYMENT) }
                    )
                }
                composable(Destinations.STEP5_PAYMENT) { entry ->
                    val bookingVm = entry.sharedBookingViewModel(navController)
                    Step5PaymentScreen(
                        viewModel = bookingVm,
                        onBack = { navController.popBackStack() },
                        onOrderPlaced = { orderIdLabel, internalId, phone ->
                            navController.navigate(Destinations.orderSuccess(orderIdLabel, internalId, phone)) {
                                popUpTo(Destinations.BOOKING_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable(Destinations.ORDER_SUCCESS) { entry ->
                val orderIdLabel = entry.arguments?.getString("orderIdLabel").orEmpty()
                val internalId = entry.arguments?.getString("internalId").orEmpty()
                val phoneArg = entry.arguments?.getString("phone").orEmpty()
                val decodedPhone = runCatching { java.net.URLDecoder.decode(phoneArg, "UTF-8") }.getOrDefault("")
                OrderSuccessScreen(
                    orderId = orderIdLabel,
                    onTrack = {
                        navController.navigate(
                            Destinations.orderTracking(internalId.ifBlank { orderIdLabel }, decodedPhone)
                        )
                    },
                    onHome = {
                        navController.navigate(Destinations.BOOKING_GRAPH) {
                            popUpTo(0) { inclusive = false }
                        }
                    }
                )
            }
            composable(Destinations.ORDER_TRACKING) { entry ->
                val orderIdArg = entry.arguments?.getString("orderId").orEmpty()
                val phoneArg = entry.arguments?.getString("phone").orEmpty()
                val decodedPhone = runCatching { java.net.URLDecoder.decode(phoneArg, "UTF-8") }.getOrDefault("")
                OrderTrackingScreen(
                    orderId = orderIdArg,
                    phone = decodedPhone,
                    poll = true,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Destinations.MY_ORDERS) {
                MyOrdersScreen(
                    onOrderClick = { id, phone ->
                        navController.navigate(Destinations.orderDetail(id, phone))
                    }
                )
            }
            composable(Destinations.ORDER_DETAIL) { entry ->
                val orderIdArg = entry.arguments?.getString("orderId").orEmpty()
                val phoneArg = entry.arguments?.getString("phone").orEmpty()
                val decodedPhone = runCatching { java.net.URLDecoder.decode(phoneArg, "UTF-8") }.getOrDefault("")
                OrderDetailScreen(
                    orderId = orderIdArg,
                    phone = decodedPhone,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.PROFILE) { ProfileScreen() }
        }
    }
}

@Composable
private fun NavBackStackEntry.sharedBookingViewModel(
    navController: NavHostController
): BookingViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(Destinations.BOOKING_GRAPH)
    }
    return hiltViewModel(parentEntry)
}

private fun shouldShowBottomNav(route: String?): Boolean {
    if (route == null) return false
    return route == Destinations.STEP1_MAP ||
            route == Destinations.MY_ORDERS ||
            route == Destinations.PROFILE
}

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Destinations.STEP1_MAP, R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Destinations.MY_ORDERS, R.string.nav_orders, Icons.Filled.Receipt),
    BottomNavItem(Destinations.PROFILE, R.string.nav_profile, Icons.Filled.Person)
)

@Composable
private fun Yuk24BottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        contentColor = TextSecondary,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute == item.route) return@NavigationBarItem
                    val target = if (item.route == Destinations.STEP1_MAP) {
                        Destinations.BOOKING_GRAPH
                    } else item.route
                    navController.navigate(target) {
                        popUpTo(Destinations.BOOKING_GRAPH) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = SurfaceWhite
                )
            )
        }
    }
}
