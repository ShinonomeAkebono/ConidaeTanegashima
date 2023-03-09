
package com.example.kotlindae2nd

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindae2nd.databinding.ActivityInductionConidaeFaceBinding

class InductionConidaeFaceActivity : AppCompatActivity(),InductionKonidae.StateListener {
    private lateinit var binding:ActivityInductionConidaeFaceBinding
    private lateinit var conidae:InductionKonidae
    private lateinit var key:BluetoothKommunication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInductionConidaeFaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Toast.makeText(this,
            "${intent.getStringExtra("DEVICE_NAME")},"+"${intent.getDoubleExtra("GOAL_LAT",0.0)},"+ "${intent.getDoubleExtra("GOAL_LONG",0.0)}",
            Toast.LENGTH_SHORT).show()
        key = BluetoothKommunication(intent.getStringExtra("DEVICE_NAME")!!,this)
        conidae = InductionKonidae(key,this)
        conidae.statelistener = this
        conidae.landAndGo(intent.getDoubleExtra("GOAL_LAT",0.0),intent.getDoubleExtra("GOAL_LONG",0.0))
    }

    override fun onDestroy() {
        super.onDestroy()
        conidae.finish()
    }
    //それぞれの表情について，changeFace()を呼び出す．
    private fun fightFace(){
        changeFace(R.drawable.fightleft,R.drawable.fightright,R.drawable.fight)
    }
    private fun normalFace(){
        changeFace(R.drawable.nomalleft,R.drawable.nomalright,R.drawable.smile)
    }
    private fun troubleFace(){
        changeFace(R.drawable.troubleleft,R.drawable.troubleright,R.drawable.fight)
    }
    private fun smileFace(){
        changeFace(R.drawable.smileright,R.drawable.smileleft,R.drawable.smile)
    }
    private fun nothingFace(){
        changeFace(R.drawable.nothingleft,R.drawable.nothingright,R.drawable.fight)
    }
    private fun surpriseFace(){
        changeFace(R.drawable.nomalleft,R.drawable.nomalright,R.drawable.surprise)
    }
    private fun changeFace(left:Int, right:Int, mouth:Int){
        val rightEye = binding.imgInductRightEye
        val leftEye = binding.imgInductLeftEye
        val mMouth = binding.imgInductMouth
        runOnUiThread(kotlinx.coroutines.Runnable {
            rightEye.setImageResource(right)
            leftEye.setImageResource(left)
            mMouth.setImageResource(mouth)
        })
    }

    override fun onStateChanged(state: Int){
        if(state.and(InductionKonidae.STATE_PRESSUREOK)==0){//高度が十分下がっていない時
            fightFace()
        }
        else if(state.and(InductionKonidae.STATE_REVERSE)!=0){//反転しているとき
            nothingFace()
        }
        else if(state.and(InductionKonidae.STATE_CONNECTION_ERR)!=0){//Bluetoothエラーのとき
            troubleFace()
        }
        else{
            normalFace()
        }
    }
}