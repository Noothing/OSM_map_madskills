package ussr.retr.fastmap

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import ussr.retr.fastmap.Objects.Attractions
import ussr.retr.fastmap.Objects.Helpers.RouteHelper
import ussr.retr.fastmap.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding

    private val attractionsList: MutableList<Attractions> = mutableListOf(
        Attractions(
            "Кафедральный собор святого праведного воина Феодора Ушакова",
            "ул. Советская, д. 53",
            GeoPoint(54.181346, 45.181057)
        ),
        Attractions(
            "Мордовский Музей Изобразительных Искусств им. С.Д. Эрьзи",
            "ул. Коммунистическая, 61",
            GeoPoint(54.184804, 45.179748)
        ),
        Attractions(
            "Мордовия Арена",
            "Волгоградская улица, 1",
            GeoPoint(54.181329, 45.203191)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance()
            .load(this, getSharedPreferences("map_shared-pref", Context.MODE_PRIVATE))
        initMap()
    }

    private lateinit var routeHelper: RouteHelper
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var compassOverlay: CompassOverlay
    private lateinit var rotationGestureOverlay: RotationGestureOverlay

    private fun initMap() {
        val madSkillsMap = XYTileSource(
            "MADskillsMap",
            1,
            20,
            256,
            ".png",
            arrayOf("http://map.madskill.ru/osm/")
        )

//        val originalMap = XYTileSource(
//            "Mapnik",
//            1,
//            18,
//            256,
//            ".png",
//            arrayOf("http://tile.openstreetmap.org/")
//        )

        binding.map.setTileSource(madSkillsMap)

        val mapController = binding.map.controller
        mapController.setZoom(18.0)
        val standardGeoPoint = GeoPoint(54.1821, 45.181)
        mapController.setCenter(standardGeoPoint)

        locationOverlay = MyLocationNewOverlay(binding.map)
        compassOverlay = CompassOverlay(this, binding.map)

        rotationGestureOverlay = RotationGestureOverlay(binding.map)
        rotationGestureOverlay.isEnabled
        binding.map.setMultiTouchControls(true);
        binding.map.overlays.add(rotationGestureOverlay)

        initLocation()
    }

    private var lastOrientation = 0f
    private lateinit var myLocation: GeoPoint

    private fun initLocation() {
        myLocation = GeoPoint(0.0, 0.0)

        if (locationAccess()) {
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            binding.map.overlays.add(locationOverlay)

            routeHelper = RouteHelper(binding.map, locationOverlay, supportFragmentManager)
            routeHelper.createRouteOverlay()

            binding.toggleRoute.visibility = View.VISIBLE
            binding.toggleRoute.setOnClickListener {
                if (routeHelper.routeHelperEnabled()){
                    routeHelper.disableRouteMode()
                    (it as MaterialButton).text = "Построить маршрут"
                }else{
                    routeHelper.enableRouteCreating()
                    (it as MaterialButton).text = "Оставить постройку маршрута"
                }
            }

            compassOverlay.enableCompass()
            compassOverlay.mOrientationProvider.startOrientationProvider { orientation, _ ->
                if (lastOrientation + 10 < orientation || lastOrientation - 10 > orientation) {
                    binding.map.setMapOrientation(orientation, true)
                    lastOrientation = orientation
                }
            }
            binding.map.overlays.add(compassOverlay)
        }
    }

    private fun locationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    private fun locationAccess(): Boolean {
        if (locationEnabled()) {
            if (EasyPermissions.hasPermissions(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                if (EasyPermissions.hasPermissions(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {

                    return true

                } else {
                    if (EasyPermissions.permissionPermanentlyDenied(
                            this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) {
                        EasyPermissions.requestPermissions(
                            this,
                            "Для работы приложения необходим доступ к геолокации",
                            0,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    } else {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Нет доступа к геолокации")
                            .setMessage("Для работы приложения требуется доступ к геолокации. Включите её, пожалуйста")
                            .setPositiveButton("Включить") { dialog, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
                                startActivity(intent)
                                dialog.dismiss()
                            }.show()
                    }
                    return false
                }
            } else {
                if (EasyPermissions.permissionPermanentlyDenied(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    EasyPermissions.requestPermissions(
                        this,
                        "Для работы приложения необходим доступ к геолокации",
                        1,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Нет доступа к геолокации")
                        .setMessage("Для работы приложения требуется доступ к геолокации. Включите её, пожалуйста")
                        .setPositiveButton("Включить") { dialog, _ ->
                            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
                            startActivity(intent)
                            dialog.dismiss()
                        }.show()
                }
                return false
            }
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Выключена геолокация")
                .setMessage("Для работы приложения требуется доступ к геолокации. Включите её, пожалуйста")
                .setPositiveButton("Включить") { dialog, _ ->
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(intent)
                    dialog.dismiss()
                }.show()
            return false
        }
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        initLocation()
    }

    override fun onPermissionsGranted(
        requestCode: Int,
        perms: MutableList<String>
    ) {
        when (requestCode) {
            1 -> initLocation()
            2 -> initLocation()
        }
    }

    override fun onPermissionsDenied(
        requestCode: Int,
        perms: MutableList<String>
    ) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog
                .Builder(this)
                .setTitle("Предупреждения")
                .setRationale("Для работы приложения требуется доступ к геолокации. Чтобы её включить, нажмите на кнопку")
                .build()
                .show()
        }
    }

}