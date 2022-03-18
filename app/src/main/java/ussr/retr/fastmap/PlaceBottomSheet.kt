package ussr.retr.fastmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import org.osmdroid.util.GeoPoint
import ussr.retr.fastmap.Objects.Helpers.RouteHelper
import ussr.retr.fastmap.Objects.Place

class PlaceBottomSheet(private val place: Place, private val routeHelper: RouteHelper) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.attraction_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (place.address?.amenity != null){
            view.findViewById<TextView>(R.id.name).text = place.address.amenity
            view.findViewById<TextView>(R.id.address).text = "${place.address.road} д.${place.address.house_number}, ${place.address.city}"
        }else{
            view.findViewById<TextView>(R.id.name).text = place.display_name
            view.findViewById<TextView>(R.id.address).text = place.address?.postcode
        }

        view.findViewById<MaterialButton>(R.id.createRoute).setOnClickListener {
            val geoPoint = GeoPoint(
                place.lat!!,
                place.lon!!
            )
            routeHelper.createRouteTo(geoPoint)
            this.dismiss()
        }
    }

    companion object {
        const val TAG = "Attraction bottom  sheet"
    }
}