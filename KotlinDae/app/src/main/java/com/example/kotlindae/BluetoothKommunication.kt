package com.example.kotlindae

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothKommunication(deviceName: String,context: Context) {//引数には、接続するESPの名前と呼び出し元のアクティビティのコンテクストをとる。
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var mBluetoothSocket: BluetoothSocket
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private var count = 0
    private var connectDeviceName: String = deviceName
    private var nowContext = context
    var isConnectionOK = false//接続状態を保存する変数
    init {
        startBluetoothConnection()//コンストラクタでもう接続をしてしまう。
    }
    private fun startBluetoothConnection(){
        mBluetoothManager = nowContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager//BluetoothManagerはBluetoothを使う際に必須。
        mBluetoothAdapter = if(Build.VERSION.SDK_INT>31){
            mBluetoothManager.adapter
        }else{
            BluetoothAdapter.getDefaultAdapter()
        }

        if(!mBluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            checkPermission()
            nowContext.startActivity(enableBtIntent)
        }
        this.connectDevice()
    }

    private fun connectDevice() {
        checkPermission()
        lateinit var connectBluetoothDevice: BluetoothDevice
        val pairedDevices = mBluetoothAdapter.bondedDevices
        //ペアリングされているデバイスたちから、渡されたデバイス名と一致するものを選択。
        if(pairedDevices!=null){
            for(bluetoothDevice in pairedDevices){
                if(connectDeviceName == bluetoothDevice.name){
                    connectBluetoothDevice = bluetoothDevice
                    break
                }
            }
        }
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        try{
            mBluetoothSocket = connectBluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            println("ソケットだえ{$mBluetoothSocket}")
        }catch (e:java.lang.Exception){
            println("やばいんだえ")
            e.printStackTrace()
            if(count<3){
                try{
                    Thread.sleep(100)
                }catch (ex:InterruptedException){
                    ex.printStackTrace()
                }
                println("もう一度トライするんだえ")
                count++
                this.connectDevice()
            }
            else{
                println("諦めるんだえ")
            }
        }
        println("ソケットが取得できたんだえ")
        try{
            mBluetoothSocket.connect()
            mOutputStream = mBluetoothSocket.outputStream
            mInputStream = mBluetoothSocket.inputStream
            println("接続ができたんだえ")
            isConnectionOK = true
        }catch (e:Exception){
            println("何らかの問題が発生したんだえ")
            isConnectionOK = false
            try{
                mBluetoothSocket.close()
                println("もう一度接続を試みるんだえ")
                this.connectDevice()
            }catch (ex:IOException){
                println("閉じれなかったんだえ")
                ex.printStackTrace()
            }
        }
    }
    fun sendData(data:String){
        println("データを送るんだえ:$data")
        val sendBytes = data.toByteArray()
        try{
            mOutputStream.write(sendBytes)
        }catch (writeIOException:IOException){
            isConnectionOK = false
            println("送れないんだえ")
            println("もう一度接続を試みるんだえ")
            connectDevice()
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            nowContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                nowContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                nowContext as Activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }
    fun quit(){
        mBluetoothSocket.close()
        isConnectionOK = false
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