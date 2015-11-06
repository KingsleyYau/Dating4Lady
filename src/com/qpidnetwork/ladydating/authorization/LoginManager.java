package com.qpidnetwork.ladydating.authorization;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack.OperateType;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.manager.PreferenceManager;
import com.qpidnetwork.request.LoginManagerJni;
import com.qpidnetwork.request.OnLoginManagerCallback;
import com.qpidnetwork.request.RequestErrorCode;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.LoginItem;

/**
 * 认证模块
 * 登录状态管理器 
 * @author Max.Chiu
 *
 */
public class LoginManager implements OnLoginManagerCallback {
	
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
	private LoginItem mLoginItem = null;
	private static LoginManager gLoginManager = null;
	
	public static LoginManager newInstance(Context context) {
		if (gLoginManager == null) {
			gLoginManager = new LoginManager(context);
		}
		return gLoginManager;
	}
	
	public static LoginManager getInstance() {
		return gLoginManager;
	}
	
	/**
	 * 登录状态改变监听
	 */
	private List<IAuthorizationCallBack> mCallbackList = new ArrayList<IAuthorizationCallBack>();
	
    /**
     * 当前登录状态
     */
    private LoginStatus mLoginStatus = LoginStatus.NONE;
    
    private PreferenceManager mPreferenceManager;
    
	@SuppressLint("HandlerLeak")
	public LoginManager(Context context) {
		
		mContext = context;
		PreferenceManager.newInstance(context);
		mPreferenceManager = PreferenceManager.getInstance();
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				RequestBaseResponse response = (RequestBaseResponse) msg.obj;
				LoginParam param = (LoginParam)response.body;
				if( response.isSuccess ) {
					// 改变状态
		        	mLoginStatus = LoginStatus.LOGINED;
					
					// 登录成功, 处理是否记住密码, 自动登录等
		        	mPreferenceManager.saveLoginParam(param);
				} else {
		        	mLoginStatus = LoginStatus.NONE;
					switch (response.errno) {
					default:
						break;
					}
				}
				
				// 通知其他模块
				for(IAuthorizationCallBack callback : mCallbackList) {
					if( callback != null ) {
						callback.OnLogin(OperateType.MANUAL, response.isSuccess, response.errno, response.errmsg, param.item);
					} 
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
     * 注销
     */
    public void Logout(OperateType operateType) {
    	RequestJni.CleanCookies();
    	
		Log.d("LoginManager", "Logout( " + 
				"mLoginStatus : " + mLoginStatus.name() + 
				" )");
		
		if( mLoginStatus != LoginStatus.LOGINED ) {
			return;
		}
    	mLoginStatus = LoginStatus.NONE;
    	
    	LoginParam param = mPreferenceManager.getLoginParam();
    	if( param != null ) {
        	param.item = null;
        	if(operateType == OperateType.MANUAL){
        		//手动注销需清除密码
        		param.password = "";
        	}
    	}

    	mPreferenceManager.saveLoginParam(param);
    	
		// 通知其他模块
		for(IAuthorizationCallBack callback : mCallbackList) {
			if( callback != null ) {
				callback.OnLogout(operateType);
			} 
		}
    }
    
    /**
     * 自动登录
     */
    public void AutoLogin() {
		Log.d("LoginManager", "AutoLogin( " + 
				"mLoginStatus : " + mLoginStatus.name() + 
				" )");
		
		if( mLoginStatus != LoginStatus.NONE ) {
			return;
		}
		
		boolean bCallback = true;
    	LoginParam param = mPreferenceManager.getLoginParam();
    	if( param != null ) {
			if( param.email != null && param.email.length() > 0 &&
		    		param.password != null && param.password.length() > 0 ) {
				Login(param.email, param.password);
				bCallback = false;
			}
    	} 
		
    	if( bCallback ) {
    		// 通知其他模块
    		for(IAuthorizationCallBack callback : mCallbackList) {
    			if( callback != null ) {
    				callback.OnLogin(OperateType.MANUAL, false, RequestErrorCode.LOCAL_ERROR_CODE_NERVER_LOGIN, "Login fail!", null);
    			} 
    		}
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
	public boolean CheckLogin(Context context) {
		return CheckLogin(context, true);
	}
	
	/**
	 * 判断是否登录, 并弹出对应界面
	 */
	public boolean CheckLogin(Context context, boolean bShowLoginActivity) {
		return CheckLogin(context, bShowLoginActivity, "");
	}
	
	/**
	 * 获取 LoginItem，若未登录则返回null
	 * @return
	 */
	public LoginItem GetLoginItem()
	{
		return mLoginItem;
	}
	
	/**
	 * 判断是否登录, 并弹出对应界面, 并传递参数
	 */
	public boolean CheckLogin(Context context, boolean bShowLoginActivity, String param) {
		boolean bFlag = false;
		// 判断是否登录
		switch (LoginManager.getInstance().GetLoginStatus()) {
		case NONE: {
			// 处于未登录状态
		}			
		case LOGINING:{
			// 处于未登录状态, 点击弹出登录界面
			if( bShowLoginActivity ) {
//				Intent intent = new Intent(mContext, RegisterActivity.class);
//				//传递参数 
//				if (null != param && !param.isEmpty()) {
//					intent.putExtra("param", param);
//				}
//				
//				context.startActivity(intent);
			}
		}break;
		case LOGINED: {
			// 处于登录状态
			bFlag = true;
		}break;
		default:
			break;
		}
		return bFlag;
	}

	@Override
	public void OnLogin(boolean isSuccess, String errno, String errmsg,
			LoginItem item) {
		// TODO Auto-generated method stub
		mLoginItem = item;
		
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		LoginParam param = new LoginParam(LoginManagerJni.GetUser(), LoginManagerJni.GetPassword(), item);
		response.body = param;
		msg.obj = response;
		mHandler.sendMessage(msg);
	}

	@Override
	public void OnLogout(int type) {
		// TODO Auto-generated method stub
		mLoginItem = null;
	}
}
