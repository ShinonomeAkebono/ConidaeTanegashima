package com.example.kotlindae2nd

import android.app.Activity
import android.content.Context
import android.location.Location
import kotlin.math.abs

class InductionKonidae(blue: BluetoothKommunication,context: Context) {
    //スレッドとインスタンス化したいクラスの宣言
    private var driveThread: Thread? = null
    private var landDetectThread:Thread?=null
    private var shell: KShell
    private var shijimi: KShijimi
    private var mContext = context
    private var eye:ConidaEye
    private var key = blue
    //誘導関連の変数
    private var right = 0
    private var left = 0
    private var goalLat:Double = 0.0
    private var goalLon:Double = 0.0
    private var breaker = false
    //センサ関連の変数
    private var orientationAngles = FloatArray(3)
    private var goalAzimuth:Double? = null
    private var distance:Float? = null
    private var magNorth = 0.0
    private var minPressure = Float.MAX_VALUE
    private val gDist = 5
    //状態記録用の変数
    var state = 0
    var statelistener:StateListener? = null
    var goalListener:GoalListener? = null
    private lateinit var selfCheckThread: Thread

    interface StateListener {//状態が変化したときのイベントリスナー
        fun onStateChanged(state:Int)
    }
    interface GoalListener {//ゴール判定ができたときのイベントリスナー
        fun onGoalDetected()
    }

