Android Conversation RTC
======================
Android端末に標準で搭載されている音声合成・音声認識機能を利用するRTCです．  
多言語の音声出力と音声認識に対応しています．  
RTM on Androidを利用したRTCで，パッケージをインストールするだけで使用できます．  

本RTCはサービスロボットの対話システムに使用することを前提として設計しています．  

  **音声合成対応言語:**  
JAPANESE  
ENGLISH  
FRANCE  
CHINESE  
GERMANY  
CANADA_FRENCH  
KOREAN  
ITALIAN  

動作環境
------
**Android OS:**  
2.3.3以上    

**依存ファイル:**    
[SpeechSynthesis Data Installer][sp]
[sp]:https://play.google.com/store/apps/details?id=com.svox.langpack.installer&hl=ja  
使用する端末に音声合成用のデータがない場合は，上記のアプリをインストールしてください． 

ファイル構成
------
AndroidConversation  
│―AndroidAPP  
│　　　　│―.settings  
│　　　　│―assets    
│　　　　│―bin  
│　　　　│―doc  
│　　　　│―gen  
│　　　　│―libs  
│　　　　│―res  
│　　　　│―src  
│　　　　│　　│―jp  
│　　　　│ 　　　　│―co  
│　　　　│　　　　　　　│―sec  
│　　　　│　　　　　　　　  　│―  rtc  
│　　　　│　　　　　　　　　　　　　　│― conversation  
│　　　　│　　　　　　　　　　　　　　│　　　　　│―RTMonAndroidConversation.java  
│　　　　│　　　　　　　　　　　　　　│  
│　　　　│　　　　　　　　　　　　　　│― RTMonAndroidImpl.java  
│　　　　│　　　　　　　　　　　　　　│― RTMonAndroidProfile.java  
│　　　　│  
│　　　　│―.classpath     
│　　　　│―.project      
│　　　　│―AndroidManifest.xml    
│　　　　│―proguard.cfg  
│　　　　│―project.properties  
│  
│―README.md  
│―LICENSE.txt  
│―AndroidConversation.apk  
│  
│―TestRTC  
　　　　　│― PyStringConsoleOut  
　　　　　│― PyStringConsoleIn   
　　　　　│― PyBooleanConsoleIn     

一部のファイルについて説明します．  

* AndroidConversation.apk  
Android端末にインストールするためのAPKファイルです．  
ただ使用するだけであるならこのファイルを使用して端末にインストールしてください．   

* RTMonAndroidConversation.java  
AndroidアプリのUIについて記述されています．  
UIを変更する場合はここを編集してください．  

* RTMonAndroidImpl.java  
ポート入出力操作について記述されています．   
  
* RTMonAndroidProfile.java  
ポートの命名，プロファイルの設定などを行っています．     

* LICENSE.txt  
本RTCはSEC Co.,LTD.のRTM on Androidを使用して作成しています．  
そのライセンス表記とHiroaki Matsudaによる変更点を記述していますので削除しないでください．  
本RTCを変更した場合にはこのライセンスファイルに変更点を追記してください．  

* TestRTC  
本RTCの動作確認するためのTest用RTCが収められています．  
動作には[OpenRTM-aist Python 1.1.0-RC1][py]が必要です．    
[py]:http://openrtm.org/openrtm/ja/node/4526

RTCの構成
------  
<img src="http://farm4.staticflickr.com/3742/11293451083_c9014ca1e7_o.png" width="400px" />    
データポートは3つあり、以下のようになっています  
  
* enable port :InPort  
データ型; TimedBoolean   
・  True: 音声認識画面が起動します．  
・  False: 何も起きません．  

* speech port :InPort  
データ型; TimedString  
 ・  String :  受信したテキストデータを読み上げます．  
後述するGUIから設定した言語で音声を出力します．  
  
* recognition port :OutPort  
データ型; TimedString  
 ・  String :  音声認識の最も順位の高い識別結果を出力します．

GUIの構成
------  
Android端末上のGUIの操作方法を説明します．

