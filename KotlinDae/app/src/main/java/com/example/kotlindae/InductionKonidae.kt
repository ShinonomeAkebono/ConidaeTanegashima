package com.example.kotlindae

import android.content.Context
import android.location.Location
import kotlin.math.abs

class InductionKonidae(blue: BluetoothKommunication,context: Context) {
    //スレッドとインスタンス化したいクラスの宣言
    private lateinit var driveThread: Thread
    private var shell: KShell
    private var shijimi: KShijimi
    private var mContext = context
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
    //状態記録用の変数
    private var state = STATE_OK
    var statelistener:StateListener? = null
    private lateinit var selfCheckThread: Thread

    interface StateListener {//状態が変化したときのイベントリスナー
        fun onStateChanged(state:Int)
    }

    init {
        println("車を用意するんだえ")
        println("エンジン始動だえ")
        shell = KShell(mContext)
        println("しじみも乗るんだえ")
        shijimi = KShijimi()
        driveLog("運転開始だえ")
        shell.setSensorLogger(shijimi)
        shell.ignition(key)
        //自分の状態を監視するスレッドを開始
        startCheck()
    }
    //自立走行するプログラム
    fun drive(gLat:Double,gLong:Double) {
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
            while (distance!! > 10) {
                //目標方位を向く
                induction()
                if (breaker) {
                    driveLog("方向転換中に終わるんだえ")
                    quit()
                    break
                }
                //前進するプログラムはここから
                calculateToGoal()
                try {
                    while (calculate() < 30) {
                        shell.axel(70, 70)
                        Thread.sleep(50)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    driveLog("ドライブ終了だえ")
                    quit()
                    break
                }
            }
            if (distance!! < 10) {
                driveLog("目的地付近なんだえ")
                quit()
            }
        }
        //スレッドを開始
        driveThread.start()
    }
    fun quit(){
        shell.axel(0,0)
    }
    fun finish(){
        driveLog("もうやめるんだえ")
        //スレッドを止める
        driveThread.interrupt()
        selfCheckThread.interrupt()
        driveLog("止まるんだえ")
        shell.quit()
        driveLog("車を降りるんだえ")
        driveLog("しじみも降りるんだえ")
        shijimi.quit()
    }
    private fun induction(){
        do{
            //driveLog("出力するんだえ")
            //driveLog("$left,$right")
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
        goalAzimuth = if(results[1]+magNorth < 180){
            results[1]+magNorth
        }else{
            results[1] - 360 + magNorth
        }

        driveLog("ゴールまでの距離：{$distance}目標方位{$goalAzimuth}なんだえ")
    }
    private fun calculate(): Int {
        driveLog("センサ値を取得するんだえ")
        //センサ値の取得
        orientationAngles = shell.orientationAngles
        //現在の方位角を取得
        val nowAzimuth = orientationAngles[0]*180/Math.PI
        val delta = goalAzimuth?.minus(nowAzimuth)
        var phi = 0.0
        if((-360<= delta!!)&&(delta<=-180)){
            phi = 360+delta
        }else if ((0<delta)&&(delta<180)){
            phi = delta
        }else if((-180<delta)&&(delta<0)) {
            phi = delta
        }else if((180<=delta)&&(delta<=360)){
            phi = 180 - delta
        }
        right = (-phi/2).toInt()
        left = (phi/2).toInt()
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
        println("チェックするんだえ")
        val lastState = state
        //以下で異常がないかの判定を行うが、else if文で判定しているため、優先度の高い異常から判定するようにすること。
        state = if(isReverse()){//反転しているとき
            println("反転しているんだえ")
            STATE_REVERSE
        }
        else if(!key.isConnectionOK){//Bluetooth接続に問題があるとき
            STATE_BAD
        }
        else{//問題が発生していない時
            STATE_OK
        }
        if(lastState!=state){//状態が変化していた時
            statelistener?.onStateChanged(state)
        }

    }

    private fun isReverse():Boolean{
        if(shell.orientationAngles[1]>1.3962f||abs(shell.orientationAngles[2])>1.5707f){
            return true
        }
        return false
    }
    companion object{
        const val STATE_OK = 100
        const val STATE_REVERSE = 200
        const val STATE_BAD = 400
    }
}