    init {
        println("車を用意するんだえ")
        println("エンジン始動だえ")
        shell = KShell(mContext)
        println("しじみも乗るんだえ")
        shijimi = KShijimi()
        driveLog("運転開始だえ")
        eye = ConidaEye(mContext as Activity)
        shell.setSensorLogger(shijimi)
        shell.ignition(key)
        //自分の状態を監視するスレッドを開始
        startCheck()
    }
    //自立走行するプログラム
    fun landAndGo(gLat:Double,gLong:Double){
        var isDriveOk = true
        landDetectThread=Thread{
            while(state.and(STATE_PRESSUREOK)==0) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    isDriveOk = false
                    break
                }
            }
            try {
                Thread.sleep(5000)
                shell.sendGoSign()
                Thread.sleep(40000)
            } catch (_: Exception) {
                isDriveOk = false
            }
            if(isDriveOk){

                drive(gLat,gLong)
            }
        }
        landDetectThread?.start()

    }
    fun drive(gLat:Double,gLong:Double) {
        driveThread = null
        goalLat = gLat
        goalLon = gLong
        driveLog("運転するんだえ")
        //スレッドを定義
        driveThread = Thread {
            while ((shell.nowLat==null)||(shell.nowLon==null)){
                driveLog("現在値取得中だえ")
                Thread.sleep(1000)
            }
            calculateToGoal()
            while (distance!! > gDist) {
                //目標方位を向く
                calculateToGoal()
                induction()
                if (breaker) {
                    driveLog("方向転換中に終わるんだえ")
                    stop()
                    break
                }
            }
            if (distance!! < gDist) {
                calculateToGoal()
                driveLog("目的地付近なんだえ")
                shell.axel(0,0)
                eye.takePhoto()
                goalListener?.onGoalDetected()
                stop()
            }
        }
        //スレッドを開始
        driveThread?.start()
    }
    fun setGoals(gLat:Double,gLong:Double){
        goalLat = gLat
        goalLon = gLong
    }
    fun driveForServer(gLat:Double,gLong:Double){
        driveThread = null
        goalLat = gLat
        goalLon = gLong
        driveLog("$gLat,$gLong に向けて運転するんだえ")
        //スレッドを定義
        driveThread = Thread {
            while ((shell.nowLat==null)||(shell.nowLon==null)){
                driveLog("現在値取得中だえ")
                Thread.sleep(1000)
            }
            calculateToGoal()
            while (true) {
                //目標方位を向く
                calculateToGoal()
                if (breaker) {
                    driveLog("方向転換中に終わるんだえ")
                    stop()
                    break
                }
                if (distance!! < gDist) {
                    driveLog("目的地付近なんだえ")
                    shell.axel(0,0)
                    eye.takePhoto()
                    goalListener?.onGoalDetected()
                }
                induction()
            }
        }
        //スレッドを開始
        driveThread?.start()
    }
    fun stop(){
        shell.axel(0,0)
        driveThread?.interrupt()
    }
    fun finish(){
        shell.axel(0,0)
        driveLog("もうやめるんだえ")
        //スレッドを止める
        landDetectThread?.interrupt()
        driveThread?.interrupt()
        selfCheckThread.interrupt()
        driveLog("止まるんだえ")
        shell.quit()
        driveLog("車を降りるんだえ")
        driveLog("しじみも降りるんだえ")
        shijimi.quit()
        eye.finish()
    }
    private fun induction(){
        do{
            driveLog("出力するんだえ")
            driveLog("$left,$right")
            shell.axel(left,right)
            try {
                Thread.sleep(20)
            }catch (e:InterruptedException){
                e.printStackTrace()
                breaker = true
                break
            }
            //出力値の更新は、下のwhileの条件式の中で行っている。
        }while (calculate()>10)
    }

    private fun calculateToGoal(){
        val results = FloatArray(2)
        driveLog("ゴールから逆算するんだえ")
        shell.nowLat?.let { shell.nowLon?.let { it1 ->
            Location.distanceBetween(it,
                it1,goalLat,goalLon,results)
        } }
        distance = results[0]
        println(results[0])
        goalAzimuth = if(results[1]+magNorth < 180){
            results[1]+magNorth
        }else{
            results[1] - 360 + magNorth
        }

        driveLog("ゴールまでの距離：{$distance}目標方位{$goalAzimuth}なんだえ")
    }
    private fun calculate(): Int {
        //センサ値の取得
        orientationAngles = shell.orientationAngles
        //現在の方位角を取得
        val nowAzimuth = orientationAngles[0]*180/Math.PI
        val delta = goalAzimuth?.minus(nowAzimuth)
        var phi = 0.0
        if((-360<= delta!!)&&(delta<=-180)){
            phi = delta+360
        }else if ((0<delta)&&(delta<180)){
            phi = delta
        }else if((-180<delta)&&(delta<0)) {
            phi = delta
        }else if((180<=delta)&&(delta<=360)){
            phi = delta-360
        }
        println(phi)
        phi /= 2
        val preRight = right
        val preLeft = left
        right = (0.5*preRight+0.5*(-0.00296296*phi*phi*phi-0.1333333*phi*phi+70.0)).toInt()
        left = (0.5*preLeft+0.5*(0.00296296*phi*phi*phi-0.1333333*phi*phi+70.0)).toInt()
        if(right>90){
            right=90
        }else if(right<-90){
            right=-90
        }
        if(left>90){
            left=90
        }else if(left<-90){
            left=-90
        }
        println("$right,$left")
        return abs(phi).toInt()
    }

    private fun driveLog(str:String){
        println(str)
        shijimi.driveRecord(str)
    }

    private fun startCheck(){
        selfCheckThread = Thread{
            while (true){
                checkSelfState()
                try {
                    Thread.sleep(100)
                }
                catch (e:InterruptedException){
                    break
                }
            }
        }
        selfCheckThread.start()
    }
    //自分の状態をチェックする関数
    private fun checkSelfState(){
        val lastState = state
        //以下で異常がないかの判定を行うが、else if文で判定しているため、優先度の高い異常から判定するようにすること。
        if(isReverse() && state.and(STATE_REVERSE)==0){//反転していて、stateの反転がtrueじゃない時
            state += STATE_REVERSE
        }
        else if(!isReverse()&&state.and(STATE_REVERSE)!=0){//反転していなくて、stateの反転がtrueのとき
            state -= STATE_REVERSE
        }

        if(key.isConnectionOK&&state.and(STATE_CONNECTION_ERR)!=0){//接続に問題が無くて、接続エラーがtrueのとき
            state -= STATE_CONNECTION_ERR
        }
        else if(!key.isConnectionOK&&state.and(STATE_CONNECTION_ERR)==0){//接続に問題があり、接続エラー状態でない時
            state += STATE_CONNECTION_ERR
        }

        if(isPressureOK()){
            if(state.and(STATE_PRESSUREOK)==0) {
                state += STATE_PRESSUREOK
            }
        }else{
            if(state.and(STATE_PRESSUREOK)!=0){
                state-= STATE_PRESSUREOK
            }
        }
        if(lastState!=state){//状態が変化していた時
            statelistener?.onStateChanged(state)
            eye.takePhoto()
        }

    }
    private fun isReverse():Boolean{
        if(shell.orientationAngles[1]>1.3962f||abs(shell.orientationAngles[2])>1.5707f){
            return true
        }
        return false
    }
    private  fun isPressureOK():Boolean{
        shell.pressureReading?:return false
        return if(shell.pressureReading!!< minPressure){
            minPressure=shell.pressureReading!!
            false
        }else{
            val fallDistance=(shell.pressureReading!!- minPressure) *8.00372
            fallDistance>15
        }
    }
    companion object{
        //自立走行でも使う状態
        const val STATE_PRESSUREOK=16
        const val STATE_REVERSE = 8
        const val STATE_CONNECTION_ERR = 4
        const val STATE_STACK = 2
        //サーバー走行の時に使う状態
        const val STATE_CONIDAE = 256
        const val STATE_OPERATOR = 512
        const val STATE_EXECUTING = 1024
        //サーバー側でいじる状態
        const val STATE_ONLINE = 65536
        const val STATE_NET_ERR = 131072
    }
    fun getStatusForQuery():String {
        return "lat=${shell.nowLat.toString()}&long=${shell.nowLon.toString()}&state=$state"
    }
    fun getGoalLat():Double{
        return goalLat
    }
    fun getGoalLong():Double{
        return goalLon
    }
}