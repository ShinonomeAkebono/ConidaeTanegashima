package com.example.kotlindae2nd

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.kotlindae2nd.databinding.ActivityCommanderMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONObject

class CommanderMapActivity : AppCompatActivity(), OnMapReadyCallback ,SendGoalDialog.ListSelectedListener{
    private lateinit var shell:KShell
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCommanderMapBinding
    private lateinit var detector: GestureDetectorCompat
    private lateinit var offlineDae:Bitmap
    private lateinit var onlineDae:Bitmap
    private lateinit var errDae:Bitmap
    private lateinit var offlineOpe:Bitmap
    private lateinit var onlineOpe:Bitmap
    private lateinit var errOpe:Bitmap
    private var markerArray = mutableListOf<Marker>()
    private var goalArray = mutableListOf<Marker>()
    private var polylineArray = mutableListOf<Polyline>()
    private var state = InductionKonidae.STATE_OPERATOR
    private var gLat = 0.0
    private var gLong = 0.0
    private var recipientName = "NAME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommanderMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBitmaps()
        shell = KShell(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        detector = GestureDetectorCompat(this,MyGestureListener(this))
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //マップのセットアップ
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.uiSettings.isMapToolbarEnabled=true
        mMap.uiSettings.isCompassEnabled=true
        checkPermission()
        mMap.isMyLocationEnabled = true
        // Add a marker in Sydney and move the camera
        val tanegashima = LatLng(30.37503154252748, 130.95760316154673)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tanegashima))
        mMap.setOnMapLongClickListener{
            mMap.addMarker(MarkerOptions().draggable(true).position(it).title("Goal"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f),3000,null)
        }
        mMap.setOnMarkerClickListener{
            run {
                if(it.title.equals("Goal")){
                    Toast.makeText(this, "${it.position.latitude},${it.position.longitude} is selected", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "${it.title}is Selected", Toast.LENGTH_SHORT).show()
                }
                val dialog = SendGoalDialog(markerArray)
                gLat = it.position.latitude
                gLong = it.position.longitude
                dialog.listSelectedListener = this
                dialog.show(supportFragmentManager,"title")
            }
            true
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        communicateServer(LOGOUT)
    }
    private fun removeAllOverlays(){ //マーカーを全削除する関数
        for(marker in markerArray){
            try{
                println(marker.title)
                marker.remove()
            }
            catch(e:Exception){
                print("なにかしら問題が発生したんだえ")
            }
        }
        for(polyLine in polylineArray){
            try{
                polyLine.remove()
            }
            catch(e:Exception){
                print("なにかしら問題が発生したんだえ")
            }
        }
        for(goal in goalArray){
            try{
                goal.remove()
            }
            catch (e:Exception){
                print("何かしら問題が発生したんだえ")
            }
        }
        markerArray = mutableListOf()
        goalArray = mutableListOf()
        polylineArray = mutableListOf()
    }

    override fun onListSelected(userName: String) {
        super.onListSelected(userName)
        Toast.makeText(this, "${userName}が選択されました", Toast.LENGTH_SHORT).show()
        recipientName = userName
        communicateServer(SET_GOAL)
    }
    private fun communicateServer(sendCommand:Int){//サーバー通信をする関数。渡されたコマンドによって動作内容を変える。
        val queue= Volley.newRequestQueue(this)
        var url="https://script.google.com/macros/s/ここにIDをいれるよ！！！！/exec?"
        url += when (sendCommand) {
            UPDATE_MAP -> {//マップ更新
                "comm=$UPDATE_MAP"+"&name=${intent.getStringExtra("USERNAME")}&${makeExtraQuery(UPDATE_MAP)}"
            }
            SET_GOAL -> {
                "comm=$SET_GOAL"+"&name=${intent.getStringExtra("USERNAME")}&${makeExtraQuery(SET_GOAL)}"
            }
            LOGOUT -> {
                "comm=$LOGOUT"+"&name=${intent.getStringExtra("USERNAME")}&${makeExtraQuery(LOGOUT)}"
            }
            else -> {
                return
            }
        }
        println(url)
        val stringRequest= StringRequest(
            Request.Method.GET,url,
            { response ->println(response)
                //レスポンスは"コマンド番号#ペイロード"の形で文字列として返ってくる。
                val responseList = response.split("#")
                val returnCommand = responseList[0]
                val payload = responseList[1]
                var command = 0
                try{
                    command = returnCommand.toInt()
                }catch (e:Exception){
                    Toast.makeText(this, "エラー：$returnCommand", Toast.LENGTH_LONG).show()
                }
                when (command) {
                    GOTO_GRANTED_GOAL -> {
                        print(response)
                    }
                    UPDATE_MAP -> {//帰ってきたコマンドがマップ更新のとき
                        val retJson = JSONObject(payload)
                        val keys: Iterator<String> = retJson.keys()
                        removeAllOverlays()//マップ上のオーバーレイを全消去
                        while(keys.hasNext()){
                            val key = keys.next()
                            val json = retJson.getJSONObject(key)
                            try{
                                val currentPosition = LatLng(json.getDouble("Latitude"),json.getDouble("Longitude"))
                                val usrName = json.getString("UserName")
                                val status = json.getInt("Status")
                                val option = createMarkOptionForStatus(currentPosition,usrName,status)
                                val marker = mMap.addMarker(option)
                                if(status.and(InductionKonidae.STATE_EXECUTING)!=0){
                                    try {
                                        val goalPosition = LatLng(json.getDouble("GoalLat"),json.getDouble("GoalLong"))
                                        val gOption = MarkerOptions().position(goalPosition).title("Goal")
                                        val goalMark = mMap.addMarker(gOption)
                                        goalArray.add(goalMark!!)
                                        val lineOption = PolylineOptions().add(currentPosition).add(goalPosition).color(ContextCompat.getColor(this,R.color.purple_200))
                                        val lineToGoal = mMap.addPolyline(lineOption)
                                        polylineArray.add(lineToGoal)
                                    }catch (e:Exception){
                                        Toast.makeText(this, "${usrName}に正しいゴールが設定されていません！", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                markerArray.add(marker!!)//マップにマーカーを追加しつつ、あとで一括管理をするためにリストに格納。
                            }catch (e:java.lang.Exception){
                                println("It does not Double!")
                            }
                        }
                    }
                    SET_GOAL -> {
                        Toast.makeText(this, payload, Toast.LENGTH_SHORT).show()
                    }
                    LOGOUT -> {
                        Toast.makeText(this, "ログアウトしました。", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            {})
        queue.add(stringRequest)
    }
    private fun createMarkOptionForStatus(position:LatLng,usrName:String,status:Int): MarkerOptions {//and()==0とすればfalse,and()==!0はtrue
        val returnOption = MarkerOptions().position(position).title(usrName)
        if(status.and(InductionKonidae.STATE_CONIDAE)!=0){//conidaeだった時
            if(status.and(InductionKonidae.STATE_NET_ERR)!=0){//ネットエラーなら
                returnOption.icon(BitmapDescriptorFactory.fromBitmap(errDae))
            }
            else{
                if(status.and(InductionKonidae.STATE_ONLINE)!=0){//オンラインなら
                    returnOption.icon(BitmapDescriptorFactory.fromBitmap(onlineDae))
                }
                else{//オフラインなら
                    returnOption.icon(BitmapDescriptorFactory.fromBitmap(offlineDae))
                }
            }
        }
        else if(status.and(InductionKonidae.STATE_OPERATOR)!=0){//operatorだった時
            if(status.and(InductionKonidae.STATE_NET_ERR)!=0){//ネットエラーなら
                returnOption.icon(BitmapDescriptorFactory.fromBitmap(errOpe))
            }
            else{
                if(status.and(InductionKonidae.STATE_ONLINE)!=0){//オンラインなら
                    returnOption.icon(BitmapDescriptorFactory.fromBitmap(onlineOpe))
                }
                else{//オフラインなら
                    returnOption.icon(BitmapDescriptorFactory.fromBitmap(offlineOpe))
                }
            }
        }

        return returnOption
    }
    private fun initBitmaps(){
        offlineDae = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources,R.drawable.offlinedae),100,100,true)
        onlineDae = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources,R.drawable.onlinedae),100,100,true)
        errDae = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources,R.drawable.errconidae),100,100,true)
        offlineOpe = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources,R.drawable.offlineoperator),100,140,true)
        onlineOpe = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources,R.drawable.onlineoperator),100,140,true)
        errOpe = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources,R.drawable.erroroperator),100,140,true)
    }
    private fun makeExtraQuery(sendCommand: Int):String{//コマンドによってクエリに追加する内容を変える関数。
        var sendQuery = "lat=${shell.nowLat.toString()}&long=${shell.nowLon.toString()}&state=$state"
        if(sendCommand== SET_GOAL){
            sendQuery+="&recipient=$recipientName&gLat=$gLat&gLong=$gLong&gUser=NONE"
        }
        return sendQuery

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            detector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }
    fun updateMap(){
        Toast.makeText(this, "更新中...", Toast.LENGTH_SHORT).show()
        communicateServer(UPDATE_MAP)
        Toast.makeText(this,"更新が完了しました！",Toast.LENGTH_SHORT).show()
    }

    private class MyGestureListener(mapAct:CommanderMapActivity):
        GestureDetector.SimpleOnGestureListener() {
        private var activity = mapAct
        override fun onDoubleTap(e: MotionEvent): Boolean {
            activity.updateMap()
            return super.onDoubleTap(e)
        }
    }
    companion object{
        private const val GOTO_GRANTED_GOAL = 2
        private const val UPDATE_MAP = 4
        private const val SET_GOAL = 8
        private const val LOGOUT = 64
        private const val REQUEST_CODE_PERMISSIONS = 90
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
    }
    private fun checkPermission(){//パーミッションの関数。基本コピペ
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this as Activity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }
}