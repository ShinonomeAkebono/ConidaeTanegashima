package com.example.kotlindae2nd

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_PRESSURE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.math.PI

class KShell(context: Context) :SensorEventListener{
    //姿勢角関連
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)
    var pressureReading:Float?=null
    //GPS関連
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient//1.
    private lateinit var locationCallback: LocationCallback//2.

    var nowLat:Double? = null
    var nowLon:Double? = null
    private var mContext:Context = context
    //Bluetooth関連
    private var blue:BluetoothKommunication? = null
    private var mRight = 0
    private var mLeft = 0

    init {
        locationStart()//locationServiceを使うための関数。
        onResume()
    }
    //オプション
    private var listenerShijimi:KShijimi? = null

    //以下クラス内の関数
    override fun onSensorChanged(event: SensorEvent) {//センサ値が変化したときに呼ばれる。
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }else if(event.sensor.type==Sensor.TYPE_PRESSURE){
            pressureReading=event.values[0]

        }
        updateOrientationAngles()
    }

    private fun updateOrientationAngles(){//姿勢角を更新する関数。
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        val rotationFinal = FloatArray(9)
        //軸の変更
        SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_Z,SensorManager.AXIS_X,rotationFinal)
        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationFinal, orientationAngles)
        // "mOrientationAngles" now has up-to-date information.
        //println("${orientationAngles[0]/PI*180},${orientationAngles[1]/PI*180},${orientationAngles[2]/PI*180}")

    }

    override fun onAccuracyChanged(Sensor: Sensor?, p1: Int) {//精度が変わったときに呼ばれる。
        println(p1)
    }

    private fun locationStart(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext)
        checkPermission()
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location:Location?->
            nowLon = location?.longitude
            nowLat = location?.latitude
        }
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val loc = p0.lastLocation
                nowLat = loc?.latitude
                nowLon = loc?.longitude
                takeLog()
            }
        }
        val locationRequest = createRequest()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper())
    }

    private fun createRequest():LocationRequest =
        // New builder
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

    fun onResume(){//センサをリスナーに登録。
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_UI)
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this,magneticField,SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_UI)
        val pressure=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(this,pressure,SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_UI)
    }

    fun onPause(){//画面を閉じるときにはセンサの登録を解除。
        sensorManager.unregisterListener(this)
    }

    fun axel(left:Int,right:Int){//加速用。
        mRight = -right + 90
        mLeft = left + 90
        blue?.sendData("$mLeft,$mRight;")
    }

    fun quit(){//終了時の関数。
        //停止
        blue?.sendData("90,90;")
        //Bluetooth接続切る
        blue?.quit()
        //センサの登録を解除
        sensorManager.unregisterListener(this)
    }

    fun ignition(key : BluetoothKommunication){
        listenerShijimi?.driveRecord("ブルルルル...")
        this.blue = key
    }

    fun setSensorLogger(shijimi:KShijimi){
        this.listenerShijimi = shijimi
    }

    private fun takeLog(){
        if(listenerShijimi!=null){
            val logStr = "$nowLat,$nowLon"
            listenerShijimi!!.sensorRecord(logStr)
        }
    }
    fun sendGoSign(){
        blue?.sendData("52120")
    }

    private fun checkPermission(){//パーミッションの関数。基本コピペ
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mContext as Activity,
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
