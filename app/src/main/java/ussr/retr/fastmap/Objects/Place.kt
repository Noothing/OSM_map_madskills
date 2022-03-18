package ussr.retr.fastmap.Objects

class Place(
    val place_id: Int?,
    val licence: String?,
    val osm_type: String?,
    val osm_id: Int?,
    val lat: Double?,
    val lon: Double?,
    val display_name: String?,
    val category: String?,
    val type: String?,
    val importance: Double?,
    val place_rank: Int?,
    val address: Address?,
    val extratags: String?,
    val boundingbox: List<Double>?,
    val geojson: GeoJson?,
) {
    class Address(
        val amenity: String?,
        val house_number: String?,
        val road: String?,
        val suburb: String?,
        val city: String?,
        val county: String?,
        val state: String?,
        val region: String?,
        val postcode: String?,
        val country: String?,
        val country_code: String?
    )

    class GeoJson(
        val type: String,
        val coordinates: List<List<Double>>
    )

}