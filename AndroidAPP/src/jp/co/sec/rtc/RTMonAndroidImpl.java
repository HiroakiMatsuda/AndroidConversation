package jp.co.sec.rtc;

import java.io.IOException;
import jp.co.sec.rtc.conversation.*;
import jp.co.sec.rtm.*;

/**
 *
 */
public class RTMonAndroidImpl extends RTCBase {
	private static final String TAG = "MyRTC";

	private static RTMonAndroidConversation	myRTC = null;
	private RTCService					rtcService;
	

	// InPort
	private InPort<TimedBoolean>		inPortCommand;
	private InPort<TimedString>			inPortSpeech;

	// OutPort
	private OutPort<TimedString>		outPortRecognition;
	
	// Recognition
	private boolean portFlag = false;
	

	/**
	 * コンストラクタ
	 *
	 * @param myRTC
	 * @param rtcService
	 * @param mode データ型
	 */
	public RTMonAndroidImpl(RTMonAndroidConversation myRTC, RTCService rtcService) {
		Logger4RTC.debug(TAG, "RTMonAndroidImpl");
		RTMonAndroidImpl.myRTC = myRTC;
		this.rtcService = rtcService;
		rtcService.setRTC(this);

		initInPort();
		initOutPort();
	}

	/**
	 * 初期化処理.コンポーネントライフサイクルの開始時に一度だけ呼ばれる.
	 */
	@Override
	public int onInitialize() {
		Logger4RTC.debug(TAG, "onInitialize");
		myRTC.showToast("onInitialize");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * 非アクティブ状態からアクティブ化されるとき1度だけ呼ばれる.
	 */
	@Override
	public int onActivated() {
		Logger4RTC.debug(TAG, "onActivated");
		myRTC.showToast("onActivate");
		

		
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * アクティブ状態時に周期的に呼ばれる.
	 */
	@Override
	public int onExecute() {
		ioControl();
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * アクティブ状態から非アクティブ化されるとき1度だけ呼ばれる.
	 */
	@Override
	public int onDeactivated() {
		Logger4RTC.debug(TAG, "onDeactivated");
		myRTC.showToast("onDectivate");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * エラー状態に入る前に1度だけ呼ばれる.
	 */
	@Override
	public int onAborting() {
		Logger4RTC.debug(TAG, "onAborting");
		myRTC.showToast("onAborting");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * エラー状態からリセットされ非アクティブ状態に移行するときに1度だけ呼ばれる.
	 */
	@Override
	public int onReset() {
		Logger4RTC.debug(TAG, "onReset");
		myRTC.showToast("onReset");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * エラー状態にいる間周期的に呼ばれる.
	 */
	@Override
	public int onError() {
		Logger4RTC.debug(TAG, "onError");
		myRTC.showToast("onError");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * コンポーネントライフサイクルの終了時に1度だけ呼ばれる.
	 */
	@Override
	public int onFinalize() {
		Logger4RTC.debug(TAG, "onFinalize");
		myRTC.showToast("onFinalize");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * InPort初期化
	 */
	private void initInPort(){
		String pName1 = RTMonAndroidProfile.InPort1;
		String pName2 = RTMonAndroidProfile.InPort2;
		TimedBoolean tmb = new TimedBoolean();
		TimedString tms = new TimedString();
		inPortCommand = new InPort<TimedBoolean>(pName1, tmb);
		inPortSpeech = new InPort<TimedString>(pName2, tms);
		rtcService.addInPort(inPortCommand);
		rtcService.addInPort(inPortSpeech);
	}

	/**
	 * OutPort初期化
	 */
	private void initOutPort(){
		String pName = RTMonAndroidProfile.OutPort1;
		TimedString tmls = new TimedString();
		outPortRecognition = new OutPort<TimedString>(pName, tmls);
		rtcService.addOutPort(outPortRecognition);
	}

	/**
	 * 入出力コントロール
	 */
	private void ioControl(){
		Logger4RTC.spec("SPEC_LOG", "RTM_11j: onExecute in Application Started.");
		
		try{
			
			// Recognition
			if (inPortCommand.isNew()){						
				TimedBoolean tmb = inPortCommand.read();   
				boolean enable = tmb.getData();
				myRTC.textDrawIn("Enable:" + enable);	
				
				if(enable == true){
					portFlag = true;
					myRTC.recognition();
				}					
			}
			
			// Speech
			if (inPortSpeech.isNew()){
				TimedString tms = inPortSpeech.read();     
				String text = tms.getData();
				
				myRTC.textDrawIn("Speech:" + text);						
				myRTC.speech(text);	
			}
			
			// Write Recognition results
			if (myRTC.recogFlag == true && portFlag == true){
				TimedString tmss = new TimedString(new RTCTime(0, 0), myRTC.results.get(0));
				
				myRTC.textDrawOut("Recognition: " + tmss.getData());
				outPortRecognition.write(tmss);

				Logger4RTC.debug(TAG, "onExecute value=" + tmss.getData());
				
				portFlag = false;
				myRTC.recogFlag = false;
			}
			
			if (myRTC.recogFlag == true && myRTC.bottonFlag == true){
				TimedString tmss = new TimedString(new RTCTime(0, 0), myRTC.results.get(0));
				
				myRTC.textDrawOut("Recognition: " + tmss.getData());
				outPortRecognition.write(tmss);

				Logger4RTC.debug(TAG, "onExecute value=" + tmss.getData());
				
				myRTC.bottonFlag = false;
				myRTC.recogFlag = false;
			}
		}
		catch (IOException e){					// InPortの読み出しでExceptionが発生する可能性があるので、ここでキャッチ
		}

	}

}
