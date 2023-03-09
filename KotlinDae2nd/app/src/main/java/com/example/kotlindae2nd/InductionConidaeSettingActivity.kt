package com.example.kotlindae2nd

import android.Manifest
import android.R
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.kotlindae2nd.databinding.ActivityInductionConidaeSettingBinding

class InductionConidaeSettingActivity : AppCompatActivity(){
    private lateinit var binding: ActivityInductionConidaeSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInductionConidaeSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initConnectDeviceSpinner()
        binding.btnStartOnInduction.setOnClickListener {
            val lat = binding.editLatitude.text.toString()
            val long = binding.editLongitude.text.toString()
            if(lat!="Latitude"&&long!="Longitude"){
                try{
                    val latitude = lat.toDouble()
                    val longitude = long.toDouble()
                    if(lat.length<17&&long.length<17) {
                        Toast.makeText(this, "入力桁が少なすぎます", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val inductionIntent = Intent(this,InductionConidaeFaceActivity::class.java)
                    inductionIntent.putExtra("GOAL_LAT",latitude)
                    inductionIntent.putExtra("GOAL_LONG",longitude)
                    inductionIntent.putExtra("DEVICE_NAME",binding.spnSelect.selectedItem.toString())
                    startActivity(inductionIntent)
                }
                catch(e:java.lang.Exception){
                    android.widget.Toast.makeText(this, "有効な値を入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }else{
                Toast.makeText(this, "Latitude,Longitudeに値を入力してください", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSelectByMap.setOnClickListener {
            val mapIntent = Intent(this, MapsActivity::class.java)
            mapIntent.putExtra("DEVICE_NAME",binding.spnSelect.selectedItem.toString())
            startActivity(mapIntent)
        }
    }
    private fun initConnectDeviceSpinner() {
        checkPermission()
        val mBluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = mBluetoothManager.adapter
        val mConnectDeviceSpinner = binding.spnSelect
        val pairedDevices: Set<BluetoothDevice> = mBluetoothAdapter.bondedDevices
        val mConnectDeviceAdapter = ArrayAdapter<String>(this, R.layout.simple_spinner_item)
        if (pairedDevices.isEmpty()) {
            mConnectDeviceAdapter.add("CONNECT_DEVICE_NONE")
        } else {
            for (bluetoothDevice in pairedDevices) {
                val deviceName = bluetoothDevice.name
                mConnectDeviceAdapter.add(deviceName)
            }
        }
        mConnectDeviceSpinner.adapter = mConnectDeviceAdapter
    }
    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this as Activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }
    companion object{
        private const val REQUEST_CODE_PERMISSIONS = 60
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT
            ).toTypedArray()
    }
}
