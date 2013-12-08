/*
 * Copyright (C) 2011 SEC (Systems Engineering Consultants) Co.,LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * Hiroaki Matsuda changed the source file.
 * This source file has changed the following points.
 * Added a voice speech and a voice recognition functions.
 * Changed the in and out ports.
 */
package jp.co.sec.rtc.conversation;

import java.util.List;
import java.util.Locale;

import jp.co.sec.rtc.RTMonAndroidProfile;
import jp.co.sec.rtc.RTMonAndroidImpl;
import jp.co.sec.rtm.Logger4RTC;
import jp.co.sec.rtm.NameServerConnectTask;
import jp.co.sec.rtm.NameServerConnectTask.NameServerConnectListener;
import jp.co.sec.rtm.RTCService;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.os.Handler;
import android.os.Message;

/**
 * RTMonAndroid
 */
public class RTMonAndroidConversation extends Activity {
	private static final String TAG = "MyRTC";

	private static final String Pref_NameServerAddress = "NameServerAddress";	// ネームサーバーアドレスをプリファレンスで扱うときのタグ
	private static final int MSG_TEXT_IN  = 0x000;
	private static final int MSG_TEXT_OUT = 0x050;
	private static final int MSG_TOAST	  = 0x100;

	private Context			context;
	private NameServerConnectTask	nameServerConnectTask = null;
	private ServiceConnection	serviceConnection = null;
	private RTCService		rtcService = null;
	private ToastHandler	mToastHandler;

	private EditText		myEditText;			// host:portの入力部分
	private Button			myStartButton;		// START RTC ボタン
	private Button			myStopButton;		// STOP RTC ボタン
	private Button			myRecognitonButton;	// START RECOGNITION ボタン
	private TextView		myInDataView;		// 受信データ表示部分
	private TextView		myOutDataView;		// 送信データ表示部分

	private String			nameServer;			// ネームサーバーアドレス

	private RTMonAndroidImpl rtcImpl;

	private boolean			drawText = false;
	
	// Text to speech
	private static final int REQUEST_CODE = 0;
	TextToSpeech tts = null;
	
	// Speech recognition
	public List<String> results;
	public boolean recogFlag = false;
	public String langage = "ENGLISH";
	public boolean bottonFlag = false;

