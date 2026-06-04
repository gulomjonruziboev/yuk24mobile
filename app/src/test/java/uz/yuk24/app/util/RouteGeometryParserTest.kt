package uz.yuk24.app.util

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGeometryParserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parseLineString() {
        val element = json.parseToJsonElement(
            """{"type":"LineString","coordinates":[[69.24,41.29],[69.28,41.31]]}"""
        )
        val points = RouteGeometryParser.parse(element)
        assertEquals(2, points.size)
        assertEquals(41.29, points[0].lat, 0.0001)
        assertEquals(69.24, points[0].lng, 0.0001)
        assertEquals(41.31, points[1].lat, 0.0001)
        assertEquals(69.28, points[1].lng, 0.0001)
    }

    @Test
    fun parseFeature() {
        val element = json.parseToJsonElement(
            """
            {
              "type": "Feature",
              "properties": {},
              "geometry": {
                "type": "LineString",
                "coordinates": [[69.1, 41.2], [69.2, 41.3], [69.3, 41.4]]
              }
            }
            """.trimIndent()
        )
        val points = RouteGeometryParser.parse(element)
        assertEquals(3, points.size)
        assertEquals(41.3, points[1].lat, 0.0001)
        assertEquals(69.2, points[1].lng, 0.0001)
    }

    @Test
    fun parseFeatureCollection_orsShape() {
        val element = json.parseToJsonElement(
            """
            {
              "type": "FeatureCollection",
              "features": [{
                "type": "Feature",
                "properties": {"segments": []},
                "geometry": {
                  "type": "LineString",
                  "coordinates": [[69.239, 41.299], [69.250, 41.305], [69.279, 41.311]]
                }
              }]
            }
            """.trimIndent()
        )
        val points = RouteGeometryParser.parse(element)
        assertEquals(3, points.size)
        assertEquals(41.299, points[0].lat, 0.001)
        assertEquals(69.239, points[0].lng, 0.001)
    }

    @Test
    fun parseNull_returnsEmpty() {
        assertTrue(RouteGeometryParser.parse(null).isEmpty())
    }

    @Test
    fun parseUnknownType_returnsEmpty() {
        val element = json.parseToJsonElement("""{"type":"Point","coordinates":[69,41]}""")
        assertTrue(RouteGeometryParser.parse(element).isEmpty())
    }
}
