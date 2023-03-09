package com.example.kotlindae2nd

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.kotlindae2nd.databinding.ActivityServerModeConidaeSettingBinding

class ServerModeConidaeSettingActivity : AppCompatActivity(){
    private lateinit var binding: ActivityServerModeConidaeSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerModeConidaeSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initConnectDeviceSpinner()
        Toast.makeText(this, intent.getStringExtra("USERNAME"), Toast.LENGTH_SHORT).show()
        binding.btnStartOnServerMode.setOnClickListener {
            if(binding.spnSelect.selectedItem.toString()=="CONNECT_DEVICE_NONE"){
                Toast.makeText(this, "機体をペアリングしてください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                val serverFaceIntent = Intent(this, ServerConidaeFaceActivity::class.java)
                //接続先の機体名(ESP32の登録名)をインテントに含めて開始。
                serverFaceIntent.putExtra("USERNAME",intent.getStringExtra("USERNAME").toString())
                serverFaceIntent.putExtra("DEVICE_NAME",binding.spnSelect.selectedItem.toString())
                startActivity(serverFaceIntent)
            }
        }
    }
    private fun initConnectDeviceSpinner() {
        checkPermission()
        val mBluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = mBluetoothManager.adapter
        val mConnectDeviceSpinner = binding.spnSelect
        val pairedDevices: Set<BluetoothDevice> = mBluetoothAdapter.getBondedDevices()
        val mConnectDeviceAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        if (pairedDevices.isEmpty()) {
            //ペアリングできるデバイスが無い場合はCONNECT_DEVICE_NONEとして出される。
            mConnectDeviceAdapter.add("CONNECT_DEVICE_NONE")
        } else {
            for (bluetoothDevice in pairedDevices) {
                val deviceName = bluetoothDevice.name
                mConnectDeviceAdapter.add(deviceName)
            }
        }
        mConnectDeviceSpinner.setAdapter(mConnectDeviceAdapter)
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
