package uz.yuk24.app.presentation.navigation

object Destinations {
    const val SPLASH = "splash"

    const val BOOKING_GRAPH = "booking_graph"
    const val STEP1_MAP = "booking/step1_map"

    const val STEP2_LOAD = "booking/step2_load"
    const val STEP3_UNLOADING = "booking/step3_unloading"
    const val STEP4_PRICE = "booking/step4_price"
    const val STEP5_PAYMENT = "booking/step5_payment"

    const val ORDER_SUCCESS = "order_success/{orderIdLabel}/{internalId}/{phone}"
    fun orderSuccess(orderIdLabel: String, internalId: String, phone: String): String =
        "order_success/$orderIdLabel/$internalId/${java.net.URLEncoder.encode(phone, "UTF-8")}"

    const val ORDER_TRACKING = "order_tracking/{orderId}/{phone}"
    fun orderTracking(orderId: String, phone: String): String =
        "order_tracking/$orderId/${java.net.URLEncoder.encode(phone, "UTF-8")}"

    const val MY_ORDERS = "my_orders"

    const val ORDER_DETAIL = "order_detail/{orderId}/{phone}"
    fun orderDetail(orderId: String, phone: String): String =
        "order_detail/$orderId/${java.net.URLEncoder.encode(phone, "UTF-8")}"

    const val PROFILE = "profile"
}
