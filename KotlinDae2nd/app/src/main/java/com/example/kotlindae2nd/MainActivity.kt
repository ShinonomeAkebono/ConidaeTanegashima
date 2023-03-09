package com.example.kotlindae2nd

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindae2nd.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initSelectModeSpinner()
        binding.btnStartOnMain.setOnClickListener {
            when(binding.spnSelect.selectedItem.toString()){
                MODE_INDUCTION->{
                    val nextActivityIntent = Intent(this,InductionConidaeSettingActivity::class.java)
                    startActivity(nextActivityIntent)
                }
                MODE_SERVER->{
                    //サーバー誘導モードの時の処理
                    if(binding.editUserName.text.toString()=="NAME"){
                        Toast.makeText(this, "ユーザー名を入力してください。", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    else if(binding.editUserName.text.toString().length<5){
                        Toast.makeText(this, "名前は5文字以上にしてください", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val serverModeIntent = Intent(this,ServerModeConidaeSettingActivity::class.java)
                        serverModeIntent.putExtra("USERNAME",binding.editUserName.text.toString())
                        startActivity(serverModeIntent)
                    }
                }
                MODE_OPERATOR->{
                    //オペレータモードの時の処理
                    if(binding.editUserName.text.toString()=="NAME"){
                        Toast.makeText(this, "ユーザー名を入力してください。", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    else if(binding.editUserName.text.toString().length<5){
                        Toast.makeText(this, "名前は5文字以上にしてください", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, binding.editUserName.text, Toast.LENGTH_SHORT).show()
                        val operatorModeIntent = Intent(this,CommanderMapActivity::class.java)
                        operatorModeIntent.putExtra("USERNAME",binding.editUserName.text.toString())
                        startActivity(operatorModeIntent)
                    }
                }
            }
        }
    }
    private fun initSelectModeSpinner() {
        val selectModeSpinner = binding.spnSelect
        val mConnectDeviceAdapter = ArrayAdapter<String>(this, R.layout.simple_spinner_item)
        val modeOptions = listOf(MODE_INDUCTION, MODE_SERVER, MODE_OPERATOR)
        for (option in modeOptions) {
            mConnectDeviceAdapter.add(option)
        }
        selectModeSpinner.adapter = mConnectDeviceAdapter
    }
    companion object{
        const val MODE_INDUCTION = "自立走行"
        const val MODE_SERVER = "サーバー誘導"
        const val MODE_OPERATOR = "オペレータ"
    }
}