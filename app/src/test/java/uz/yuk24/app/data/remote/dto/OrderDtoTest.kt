package uz.yuk24.app.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.yuk24.app.domain.model.Order

class OrderDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun parseOrder_driverWithoutName_usesUsername() {
        val element = json.parseToJsonElement(
            """
            {
              "_id": "507f1f77bcf86cd799439011",
              "orderId": "ORD-1001",
              "customerPhone": "+998976053110",
              "pickup": { "label": "Chorsu", "coords": [41.326, 69.228] },
              "delivery": { "label": "Airport", "coords": [41.258, 69.281] },
              "loadSize": "medium",
              "unloading": false,
              "price": 50000,
              "distanceKm": 5,
              "durationMin": 12,
              "status": "process",
              "driverId": { "username": "driver1" },
              "createdAt": "2026-06-05T12:00:00.000Z"
            }
            """.trimIndent()
        )
        val dto = json.decodeFromJsonElement(OrderDto.serializer(), element)
        val order = Order.fromDto(dto)
        assertEquals("driver1", order.driverName)
    }

    @Test
    fun parseOrder_driverWithName_prefersName() {
        val element = json.parseToJsonElement(
            """
            {
              "_id": "507f1f77bcf86cd799439012",
              "orderId": "ORD-1002",
              "customerPhone": "+998976053110",
              "pickup": { "label": "A", "coords": [41.3, 69.2] },
              "delivery": { "label": "B", "coords": [41.31, 69.28] },
              "loadSize": "small",
              "unloading": false,
              "price": 30000,
              "distanceKm": 3,
              "durationMin": 8,
              "status": "delivered",
              "driverId": { "username": "driver1", "name": "Ali Valiyev", "phone": "+998901112233" },
              "createdAt": "2026-06-05T12:00:00.000Z"
            }
            """.trimIndent()
        )
        val dto = json.decodeFromJsonElement(OrderDto.serializer(), element)
        val order = Order.fromDto(dto)
        assertEquals("Ali Valiyev", order.driverName)
        assertEquals("+998901112233", order.driverPhone)
    }

    @Test
    fun parseOrder_driverIdAsStringObjectId_parsesWithNullDriver() {
        val element = json.parseToJsonElement(
            """
            {
              "_id": "507f1f77bcf86cd799439013",
              "orderId": "ORD-1003",
              "customerPhone": "+998976053110",
              "pickup": { "label": "A", "coords": [41.3, 69.2] },
              "delivery": { "label": "B", "coords": [41.31, 69.28] },
              "loadSize": "small",
              "unloading": false,
              "price": 30000,
              "distanceKm": 3,
              "durationMin": 8,
              "status": "delivered",
              "driverId": "6a21a1ed92701d8eb91",
              "review": { "rating": 5, "comment": "haydovchi zor ekan" },
              "createdAt": "2026-06-05T12:00:00.000Z"
            }
            """.trimIndent()
        )
        val dto = json.decodeFromJsonElement(OrderDto.serializer(), element)
        val order = Order.fromDto(dto)
        assertEquals(null, order.driverName)
        assertEquals(5, order.rating)
        assertEquals("haydovchi zor ekan", order.reviewComment)
    }

    @Test
    fun parseOrder_reviewResponseWithPopulatedDriver() {
        val element = json.parseToJsonElement(
            """
            {
              "_id": "507f1f77bcf86cd799439014",
              "orderId": "ORD-1004",
              "customerPhone": "+998976053110",
              "pickup": { "label": "A", "coords": [41.3, 69.2] },
              "delivery": { "label": "B", "coords": [41.31, 69.28] },
              "loadSize": "medium",
              "unloading": false,
              "price": 40000,
              "distanceKm": 4,
              "durationMin": 10,
              "status": "delivered",
              "driverId": { "username": "driver2", "name": "Vali", "phone": "+998901112233" },
              "review": { "rating": 4, "comment": "Yaxshi" },
              "createdAt": "2026-06-05T12:00:00.000Z"
            }
            """.trimIndent()
        )
        val dto = json.decodeFromJsonElement(OrderDto.serializer(), element)
        val order = Order.fromDto(dto)
        assertEquals("Vali", order.driverName)
        assertEquals(4, order.rating)
    }
}
