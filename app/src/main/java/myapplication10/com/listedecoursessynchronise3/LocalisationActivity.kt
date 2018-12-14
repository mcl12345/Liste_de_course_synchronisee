package myapplication10.com.listedecoursessynchronise3

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.localisation_activity.*
import org.jetbrains.anko.toast

class LocalisationActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    lateinit var apiClient : GoogleApiClient
    var gapiOkay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.localisation_activity)

        //-------------------------------------------------------------------
        // Localisation
        // -------------------------------------------------------------------
        apiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build()


        if(gapiOkay) {
            // On vérifie les permissions
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val pos = LocationServices.FusedLocationApi.getLastLocation(apiClient)
                latitude_tv.text = "Lat = $pos?.latitude"
                longitude_tv.text = "Lat = $pos?.longitude"
                altitude_tv.text = "Lat = $pos?.altitude"
            } else {
                // On demande la permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1
                );
            }
            val lr = LocationRequest.create()
            lr.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            lr.interval = 60 * 1000 // 1 minute
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, lr) {
                // La position est dans "it" ( ici jamais null )
                latitude_tv.text = "Lat = $it?.latitude"
                longitude_tv.text = "Lat = $it?.longitude"
                altitude_tv.text = "Lat = $it?.altitude"
            }
        }
        //-------------------------------------------------------------------



    }


    override fun onConnected(bundle: Bundle?) {
        gapiOkay = true
    }

    override fun onConnectionSuspended(cause: Int) {
        gapiOkay = false
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        toast("Connection to Google API Failed")
    }

    override fun onStart() {
        super.onStart()
        apiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        apiClient.disconnect()
    }
}