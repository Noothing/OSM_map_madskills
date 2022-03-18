package ussr.retr.fastmap.Networks.Search

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class SearchAPI {
    companion object {
        const val URL = " https://nominatim.openstreetmap.org/"
    }

    private val retrofit = Retrofit
        .Builder()
        .baseUrl(URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun init() : SearchAPIClient{
        return retrofit.create(SearchAPIClient::class.java)
    }
}