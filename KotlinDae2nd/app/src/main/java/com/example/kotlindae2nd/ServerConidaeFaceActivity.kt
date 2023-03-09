package com.example.kotlindae2nd

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.kotlindae2nd.databinding.ActivityServerConidaeFaceBinding

class ServerConidaeFaceActivity : AppCompatActivity(),InductionKonidae.StateListener,InductionKonidae.GoalListener {

    private lateinit var binding:ActivityServerConidaeFaceBinding
    private lateinit var key:BluetoothKommunication
    private lateinit var conidae:InductionKonidae
    private lateinit var faceThread:Thread
    private var isFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerConidaeFaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        key = BluetoothKommunication(intent.getStringExtra("DEVICE_NAME")!!,this)
        conidae = InductionKonidae(key,this)
        conidae.statelistener = this
        conidae.state += InductionKonidae.STATE_CONIDAE//stateをコニダエとして登録。
        faceThread = Thread{
            while (true){
                try {
                    Thread.sleep(15000)
                    serverDrive(REGISTER_AND_REQUEST_GOAL)
                }catch (e:java.lang.Exception){
                    break
                }
            }
        }
        faceThread.start()
    }
    private fun serverDrive(sendCommand:Int){
        val queue=Volley.newRequestQueue(this)
        var url="https://script.google.com/macros/s/ここにIDをいれるよ！！！！/exec?"
        url += when (sendCommand) {
            REGISTER_AND_REQUEST_GOAL -> {
                "comm=2"
            }
            NOTICE_GOAL -> {
                "comm=16"
            }
            LOGOUT -> {
                "comm=$LOGOUT"
            }
            else -> {
                return
            }
        }
        url += "&name=${intent.getStringExtra("USERNAME")}&${conidae.getStatusForQuery()}"
        val stringRequest= StringRequest(
            Request.Method.GET,url,
            { response ->println(response)
                //レスポンスは"コマンド番号#ペイロード"の形で文字列として返ってくる。
                val responseList = response.split("#")
                val command = responseList[0]
                val payload = responseList[1]
                if(command.toInt()== REGISTER_AND_REQUEST_GOAL){
                    val goal = payload.split(",")
                    if(conidae.getGoalLat()!=goal[0].toDouble()||conidae.getGoalLong()!=goal[1].toDouble()){
                        //新たに取得した目的地がこれまでと違った場合は、conidaeのdriveThreadを一旦中断して新しく始める。
                        if(isFirst){
                            conidae.driveForServer(goal[0].toDouble(),goal[1].toDouble())
                            isFirst = false
                        }else{
                            conidae.setGoals(goal[0].toDouble(),goal[1].toDouble())
                        }
                    }
                    if(conidae.state.and(InductionKonidae.STATE_EXECUTING)==0){//ミッション実行中のステータスでないとき
                        conidae.state+=InductionKonidae.STATE_EXECUTING
                    }
                }
                else if(command.toInt()== NOTICE_GOAL){
                }
            },
            {})
        queue.add(stringRequest)
    }
    override fun onDestroy() {
        super.onDestroy()

        conidae.finish()
        serverDrive(LOGOUT)
        faceThread.interrupt()
    }
    override fun onStateChanged(state: Int){
        if(state.and(InductionKonidae.STATE_REVERSE)!=0){
            nothingFace()
        }
        else if(state.and(InductionKonidae.STATE_CONNECTION_ERR)!=0){
            troubleFace()
        }
        else{
            normalFace()
        }
    }
    //それぞれの表情について，changeFace()を呼び出す．
    fun fightFace(){
        changeFace(R.drawable.fightleft,R.drawable.fightright,R.drawable.fight)
    }
    private fun normalFace(){
        changeFace(R.drawable.nomalleft,R.drawable.nomalright,R.drawable.smile)
    }
    private fun troubleFace(){
        changeFace(R.drawable.troubleleft,R.drawable.troubleright,R.drawable.fight)
    }
    fun smileFace(){
        changeFace(R.drawable.smileright,R.drawable.smileleft,R.drawable.smile)
    }
    private fun nothingFace(){
        changeFace(R.drawable.nothingleft,R.drawable.nothingright,R.drawable.fight)
    }
    fun surpriseFace(){
        changeFace(R.drawable.nomalleft,R.drawable.nomalright,R.drawable.surprise)
    }
    private fun changeFace(left:Int, right:Int, mouth:Int){
        val rightEye = binding.imgRightEye
        val leftEye = binding.imgLeftEye
        val mMouth = binding.imgMouth
        runOnUiThread(kotlinx.coroutines.Runnable {
            rightEye.setImageResource(right)
            leftEye.setImageResource(left)
            mMouth.setImageResource(mouth)
        })
    }
    override fun onGoalDetected() {
        if(conidae.state.and(InductionKonidae.STATE_EXECUTING)!=0){
            conidae.state -= InductionKonidae.STATE_EXECUTING
        }
        serverDrive(NOTICE_GOAL)
    }
    companion object{
        const val REGISTER_AND_REQUEST_GOAL = 2
        const val NOTICE_GOAL = 16
        const val LOGOUT = 64
    }
}