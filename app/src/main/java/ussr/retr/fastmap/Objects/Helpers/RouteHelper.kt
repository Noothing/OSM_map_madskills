package ussr.retr.fastmap.Objects.Helpers

import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ussr.retr.fastmap.Networks.RouteAPI
import ussr.retr.fastmap.Networks.Search.SearchAPI
import ussr.retr.fastmap.Objects.Place
import ussr.retr.fastmap.Objects.Route
import ussr.retr.fastmap.PlaceBottomSheet
import ussr.retr.fastmap.R

class RouteHelper(private val map: MapView, private val locationOverlay: MyLocationNewOverlay, private val supportFragmentManager: FragmentManager) {

    private var routeOverlay: Overlay
    private var routeMode = false
    private var route = Polyline(map)
    private var routeMarker = Marker(map)

    init {
        routeMarker.icon =
            AppCompatResources.getDrawable(map.context, R.drawable.ic_baseline_location_on_24)

        routeOverlay = object : Overlay() {
            override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                val proj = mapView!!.projection
                val loc = proj.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint

                if (map.overlays.contains(routeMarker)) {
                    map.overlays.remove(routeMarker)
                }

                routeMarker.position = loc
                routeMarker.infoWindow = null
                map.overlays.add(routeMarker)

                if (routeMode){
                    createRouteFromTo(locationOverlay.myLocation, loc)
                }else{
                    createBottomSheet(loc)
                }
                return super.onLongPress(e, mapView)
            }
        }
    }

    fun createBottomSheet(location: GeoPoint) {

        val searchAPI = SearchAPI().init()
        searchAPI.searchByLocation(lat = location.latitude, lon = location.longitude).enqueue(object: Callback<Place>{
            override fun onResponse(call: Call<Place>, response: Response<Place>) {
                if (response.isSuccessful){

                    val place = response.body() as Place
                    val modalSheet = PlaceBottomSheet(place, this@RouteHelper)
                    modalSheet.show(supportFragmentManager, PlaceBottomSheet.TAG)
                }
            }

            override fun onFailure(call: Call<Place>, t: Throwable) {
                Log.d("tag", Gson().toJson(t))
            }

        })
    }

    fun enableRouteCreating() {
        routeMode = true
    }
    
    fun disableRouteMode() {
        routeMode = false
    }

    fun routeHelperEnabled() : Boolean{
        return map.overlays.contains(routeOverlay)
    }

    fun createRouteOverlay() {
        map.overlays.add(routeOverlay)
    }

    fun destroyRouteOverlay() {
        if (map.overlays.contains(routeOverlay)){
            map.overlays.remove(routeOverlay)
        }
    }

    fun createRouteFromTo(startPoint: GeoPoint, endPoint: GeoPoint) {
        val startPointFormatted = "${startPoint.longitude},${startPoint.latitude}"
        val endPointFormatted = "${endPoint.longitude},${endPoint.latitude}"
        requestRoute(startPointFormatted, endPointFormatted)
    }

    private fun drawRoute(list: List<GeoPoint>) {
        map.overlays.remove(route)
        route.setPoints(list)
        route.infoWindow = null
        map.overlays.add(route)
    }

    private fun requestRoute(startPointFormatted: String, endPointFormatted: String) {
        val retrofit = RouteAPI().init()
        retrofit
            .getRouteDrive("drive", startPointFormatted, endPointFormatted)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.isSuccessful) {

                        val listGeoPoint: MutableList<GeoPoint> = mutableListOf()
                        val routeObject: Route = Gson().fromJson(response.body(), Route::class.java)
                        routeObject.routes!!.first().geometry.coordinates.forEach { coordinate ->
                            val geoPoint = GeoPoint(coordinate[1], coordinate[0])
                            listGeoPoint.add(geoPoint)
                        }
                        drawRoute(listGeoPoint)
                    } else {
                        Log.d("tag", "${call.request().url()}")
                        Log.d("tag", "Response code: ${Gson().toJson(response.code())}")
                        Log.d("tag", "Response body: ${Gson().toJson(response.body())}")
                        Log.d("tag", "Error body ${Gson().toJson(response.errorBody())}")
                        Log.d("tag", "Message ${Gson().toJson(response.message())}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("tag", "${call.request().url()}")
                    Log.d("tag", Gson().toJson(t))
                }

            })
    }

    fun createRouteTo(endPoint: GeoPoint) {
        val startPointFormatted = "${locationOverlay.myLocation.longitude},${locationOverlay.myLocation.latitude}"
        val endPointFormatted = "${endPoint.longitude},${endPoint.latitude}"
        requestRoute(startPointFormatted, endPointFormatted)
    }
}