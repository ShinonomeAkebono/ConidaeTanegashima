#include "BluetoothSerial.h"
#include <ESP32Servo.h>

BluetoothSerial SerialBT;
Servo rightservo;//create servo object to control a servo
Servo leftservo;
int rightPin = 5;
int leftPin = 18;
int val;
int val_ipt;
void setup() {
  //BTの処理
  Serial.begin(115200);
  //↓bluetooth探索したときに出てくる名前
  SerialBT.begin("Shell");

  //Servo用の処理
  ESP32PWM::allocateTimer(0);
  ESP32PWM::allocateTimer(1);
  ESP32PWM::allocateTimer(2);
  ESP32PWM::allocateTimer(3);
  rightservo.setPeriodHertz(50);
  leftservo.setPeriodHertz(50);
  rightservo.attach(rightPin,700,2300);
  leftservo.attach(leftPin,700,2300);
  
  val =90;
}

void loop() {
  //Bluetooth経由でのデータ取得
  if (SerialBT.available()){
    String val_ipt = SerialBT.readStringUntil(';');
    //文字列解析のためにchar型に変換
    char charBuf[50];
    char *value = NULL;
    val_ipt.toCharArray(charBuf,50);
    value = strtok(charBuf,",");
    String rightValue = value;
    int right= rightValue.toInt();
    value = strtok(NULL,",");
    String leftValue = value;
    int left= leftValue.toInt();
    Serial.print(right);
    Serial.print(left);
    if(right<0|180<right){
      Serial.print("invalid right input\n");
    }
    else if(left<0|180<left){
      Serial.print("invalid left input\n");
    }
    else {
      Serial.print("setR:");
      Serial.print(right);
      rightservo.write(right);
      Serial.print("L:");
      Serial.print(left);
      leftservo.write(left);
      Serial.print("\n");
    }
 }

 delay(20);
}
