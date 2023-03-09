# ConidaeTanegashima
第19回種子島ロケットコンテストのCansat部門ミッション種目にて優勝したチームConidaeのプログラムです。

## Contents
* KotlinDae2nd
* KotlinDae
* Conidae_ESP32
* Conidae_ESP32_with_melt
## 解説
### KotlinDae2nd
#### 機能
スマホで動作するアプリで、自立走行、サーバー駆動、オペレータの3つのモードがあります。  
自立走行の時は、後述するConidae_ESP_32_with_meltを書き込んだESP32とペアリングして探査機として動作します。  
サーバー駆動の時は、Conidae_ESP_32を書き込んだESP32とペアリングして動作し、さらにサーバーとの接続を行いながらオペレサーバーの指示に従い動作します。  
オペレータの時は、主にサーバーとの通信を行って
#### 操作方法
初期画面：  
初期の画面ではモードの選択とユーザー名の書き込みができ、モード選択は画面中央のプルダウンから行えます。また、サーバー駆動とオペレータに関しては、機体の識別子としてユーザー名を登録する必要があるので、5文字以上の英数字でユーザー名を登録してスタートしてください。自立走行の場合、ユーザー名は必要ありません。

自立走行：  
１．あらかじめ、Conidae_ESP32_with_meltを書き込んだESP32をBluetoothでペアリングしておきます。  
２．モードはデフォルトで自立走行モードが選択されているので、そのままSTARTを押します。  
３．Select Bluetooth Deviceのプルダウンで、ペアリングしたESPの名前を選択します。  
４．Latitude,Longitudeと書かれた部分にゴール地点の緯度経度を入力しSTARTを押して始めます。(ゴールを手動で設定するとき)  
４．SELECT BY MAPを押します(以下、ゴールをマップ上から指定するとき)  
５.ゴール地点としたい位置を地図上で長押しして、接地したマーカーをタップします。  
６．"Do you want to set here as Goal?"というダイアログが出るので、SETを押すと始まります。

サーバー駆動：  
１．あらかじめ、Conidae_ESP32を書き込んだESP32をBluetoothでペアリングしておきます。  
２．ユーザー名を英数字5文字以上で入力します。  
３．モード選択で、サーバー誘導を選択し、STARTを押します。  
４．Select Bluetooth Deviceのプルダウンで、ペアリングしたESPの名前を選択します。  
５．STARTを押すと開始し、サーバーと通信して駆動を開始します。
６．戻るボタンまたはジェスチャーを行うことでログアウトします。
オペレータ：  
１．ユーザー名を英数字5文字以上で入力します。  
２．モード選択で、オペレータを選択し、STARTを押します。  
３．地図上でダブルタップすると、フィールド上の状態が更新されます。なお、自動で更新はされないので、最新情報を知りたい場合は毎回ダブルタップをする必要があります。  
４．地図を長押しするとマーカーの設置ができ、それをタップして向かわせたい探査機のユーザー名をタップするとゴール地点を設定することができます。  
５．戻るボタンまたはジェスチャーを行うことでログアウトします。
オペレータ画面の補足:  
操作者→太陽の塔みたいなアイコン  
赤のとき接続タイムアウト(1分間更新していない)  
青の時ログアウト済み  
紫の時オンライン  
探査機→目玉みたいなアイコン  
赤の時接続タイムアウト  
青の時ログアウト済み  
緑のときオンライン  
### KotlinDae
#### 機能
サーバー機能などを実装しておらず、純粋に探査機としての機能を実装したアプリになっています。すこしバージョンが古いので技術資料とは少々異なる内容になってはいますが、書かれている内容が単純な分理解はしやすいかと思います。
### Conidae_ESP32
サーバー駆動用の機体のESP32のプログラムです。
#### 機能
これを書き込んだESP32はBluetoothを介してスマホからの信号をモータに伝える役割があります。  
信号を送るときは、  
**"左モータの出力,右モータの出力;"**  
の形でデータを送信します。出力値は0から180までの値で、モータは90で静止し、これより大きくなるにつれて時計回りに速く、小さくなるに連れて反時計回り速く回転します。  
スマホ側から表示されるデバイス名を変更するときはプログラム内のSerialBt.Begin()の引数を書き換えます。

### Conidae_ESP32_with_melt
自立走行用の機体のESP32のプログラムです。
#### 機能
機能としてはConidae_ESP32に溶断用のプログラムを追加したもので、電源が接続されてから、最初に  
**"52120"**  
というデータが送られると溶断を開始し、その後はConidae_ESP32と全く同じ動作をします。  
## サーバー・データベースのURL
[サーバー](https://docs.google.com/document/d/1RpMjHWsSF7Ac74QMa1fZhfSOrmiUf2NCvFHljLfUnbI/edit?usp=sharing)←こちらはサーバー側のコードのテキストデータとなりますので、これをGoogle Apps Scriptにコピペしてください。   
[データベース](https://docs.google.com/spreadsheets/d/1S9cln5S1FHNmxY2MARpbRo55B0uS7WgbZ1usYiUxpLU/edit?usp=sharing)←こちらは閲覧専用なので、ご自身のドライブにコピーをお願いします。
