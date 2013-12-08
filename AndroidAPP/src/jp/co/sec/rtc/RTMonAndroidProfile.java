package jp.co.sec.rtc;

/**
 * コンポーネントのコンフィグレーションを定義
 */
public class RTMonAndroidProfile {
	public static final String DefaultNameServer= "192.168.0.14:2809";		// host[:port]
	public static final String Name				= "conv";
	public static final String ImplementationId	= "Conversation";
	public static final String Type				= "Conversation";
	public static final String Description		= "Conversation";
	public static final String Version			= "1.0";
	public static final String Vendor			= "Hiroaki Matsuda";
	public static final String Category			= "Android";

	public static final float execute_rate		= 5F; // 秒間何回処理をするか (wait = 1.0 / execute_rate * 1000000 usec)

	//
	// コンポーネントのコンフィグレーションを定義
	//
	public static final String ConfigName1		= "S_multiple_coefficient";
	public static final String ConfigName2		= "S_float_sample";
	public static final String ConfigName3		= "S_string_sample_1";
	public static final String ConfigName4		= "S_string_sample_2";

	//
	// コンポーネントのデータポートを定義
	//
	// << in port >>
	public static final String InPort1			= "enable";
	public static final String InPort2			= "speech";

	// << out port >>
	public static final String OutPort1			= "recognition";
}