<img src="http://farm8.staticflickr.com/7432/11268937874_827bf75fa3_o.png" width="250px" />  

* 1　ネームサーバーのアドレスフォーム  
ネームサーバーのIPアドレスとポート番号を記入します．

* 2　RTCの起動  
ボタンを押すとRTCが起動します．   

* 3 RTCの終了
ボタンを押すとRTCが終了します．  

* 4　InPortの受信データ確認フォーム  
InPortに入力されたデータを確認できます．  
・Enableタグ  
enableポートから入力されたデータ．  
・Speechタグ  
speechポートから入力されたデータ．

* 5　OutPortの送信データ確認フォーム  
OutPortから出力したデータを確認できます．

* 6 発話言語の選択スピナー  
発話する音声データの言語を選択できます．

* 7 音声認識の起動ボタン  
ボタンを押すと音声認識画面が起動します．
  
使い方
------
###1. AndroidConversation.apkのインストール###
AndroidConversation.apkを端末に移し，インストールして下さい． 

###2. SpeechSynthesis Data Installerのインストール###
本RTCは発話に音声データが必要です．  
端末に音声データがない場合は，[SpeechSynthesis Data Installer][sp]をGoogle Playからイントールしてください．  

###3. 端末の言語の設定###
音声認識にしようする言語は端末の言語と同じ言語となります．  
音声認識する言語にあわせて端末の言語設定を変更してください．  
一般的には 設定>言語と入力>言語から変更できます．  
 
###4. テスト用RTCの実行###
1. ネームサーバーを起動します．  
Windows:  
Start Naming Serviceで起動します．  
Linux:  
以下のコマンドで起動します．2809はポート番号で任意で選んで構いません．  
$ rtm-naming 2809

2. PyStringConsoleOut.pyを起動します．  
Windows:  
ダブルクリックで起動します．  
Linux:  
以下のコマンドで起動します．  
$ python PyStringConsoleOut.py  

3. PyStringConsoleIn.pyを起動します．  
Windows:  
ダブルクリックで起動します．  
Linux:  
以下のコマンドで起動します．  
$ python PyStringConsoleIn.py  

4. PyBooleanConsoleIn.pyを起動します．  
Windows:  
ダブルクリックで起動します．  
Linux:  
以下のコマンドで起動します．  
$ python PyBooleanConsoleIn.py  

###5. Conversation RTCの実行###
1. ネームサーバーを入力します．  
ネームサーバーのアドレスフォームにネームサーバーが起動しているPCのIPアドレスとネームサーバーのポート番号を入力します．  

2. Conversation RTCを起動します．  
START RTCボタンを押して，Conversation RTCを起動します．  

3. RTCを接続します．  
RT System Editorを使用して，各RTCを以下のように接続しActivateします．  

<img src="http://farm4.staticflickr.com/3675/11268316856_26fc447690_o.png" width="450px" />  

PyBooleanConsoleIn:  
1を入力するとAndroid端末上で音声認識が起動します．  

PyStringConsoleOut:   
音声認識の結果が表示されます．  

PyStringConsoleIn:  
テキストを入力するとAndroid端末から合成された音声が出力されます．  

以上が本RTCの使い方となります  

LICENSE
----------
・**AndroidConversation RTC**

 Copyright (C) 2011 SEC (Systems Engineering Consultants) Co.,LTD.
 
Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and limitations under the License.  
 
 Hiroaki Matsuda changed the source file.  
 This source files were changed the following points.  
・Added a voice speech and a voice recognition functions.  
・Changed the input and output ports.  


・**TEST RTCs**  
PyBooleanConsoleIn    
PyStringConsoleOut  
PyStringConsoleIn  

Copyright © 2013 Hiroaki Matsuda
Licensed under the [Apache License, Version 2.0][Apache].  
Distributed under the [MIT License][MIT].  
Dual licensed under the [MIT License][MIT] and [Apache License, Version 2.0][Apache].   
 
[Apache]: http://www.apache.org/licenses/LICENSE-2.0
[MIT]: http://www.opensource.org/licenses/mit-license.php