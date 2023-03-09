package com.example.kotlindae2nd

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GestureDetectorCompat
import com.example.kotlindae2nd.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback{
    private lateinit var shell:KShell
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        detector = GestureDetectorCompat(this,MyGestureListener(this))
        shell = KShell(this)
        Toast.makeText(this,"ダブルタップで現在位置を表示します。",Toast.LENGTH_SHORT).show()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.uiSettings.isCompassEnabled=true
        mMap.uiSettings.isMapToolbarEnabled=true
        checkPermission()
        mMap.isMyLocationEnabled=true
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(0.0,0.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.setOnMapLongClickListener{
            mMap.addMarker(MarkerOptions().draggable(true).position(it).title("clickedPoint"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f),3000,null)
        }
        mMap.setOnMarkerClickListener{
            run {
                Toast.makeText(this, "${it.position.latitude},${it.position.longitude} is selected", Toast.LENGTH_SHORT).show()
                val dialog = StartDialogFragment(it.position.latitude,it.position.longitude,intent.getStringExtra("DEVICE_NAME").toString(),this@MapsActivity)
                dialog.show(supportFragmentManager,"title")
            }
            true
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            detector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun moveNowLocation(){

        val outPutText=if(shell.nowLat==null||shell.nowLon==null){
            "もう少々お待ちください。"
        }else{
         "現在位置:${shell.nowLat},${shell.nowLon}"
        }
        Toast.makeText(this, outPutText, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission(){//パーミッションの関数。基本コピペ
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this as Activity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }
    companion object{
        private const val REQUEST_CODE_PERMISSIONS = 60
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
    }
}
private class MyGestureListener(mapAct:MapsActivity):
    GestureDetector.SimpleOnGestureListener(){
    private var activity = mapAct
    override fun onDoubleTap(e: MotionEvent): Boolean {
        activity.moveNowLocation()
        return super.onDoubleTap(e)
    }
}