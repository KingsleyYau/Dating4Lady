package com.qpidnetwork.ladydating;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.ladydating.album.VideoUploadManager;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.chat.contact.ContactManager;
import com.qpidnetwork.ladydating.chat.translate.TranslateManager;
import com.qpidnetwork.ladydating.googleanalytics.AnalyticsManager;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MultiLanguageManager;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.ConfigManagerJni;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.tool.CrashHandler;
import com.qpidnetwork.tool.CrashHandlerJni;
import com.qpidnetwork.tool.ImageHandler;

public class QpidApplication extends Application implements IAuthorizationCallBack {
	
	private static Context process;
	public static boolean isDemo = true;
	public static boolean isDebug = true;
	public static int versionCode = 1;
	public static String versionName = "";
	public static String DEVICE_TYPE = "30"; 
//	private LiveChatManagerTest liveChatTest = new LiveChatManagerTest();
	
	public static boolean updataFavIfNeed = false;
	public static boolean updateAlbumsNeed = false;
	
	//存放被踢时标志位及被踢时间，用于判断再次进入是否弹出被T提示
	public static boolean isKickOff = false;
	public static int lastestKickoffTime = 0;
	public static KickOfflineType kickOffType = KickOfflineType.Unknow;
	
	@Override
	public void onCreate() {
		super.onCreate();
		process = this;
		
		// 获取是否demo标志
		isDemo = getResources().getBoolean(R.bool.is_demo);
		// 判断是否测试（是，则打log）
		try {
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			isDebug = pInfo.versionName.matches(".*[a-zA-Z].*");
		} catch (NameNotFoundException e1) {
		}
		
		// 设置log级别（demo环境才打印log）
		if (isDebug) {
			Log.SetLevel(android.util.Log.DEBUG);
		}
		else {
			Log.SetLevel(android.util.Log.ERROR);
		}
		
		// 初始化GA管理器
		if (isDebug || isDemo) {
			AnalyticsManager.newInstance().init(this, R.xml.tracker_demo, getString(R.string.web_short_name));
		}
		else {
			AnalyticsManager.newInstance().init(this, R.xml.tracker, getString(R.string.web_short_name));
		}
		
	   	//deviceType
	   	DisplayMetrics dm = SystemUtil.getDisplayMetrics(this);
	   	double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		double screenSize = diagonalPixels / (160 * dm.density); // 求出屏幕尺寸（不是很精确）
		DEVICE_TYPE = screenSize >= 6 ? "34" : "30";
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QpidDatingLady/" + getResources().getString(R.string.app_name);
		
		FileCacheManager.newInstance(this);
		FileCacheManager.getInstance().ChangeMainPath(path);
		
		//初始化站基本配置
		WebsiteManager.newInstance(this);
		
		// crash日志管理器
		CrashHandler.newInstance(this);
		// Jni错误捕捉
		CrashHandlerJni.SetCrashLogDirectory(FileCacheManager.getInstance().GetCrashInfoPath());
		CrashHandler.getInstance().SaveAppVersionFile();
		ImageHandler.SetLogPath(FileCacheManager.getInstance().GetLogPath());
		
		RequestJni.SetLogDirectory(FileCacheManager.getInstance().GetLogPath());
		RequestJni.SetWebSite(WebsiteManager.getInstance().mWebSite.webSiteHost, WebsiteManager.getInstance().mWebSite.webHost);
		RequestJni.SetCookiesDirectory(path);
		// 设置demo请求环境
		if( isDemo ) {
			RequestJni.SetAuthorization("test", "5179");
		}
		// 设置客户端版本号
	   	try {
	   		PackageManager pm = getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);  
	        if (pi != null) {
	        	// 版本号
	        	versionCode = pi.versionCode;
	        	versionName = pi.versionName;
	        	RequestJni.SetVersionCode(String.valueOf(versionCode));
	        	
	        	// 设备Id
	    		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	    		RequestJni.SetDeviceId(tm);
	        }  
	    } catch (NameNotFoundException e) {  
	    } 
		
		// 初始化LiveChatManager
		LiveChatManager lcMgr = LiveChatManager.newInstance(this);
		ConfigManagerJni.AddCallback(lcMgr);	// 添加 LiveChatManager 获取同步配置
//		liveChatTest.Init();
		
		//初始化联系人管理
		ContactManager contactManager = ContactManager.newInstance(this);
		
		//初始化翻译管理模块
		TranslateManager transManager = TranslateManager.newInstance(this);
		
		// 初始化登录管理器
		LoginManager lm = LoginManager.newInstance(this);
		lm.AddListenner(lcMgr);	// 添加LiveChatManager Listener
		lm.AddListenner(transManager);//transManager添加监听
		lm.AddListenner(contactManager);;//联系人监听登陆注销
		
		//初始化默认语言设置
		new MultiLanguageManager(this).initLocalLanguage();
		
		//初始化VideoUploadManager
		VideoUploadManager.newInstance(this);
		
	}
	
	
	synchronized public static Context getProcess(){
		return process;
	}


	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		// TODO Auto-generated method stub
		Log.d("test", "OnLogin( " + item.toString() + " )");
	}


	@Override
	public void OnLogout(OperateType operateType) {
		// TODO Auto-generated method stub
		
	}
	
}
