package com.qpidnetwork.ladydating;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerTest;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MultiLanguageManager;
import com.qpidnetwork.manager.PreferenceManager;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.ConfigManagerJni;
import com.qpidnetwork.request.OnConfigManagerCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.SynConfigItem;
import com.qpidnetwork.tool.CrashHandlerJni;
import com.qpidnetwork.tool.ImageHandler;

public class QpidApplication extends Application implements IAuthorizationCallBack, OnConfigManagerCallback {
	
	private static Context process;
	private Handler mHandler;
	private Handler mHandlerTest;
	private boolean bTest = false;
//	private LiveChatManagerTest lcTest = new LiveChatManagerTest();
	@Override
	public void onCreate() {
		super.onCreate();
		process = this;
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QpidDatingLady/";
		
		FileCacheManager.newInstance(this);
		FileCacheManager.getInstance().ChangeMainPath(path);
		
		//初始化站基本配置
		WebsiteManager.newInstance(this);
		
		//preference manager 初始化
		PreferenceManager.newInstance(this);
		
		// Jni错误捕捉
		CrashHandlerJni.SetCrashLogDirectory(FileCacheManager.getInstance().GetCrashInfoPath());
		ImageHandler.SetLogPath(FileCacheManager.getInstance().GetLogPath());
		
		RequestJni.SetLogDirectory(FileCacheManager.getInstance().GetLogPath());
		RequestJni.SetWebSite("http://demo.chnlove.com", "http://demo-mobile.chnlove.com");
		RequestJni.SetCookiesDirectory(path);
		RequestJni.SetAuthorization("test", "5179");
		
		// 初始化LiveChatManager
		LiveChatManager lcMgr = LiveChatManager.newInstance(this);
		ConfigManagerJni.AddCallback(lcMgr);	// 添加 LiveChatManager 获取同步配置
		// LiveChatManager测试
//		lcMgr.RegisterOtherListener(lcTest);
//		lcMgr.RegisterMessageListener(lcTest);
//		lcMgr.RegisterEmotionListener(lcTest);
//		lcTest.Init();
		
		// 初始化登录管理器
		LoginManager lm = LoginManager.newInstance(this);
		lm.AddListenner(lcMgr);	// 添加LiveChatManager Listener
		
		// 获取同步配置
		ConfigManagerJni.AddCallback(this);
		ConfigManagerJni.Sync();
		
//		Test();
	}
	
	
	synchronized public static Context getProcess(){
		return process;
	}
	
