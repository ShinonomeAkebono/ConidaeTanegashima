package com.example.kotlindae

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlindae.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity(),InductionKonidae.StateListener {
    private lateinit var binding: ActivityMainBinding
    //Conidaeを使う手順:
    // 1.BluetoothKommunicationをインスタンス化。接続するデバイス名とコンテクストを渡す。
    // 2.Conidaeにインスタンス化したBluetoothKommunicationとコンテクストを渡してインスタンス化
    // 3.Conidaeにゴールを渡してdrive()を開始する。
    // 4.終了時はConidaeのquit()メゾットを呼び出す。
    private lateinit var conidae: InductionKonidae
    private lateinit var blue:BluetoothKommunication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (allPermissionsGranted()) {
            startApplication()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        binding.btnStart.setOnClickListener {
            blue = BluetoothKommunication("Shell", this)
            conidae = InductionKonidae(blue, this)
            conidae.statelistener = this
            conidae.drive(35.6990494564085, 139.74353592943586)
        }
        binding.btnStop.setOnClickListener {
            conidae.finish()
        }

        binding.btnAction.setOnClickListener {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        conidae.finish()
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun startApplication(){

    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startApplication()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    companion object{
        private const val TAG = "ConidaeApp"
        private const val REQUEST_CODE_PERMISSIONS = 100
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onStateChanged(state: Int) {
        if(state==InductionKonidae.STATE_REVERSE){
            runOnUiThread{Toast.makeText(this, "反転", Toast.LENGTH_SHORT).show()}
        }
        else{
            runOnUiThread { Toast.makeText(this, "問題なし", Toast.LENGTH_SHORT).show() }
        }
    }
}