	/**
	 * アプリ生成
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		Logger4RTC.setDebuggable(context);
		Logger4RTC.debug(TAG, "onCreate Start");

		rtcService = null;
		serviceConnection = null;
		setContentView(R.layout.main);
		initToast();

		myEditText = (EditText)findViewById(R.id.editText);
		myEditText.setText(getNameServerAddress(RTMonAndroidProfile.DefaultNameServer));

		myStartButton = (Button)findViewById(R.id.startButton);
		myStopButton  = (Button)findViewById(R.id.stopButton);

		myInDataView = (TextView)findViewById(R.id.inDataView);
		myOutDataView = (TextView)findViewById(R.id.outDataView);
		myRecognitonButton  = (Button)findViewById(R.id.startRecognition);
		tts = new TextToSpeech(this, null);

		/**
		 *	START RTC ボタン
		 */
		myStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Logger4RTC.debug(TAG, "startRTC Button Pushed");
				if (nameServerConnectTask == null) {
					connectNameServer();		// NameServerへの接続
				}
				else {
					showToast("RTC Service already started !!", 1);
				}
			}
		});
		Logger4RTC.debug(TAG, "0");

		/**
		 *	STOP RTC ボタン
		 */
		myStopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Logger4RTC.debug(TAG, "stopRTC Button Pushed");
				if (rtcService != null) {
					rtcService.stopRTC();
					rtcService = null;
					showToast("RTCService destroyRTC", 1);
				}
				releaseService();
			}
		});
		Logger4RTC.debug(TAG, "1");
		/**
		 * 
		 *  Spinner
		 */
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
		adapter.add("JAPANESE");
        adapter.add("ENGLISH");
        adapter.add("FRANCE");
        adapter.add("CHINESE");
        adapter.add("GERMANY");
        adapter.add("CANADA_FRENCH");
        adapter.add("KOREAN");
        adapter.add("ITALIAN");
        
        Spinner spinner = (Spinner) findViewById(R.id.langageSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                Spinner spinner = (Spinner) parent;
                langage = (String) spinner.getSelectedItem();
                Toast.makeText(getBaseContext(), "Langage: " + langage, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        
        Logger4RTC.debug(TAG, "2");
        /**
		 *	START RECOGNITION ボタン
		 */
        myRecognitonButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Logger4RTC.debug(TAG, "startRecognition Button Pushed");
				bottonFlag = true;
				recognition();
			}
		});
        
        Logger4RTC.debug(TAG, "3");


	}
	
	public void speech(String text){
		if(tts.isSpeaking()) {
            tts.stop();
        }
		
		float pitch = 1.0f; // 音の高低
        float rate = 1.0f; // 話すスピード
		
        if(langage.equals("JAPANESE")){
        	Locale locale = Locale.JAPANESE; // 対象言語のロケール
        	tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        else if(langage.equals("CANADA_FRENCH")){
        	Locale locale = Locale.CANADA_FRENCH;
        	tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    	else if(langage.equals("CHINESE")){
    		Locale locale = Locale.CHINESE;
    		tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	}
    	else if(langage.equals("ENGLISH")){
    		Locale locale = Locale.ENGLISH;
    		tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	}
    	else if(langage.equals("FRENCH")){
    		Locale locale = Locale.FRENCH;
    		tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	}
    	else if(langage.equals("GERMANY")){
    		Locale locale = Locale.GERMANY;
    		tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	}
    	else if(langage.equals("ITALIAN")){
    		Locale locale = Locale.ITALIAN;
    		tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	}
    	else if(langage.equals("KOREAN")){
    		Locale locale = Locale.KOREAN;
    		tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	}
		else{
			Locale locale = Locale.ENGLISH; 
			tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	public void recognition(){
		
		try {
            // "android.speech.action.RECOGNIZE_SPEECH" を引数にインテント作成
			
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            // 「お話しください」の画面で表示される文字列
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech Recognition");
            
            // 音声入力開始
            startActivityForResult(intent, REQUEST_CODE);
        } 
		catch (ActivityNotFoundException e) {
            // 非対応の場合
            Toast.makeText(this, "This terminal doesn't support speech recognition.", Toast.LENGTH_LONG).show();
		}
	}
	
	public List<String> getResults(){
		Logger4RTC.debug(TAG, "results");
		return results;
	}
	
	 @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		 	 
		 // インテントの発行元を限定
		 if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

	     // 音声入力の結果の最上位のみを取得
		 results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);	 
		 
		 Logger4RTC.debug(TAG, ""+ results);
	     Toast.makeText(this, results.get(0), Toast.LENGTH_LONG).show();

	     recogFlag = true;
	     
	     super.onActivityResult(requestCode, resultCode, data);
	     
		 }
	 }

	/**
	 * アプリ起動
	 */
	@Override
	public void onStart() {
		super.onStart();
		Logger4RTC.debug(TAG, "onStart");
	}

	/**
	 * アプリ開始
	 */
	@Override
	public void onResume() {
		super.onResume();
		drawText = true;
		Logger4RTC.debug(TAG, "onResume");
	}

	/**
	 * アプリ停止
	 */
	@Override
	public void onPause() {
		drawText = false;
		super.onPause();
		Logger4RTC.debug(TAG, "onPause");
	}

	/**
	 * アプリ破棄
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger4RTC.debug(TAG, "onDestroy");
		if (rtcService != null) {
			rtcService.stopRTC();
			rtcService = null;
			showToast("RTCService destroyRTC", 1);
		}
		releaseService();
	}

	/**
	 * NameServerへの接続
	 */
	private void connectNameServer() {
		Logger4RTC.debug(TAG, "connectNameServer");
		SpannableStringBuilder sb = (SpannableStringBuilder)myEditText.getText();
		nameServer = sb.toString();
		saveNameServerAddress(nameServer);		// この時点でのネームサーバーアドレスを保存しておく

		nameServerConnectTask = new NameServerConnectTask(this);
		nameServerConnectTask.setListener(new NameServerConnectListenerImpl());	// NameServer接続完了リスナーを登録
		nameServerConnectTask.setIpAddress(nameServer);
		nameServerConnectTask.setRetryCount(5);
		nameServerConnectTask.execute();		// ネームサーバーへの接続を開始する
	}

	/**
	 * NameServer接続完了リスナー
	 */
	private class NameServerConnectListenerImpl implements NameServerConnectListener {
		/**
		 * RTCサービスを開始
		 */
		public void onConnected() {
			Intent intent = new Intent(RTMonAndroidConversation.this, RTCService.class);
			startService(intent);
			serviceConnection = new RtcServiceConnection();
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}

		/**
		 * 接続失敗
		 */
		public void onConnectFailed() {
		}

		/**
		 * 接続キャンセル
		 */
		public void onConnectCanceled() {
		}
	}

	/**
	 * RTCService接続処理
	 */
	private class RtcServiceConnection implements ServiceConnection, TextToSpeech.OnInitListener{
		/**
		 * RTCService bind完了
		 */
		public void onServiceConnected(ComponentName className, IBinder service) {
			Logger4RTC.debug(TAG, "RTCService binded");
			rtcService = ((RTCService.RTCServiceBinder) service).getService();

			// プロパティーを設定する
			rtcService.setProfiles(
					RTMonAndroidProfile.DefaultNameServer,	RTMonAndroidProfile.Name,
					RTMonAndroidProfile.ImplementationId,	RTMonAndroidProfile.Type,
					RTMonAndroidProfile.Description,		RTMonAndroidProfile.Version,
					RTMonAndroidProfile.Vendor,				RTMonAndroidProfile.Category,
					String.valueOf(RTMonAndroidProfile.execute_rate));

			rtcService.setLongConfig(RTMonAndroidProfile.ConfigName1, 0);
			rtcImpl = new RTMonAndroidImpl(RTMonAndroidConversation.this, rtcService);

			SpannableStringBuilder sb = (SpannableStringBuilder)myEditText.getText();	// EditTextに入力された文字
			rtcService.startRTC(sb.toString(), context.getPackageName());		// START RTC
			showToast("start RTC : " + nameServer, 1);
		}

		/**
		 * RTCService終了処理
		 */
		public void onServiceDisconnected(ComponentName className) {
			Logger4RTC.debug(TAG, "onServiceDisconnected");
		}

		@Override
		public void onInit(int status) {
			// TODO Auto-generated method stub
	        if(status == TextToSpeech.SUCCESS) {
	            // 音声合成の設定を行う

	            float pitch = 1.0f; // 音の高低
	            float rate = 1.0f; // 話すスピード
	            
	            Locale locale = Locale.JAPANESE; // 対象言語のロケール
            	tts.setPitch(pitch);
	            tts.setSpeechRate(rate);
	            tts.setLanguage(locale);
	        }
			
		} 
		
	}

	/**
	 * RTCServiceとの結合を解除
	 */
	private void releaseService() {
		if (nameServerConnectTask != null) {
			nameServerConnectTask.cancel(true);
			nameServerConnectTask = null;
		}
		if (serviceConnection != null) {
			unbindService(serviceConnection);
			serviceConnection = null;
			Intent intent = new Intent(RTMonAndroidConversation.this, RTCService.class);
			stopService(intent);
		}

		if (rtcImpl != null){
			rtcImpl = null;
		}
	}

	/**
	 * 受信内容を画面書き込む
	 */
	public void textDrawIn(String str) {
		Logger4RTC.debug(TAG, "receiverDraw : " + str);
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TEXT_IN, str));
	}
	
	/**
	 * 送信内容を画面書き込む
	 */
	public void textDrawOut(String str) {
		Logger4RTC.debug(TAG, "sendDraw : " + str);
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TEXT_OUT, str));
	}
	
	
	/**
	 * トースト表示のハンドラで表示の為の初期化
	 */
	private void initToast(){
		mToastHandler = new ToastHandler();
	}

	/**
	 * トースト表示のリクエスト
	 * @param msg 表示する文字
	 */
	public void showToast(String msg){
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TOAST, msg));
	}

	/**
	 * トースト表示のリクエスト
	 * @param msg 表示する文字
	 * @param mode 0=long, 1=short
	 */
	public void showToast(String msg, int mode){
		int lng = Toast.LENGTH_LONG;
		if (0 == mode) lng = Toast.LENGTH_SHORT;
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TOAST+lng, msg));
	}

	/**
	 * TEXTとトーストの表示
	 */
	private class ToastHandler extends Handler{
		public void handleMessage(Message msg) {
			int para = msg.what;
			Logger4RTC.debug(TAG, "what : " + para);
			
			switch(para){
			case MSG_TOAST:
				Toast toast = Toast.makeText(getBaseContext(), msg.obj.toString(), msg.what);
				toast.show();
				break;
				
			case MSG_TEXT_IN:
				if (drawText){
					myInDataView.setText(msg.obj.toString());	// TEXT描画
				}
				break;
				
			case MSG_TEXT_OUT:
				if (drawText){
					myOutDataView.setText(msg.obj.toString());
				}
				break;
			}
			
		}
	}

	/**
	 * ネームサーバーのアドレスを得る
	 * @param default_name_server デフォルトネームサーバー
	 * @return ネームサーバーのアドレス
	 */
	private String getNameServerAddress(String default_name_server){
		String nameServer = default_name_server;
		try{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			nameServer = sp.getString(Pref_NameServerAddress, nameServer);
		}
		catch (Exception e) {
		}
		return nameServer;
	}

	/**
	 * ネームサーバーのアドレスをプリファレンスに保存する
	 * @param nameServerAddress ネームサーバーアドレス
	 * @return 成功時 true
	 */
	private boolean saveNameServerAddress(String nameServerAddress){
		try{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Pref_NameServerAddress, nameServerAddress);
			editor.commit();
		}
		catch (Exception e) {
			Logger4RTC.error(TAG, "error Preference Write");
			return false;
		}
		return true;
	}
	
}
