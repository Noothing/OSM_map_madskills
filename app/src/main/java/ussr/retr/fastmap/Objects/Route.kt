package ussr.retr.fastmap.Objects

class Route(
    val code: String?,
    val routes: List<Routes>?,
    val waypoints: List<Waypoint>?
) {
    class Routes(
        val geometry: Geometry,
        val legs: List<Leg>,
        val distance: Double,
        val duration: Double,
        val weight_name: String,
        val weight: Double
    ) {
        class Geometry(
            val coordinates: List<List<Double>>,
            val type: String
        )
        class Leg(
            val steps: List<String>?,
            val distance: Double,
            val duration: Double,
            val summary: String,
            val weight: Double
        )
    }
    class Waypoint(
        val hint: String,
        val distance: Double,
        val name: String,
        val location: List<Double>
    )
}

