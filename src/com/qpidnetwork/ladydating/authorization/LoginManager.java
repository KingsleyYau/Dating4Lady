package com.qpidnetwork.ladydating.authorization;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.qpidnetwork.ladydating.auth.PhoneInfoManager;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack.OperateType;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.ladydating.googleanalytics.AnalyticsManager;
import com.qpidnetwork.manager.PreferenceManager;
import com.qpidnetwork.request.LoginManagerJni;
import com.qpidnetwork.request.OnLoginManagerCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.SynConfigItem;

/**
 * 认证模块
 * 登录状态管理器 
 * @author Max.Chiu
 *
 */
public class LoginManager implements OnLoginManagerCallback {
	
	private static final int LOGIN_CALLBACK = 1;
	private static final int LOGOUT_CALLBACK = 2;
	
	/**
	 * 登录状态
	 * @param NONE			未登录
	 * @param LOGINING		登录中
	 * @param LOGINED		已登录
	 */
	public enum LoginStatus {
		NONE,
		LOGINING,
		LOGINED,
	}	
	
	/**
	 * 实例变量
	 */
	private Context mContext = null;
	private Handler mHandler = null;
	private static LoginManager gLoginManager = null;
	private String accountId = "";
	private String password = "";
	private OperateType mOperateType = OperateType.MANUAL;//用于区分当前登陆是否是session过期重新登陆还是手动登陆
	
	/**
	 * 登录状态改变监听
	 */
	private List<IAuthorizationCallBack> mCallbackList = new ArrayList<IAuthorizationCallBack>();
	
    /**
     * 当前登录状态
     */
    private LoginStatus mLoginStatus = LoginStatus.NONE;
    
    private PreferenceManager mPreferenceManager;
    
    //保存同步配置信息，用于登陆后使用
  	private SynConfigItem mSynConfigItem = null;
    
    
    public static LoginManager newInstance(Context context) {
		if (gLoginManager == null) {
			gLoginManager = new LoginManager(context);
		}
		return gLoginManager;
	}
	
	public static LoginManager getInstance() {
		return gLoginManager;
	}
    
	@SuppressLint("HandlerLeak")
	public LoginManager(Context context) {
		
		mContext = context;
		mPreferenceManager = new PreferenceManager(context);
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				RequestBaseResponse response = (RequestBaseResponse) msg.obj;
				switch (msg.what) {
				case LOGIN_CALLBACK:{
					LoginParam param = (LoginParam)response.body;
					if( response.isSuccess ) {
						// 登录成功, 处理是否记住密码, 自动登录等
			        	mPreferenceManager.saveLoginParam(param);
			        	if(param != null && param.item != null){
			        		mPreferenceManager.setCurrentUserId(param.item.lady_id);
				        	//Login成功同步用户信息到数据库，多用户处理
				        	ChatHistoryDB.getInstance(mContext).synUserId(param.item.lady_id);
			        	}
			        	
			        	//上传PhoneInfo
			        	PhoneInfoManager.RequestPhoneInfo(mContext, param.item);
			        	
			        	// 统计登录
			        	AnalyticsManager.newInstance().setGAUserId(param.item.lady_id);
					} else {
						switch (response.errno) {
						default:
							break;
						}
					}
					
					// 通知其他模块
					for(IAuthorizationCallBack callback : mCallbackList) {
						if( callback != null ) {
							callback.OnLogin(mOperateType, response.isSuccess, response.errno, response.errmsg, param.item);
						} 
					}
				}break;

				case LOGOUT_CALLBACK:{
					int type = (Integer)response.body;
					mOperateType = OperateType.values()[type];
					// 通知其他模块
					for(IAuthorizationCallBack callback : mCallbackList) {
						if( callback != null ) {
							callback.OnLogout(OperateType.values()[type]);
						} 
					}
				}break;
				
				default:
					break;
				}
				
			}
		};
		
		LoginManagerJni.SetLoginCallback(this);
	}

	/**
	 * 增加登录结果监听器
	 * @param callback
	 */
	public void AddListenner(IAuthorizationCallBack callback) {
		mCallbackList.add(callback);
	}
	
	/**
	 * 删除登录结果监听器
	 * @param callback
	 */
	public void RemoveListenner(IAuthorizationCallBack callback) {
		mCallbackList.remove(callback);
	}
	
	/**
	 * 登录接口
	 * @param email			账号
	 * @param password		密码
	 * @return				
	 */
	public void Login(final String email, final String password) {
		this.accountId = email;
		this.password = password;
		TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		LoginManagerJni.Login(
				email, 
				password, 
				RequestJni.GetDeviceId(tm), 
				Build.MODEL, 
				Build.MANUFACTURER
				);
	}
	
	/**
	 * Session过期后重新登陆（处理Webview等 Session过期）
	 */
	public void LoginBySessionOuttime(){
		Logout(OperateType.AUTO);
		Login(accountId, password);
	}
	
    /**
     * 手动注销，清除密码，
     */
    public void Logout(OperateType type) {
    	LoginManagerJni.Logout(type.ordinal());
    	mLoginStatus = LoginStatus.NONE;
    	
    	if(type == OperateType.MANUAL){
    		//手动注销清除账号密码
	    	LoginParam param = GetLoginParam();
	    	if( param != null ) {
	        	param.item = null;
	        	param.password = "";
	    	}
	
	    	mPreferenceManager.saveLoginParam(param);
    	}
    }
    
    /**
     * 获取当前登录状态
     * @return
     */
    public LoginStatus GetLoginStatus() {
    	return mLoginStatus;
    }
    
    /**
     * 设置当前登录状态
     * @param status
     */
    public void setLoginnStatus(LoginStatus status){
    	this.mLoginStatus = status;
    }
    
    /**
     * 获取登录参数（包括帐号、密码等）
     * @return
     */
    public LoginParam GetLoginParam() 
    {
    	return mPreferenceManager.getLoginParam();
    }
    
	/**
	 * 判断是否登录, 并弹出对应界面
	 */
	public boolean CheckLogin() {
		boolean isLogined = false;
		if(mLoginStatus == LoginStatus.LOGINED){
			isLogined = true;
		}
		return isLogined;
	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item) {
		Message msg = Message.obtain();
		msg.what = LOGIN_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		LoginParam param = new LoginParam(LoginManagerJni.GetUser(), LoginManagerJni.GetPassword(), item);
		response.body = param;
		msg.obj = response;
		mHandler.sendMessage(msg);
	}

	@Override
	public void OnLogout(int type) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = LOGOUT_CALLBACK;
		OperateType operateType = OperateType.values()[type];
		RequestBaseResponse response = new RequestBaseResponse(true, "", "", Integer.valueOf(operateType.ordinal()));
		msg.obj = response;
		mHandler.sendMessage(msg);
	}
	
	/**
	 * 更新同步配置信息
	 * @param item
	 */
	public void setSynConfigItem(SynConfigItem item){
		if(item != null){
			//设置翻译Host
			String host = "";
			if(!TextUtils.isEmpty(item.translateUrl)){
				host = item.translateUrl.substring(0, item.translateUrl.indexOf("/v2/http.svc/Translate"));
				RequestJni.SetTransSite(host);
			}
		}
		this.mSynConfigItem = item;
	}

	/**
	 * 获取同步配置信息
	 * @return
	 */
	public SynConfigItem getSynConfigItem(){
			return mSynConfigItem;
	}
}
