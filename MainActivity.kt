import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback, AMapLocationListener {

    private lateinit var map: GoogleMap
    private lateinit var locationClient: AMapLocationClient
    private val permissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        locationClient = AMapLocationClient(this)
        val locationOption = AMapLocationClientOption()
        locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        locationClient.setLocationOption(locationOption)
        locationClient.startLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), permissionRequestCode)
            return
        }
        map.isMyLocationEnabled = true
    }

    override fun onLocationChanged(aMapLocation: AMapLocation) {
        if (aMapLocation.errorCode == 0) {
            val location = Location("AMap")
            location.latitude = aMapLocation.latitude
            location.longitude = aMapLocation.longitude

            // Update map with current location
            val latLng = LatLng(aMapLocation.latitude, aMapLocation.longitude)
            map.addMarker(MarkerOptions().position(latLng))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            // Calculate distance to station and other tasks
            //...
        } else {
            Log.e("AMapError", "Location Error, ErrCode:" + aMapLocation.errorCode + ", errInfo:" + aMapLocation.errorInfo)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Permissions denied. Cannot use location services.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}