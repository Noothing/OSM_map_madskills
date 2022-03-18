package ussr.retr.fastmap.Networks.Search

import retrofit2.Call
import retrofit2.http.*
import ussr.retr.fastmap.Objects.Place

interface SearchAPIClient {

    @GET("search")
    fun searchByName(
        @Query("q") query: String
    ): Call<List<Place>>

    @GET("/reverse")
    fun searchByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
    ) : Call<Place>
}