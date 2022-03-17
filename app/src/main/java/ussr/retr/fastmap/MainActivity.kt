package ussr.retr.fastmap

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import ussr.retr.fastmap.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance()
            .load(this, getSharedPreferences("map_shared-pref", Context.MODE_PRIVATE))
        initMap()
    }

    private fun initMap() {

        val MADskillsMap = XYTileSource(
            "MADskillsMap",
            1,
            20,
            256,
            ".png",
            arrayOf("http://map.madskill.ru/osm/")
        )

        binding.map.setTileSource(MADskillsMap)

        val mapController = binding.map.controller
        mapController.setZoom(18.0)
        val standardGeoPoint = GeoPoint(54.1821, 45.181)
        mapController.setCenter(standardGeoPoint)
        initLocation()
    }

    private fun initLocation() {
        if (locationAccess()) {
            val locationOverlay = MyLocationNewOverlay(binding.map)
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            binding.map.overlayManager.add(locationOverlay)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        initLocation()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            1 -> initLocation()
            2 -> initLocation()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
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