	public void Test() {
//		LoginManager.getInstance().AddListenner(this);
//		LoginManager.getInstance().Login("P580502", "12345");
		
		mHandlerTest = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
//				RequestJniMan.QueryManList(0, 30, QueryType.DEFAULT, "", -1, -1, Country.Unknow, false, new OnQueryManListCallback() {
//					@Override
//					public void OnQueryManList(boolean isSuccess, String errno,
//							String errmsg, ManListItem[] itemList,
//							int totalCount) {
//						// TODO Auto-generated method stub
//						Log.d("test", "QueryManList( isSuccess : " + isSuccess + " )");
//						if( itemList != null ) {
//							for( ManListItem item : itemList ) {
//								Log.d("test", "QueryManDetail( " + item.toString() + " )");
//							}
//						}
//					}
//				});
				
//				RequestJniMan.QueryManDetail("CM46528572", new OnQueryManDetailCallback() {
//					@Override
//					public void OnQueryManDetail(boolean isSuccess,
//							String errno, String errmsg, ManDetailItem item) {
//						// TODO Auto-generated method stub
//						Log.d("test", "QueryManDetail( isSuccess : " + isSuccess + " )");
//						if( item != null ) {
//							Log.d("test", "QueryManDetail( " + item.toString() + " )");
//						}
//					}
//				});
				
//				RequestJniMan.QueryFavourList(new OnQueryFavourListCallback() {
//					@Override
//					public void OnQueryFavourList(boolean isSuccess, String errno,
//							String errmsg, String[] itemList) {
//						// TODO Auto-generated method stub
//						Log.d("test", "QueryFavourList( isSuccess : " + isSuccess + " )");
//						if( itemList != null ) {
//							for( String item : itemList ) {
//								Log.d("test", "QueryFavourList( " + item + " )");
//							}
//						}
//					}
//				});
				
//				RequestJniMan.AddFavourites("CM46528572", new OnRequestCallback() {
//					
//					@Override
//					public void OnRequest(boolean isSuccess, String errno, String errmsg) {
//						// TODO Auto-generated method stub
//						Log.d("test", "AddFavourites( isSuccess : " + isSuccess + " )");
//					}
//				});
				
//				RequestJniMan.RemoveFavourites(new String[]{"CM46528572"}, new OnRequestCallback() {
//					
//					@Override
//					public void OnRequest(boolean isSuccess, String errno, String errmsg) {
//						// TODO Auto-generated method stub
//						Log.d("test", "RemoveFavourites( isSuccess : " + isSuccess + " )");
//					}
//				});
				
//				RequestJniMan.QueryManRecentChatList(0, 30, RecentChatQueryType.ALL, new OnQueryManRecentChatListCallback() {
//					@Override
//					public void OnQueryManRecentChatList(boolean isSuccess,
//							String errno, String errmsg,
//							ManRecentChatListItem[] itemList, int totalCount) {
//						// TODO Auto-generated method stub
//						Log.d("test", "QueryManRecentChatList( isSuccess : " + isSuccess + " )");
//						if( itemList != null ) {
//							for( ManRecentChatListItem item : itemList ) {
//								Log.d("test", "QueryManRecentChatList( " + item.toString() + " )");
//							}
//						}
//					}
//				});
				
//				RequestJniMan.QueryManRecentViewList(new OnQueryManRecentViewListCallback() {
//					@Override
//					public void OnQueryManRecentViewList(boolean isSuccess,
//							String errno, String errmsg,
//							ManRecentViewListItem[] itemList) {
//						// TODO Auto-generated method stub
//						Log.d("test", "QueryManRecentChatList( isSuccess : " + isSuccess + " )");
//						if( itemList != null ) {
//							for( ManRecentViewListItem item : itemList ) {
//								Log.d("test", "QueryManRecentViewList( " + item.toString() + " )");
//							}
//						}
//					}
//				});
				
//				if( !bTest ) {
//					bTest = true;
//					long requestId = RequestJniMan.QueryManList(0, 30, QueryType.DEFAULT, "", -1, -1, Country.Unknow, false, new OnQueryManListCallback() {
//						@Override
//						public void OnQueryManList(boolean isSuccess, String errno,
//								String errmsg, ManListItem[] itemList,
//								int totalCount) {
//							// TODO Auto-generated method stub
//							Log.d("test", "QueryManList( isSuccess : " + isSuccess + " )");
//							if( itemList != null ) {
//								for( ManListItem item : itemList ) {
//									Log.d("test", "QueryManList( " + item.toString() + " )");
//								}
//							}
//						}
//					});
////					try {
////						Thread.sleep(3000);
////					} catch (InterruptedException e) {
////						// TODO Auto-generated catch block
////						e.printStackTrace();
////					}
////					RequestJni.StopRequest(requestId);
//				}
			}
		};
		
//		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//		long requestId = RequestJniAuthorization.Login(
//				"P580502", 
//				"12345", 
//				RequestJni.GetDeviceId(tm), 
//				Build.MODEL, 
//				Build.MANUFACTURER, 
//				new OnLoginCallback() {
//					
//					@Override
//					public void OnLogin(boolean isSuccess, String errno, String errmsg,
//							LoginItem item) {
//						// TODO Auto-generated method stub
//						Log.d("test", "Login( isSuccess : " + isSuccess + ", errmsg : " + errmsg + " )");
//						if( isSuccess ) {
//							if( item != null ) {
//								Log.d("test", "Login( " + item.toString() + " )");
//							}
//							mHandlerTest.sendEmptyMessage(0);
//						}
//					}
//				});
//		
//		RequestJni.StopRequest(requestId);
//		LoginManagerJni.SetLoginCallback(this);
//		LoginManagerJni.Login(
//				"P580502", 
//				"12345", 
//				RequestJni.GetDeviceId(tm), 
//				Build.MODEL, 
//				Build.MANUFACTURER
//				);
		
//		long requestId = RequestJniMan.QueryManList(0, 30, QueryType.DEFAULT, "", -1, -1, Country.Unknow, false, new OnQueryManListCallback() {
//			@Override
//			public void OnQueryManList(boolean isSuccess, String errno,
//					String errmsg, ManListItem[] itemList,
//					int totalCount) {
//				// TODO Auto-generated method stub
//				Log.d("test", "QueryManList( isSuccess : " + isSuccess + " )");
//				if( itemList != null ) {
//					for( ManListItem item : itemList ) {
//						Log.d("test", "QueryManList( " + item.toString() + " )");
//					}
//				}
//			}
//		});
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


	@Override
	public void OnSynConfig(boolean isSuccess, String errno, String errmsg,
			SynConfigItem item) {
		// TODO Auto-generated method stub
		Log.d("test", "OnSynConfig( " + item.toString() + " )");
	}
}
