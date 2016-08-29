package com.qpidnetwork.ladydating.base;


import java.lang.ref.WeakReference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.auth.LoginActivity;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack.OperateType;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.customized.view.FlatToast;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressDialog;
import com.qpidnetwork.ladydating.googleanalytics.AnalyticsFragmentActivity;
import com.qpidnetwork.ladydating.home.HomeActivity;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;

public abstract class BaseFragmentActivity extends AnalyticsFragmentActivity {
	
	public static final String LIVECHAT_KICKOFF_ACTION = "kickoff";

	private FlatToast flatToast;
	private MaterialProgressDialog progressBar;
	
	private boolean isActivityVisible = false;//判断activity是否可见，用于处理异步Dialog显示 windowToken异常
	
	private BroadcastReceiver kickoffReceiver = new BroadcastReceiver(){
		public void onReceive(android.content.Context context, android.content.Intent intent) {
			String action = intent.getAction();
			if(action.equals(LIVECHAT_KICKOFF_ACTION)){
				Bundle bundle = intent.getExtras();
				if(bundle != null && bundle.containsKey(LoginActivity.LIVE_CHAT_KICK_OFF)){
					final KickOfflineType type = KickOfflineType.values()[bundle.getInt(LoginActivity.LIVE_CHAT_KICK_OFF)];
					Intent jumpIntent = new Intent(BaseFragmentActivity.this, HomeActivity.class);
					jumpIntent.putExtra(LoginActivity.LIVE_CHAT_KICK_OFF, type);
					jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(jumpIntent);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		flatToast = new FlatToast(this);
		progressBar = new MaterialProgressDialog(this);
		isActivityVisible = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isActivityVisible = true;
		/*处理被踢逻辑*/
		IntentFilter filter = new IntentFilter();
		filter.addAction(LIVECHAT_KICKOFF_ACTION);
		registerReceiver(kickoffReceiver, filter);
		
		if(QpidApplication.isKickOff){
			LoginManager.getInstance().Logout(OperateType.MANUAL);
			if(!(this instanceof LoginActivity)){
				Intent intent = new Intent(this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		isActivityVisible = false;
		try{
			unregisterReceiver(kickoffReceiver);
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 设定进度条是否可以手动返回取消
	 * @param canCancel
	 */
	public void setProgressDialogCanCancel(boolean canCancel){
		if(progressBar != null){
			progressBar.setCancelable(canCancel);
		}
	}
	
	protected void showProgressToast(String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		flatToast.setProgressing(text);
		if(isActivityVisible){
			flatToast.show();
		}
	}
	
	protected void showFailedToast(String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		flatToast.setFailed(text);
		if(isActivityVisible){
			flatToast.show();
		}
	}
	
	protected void showDoneToast(String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		flatToast.setDone(text);
		if(isActivityVisible){
			flatToast.show();
		}
	}
	
	protected void cancelToast(){
		if(flatToast != null){
			flatToast.cancelImmediately();
		}
	}
	
	protected void showAutoDismissToast(FlatToast.StikyToastType type, String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		FlatToast.showStickToast(this, text, type);
	}
	
	protected void showProgressDialog(String text){
		if(flatToast != null){
			flatToast.cancelImmediately();// cancel flat toast if it's showing before popuping a dialog.
		}
		if( !progressBar.isShowing() && isActivityVisible) {
			progressBar.setMessage(text);
			progressBar.show();
		}
	}
	
	protected void dismissProgressDialog(){
		try {
			if( progressBar != null ) {
				progressBar.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void shakeView(View v, boolean vibrate){
		
		if(vibrate){
			try{
				Vibrator vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);  
		        long [] pattern = {100, 200, 0};   //stop | vibrate | stop | vibrate
		        vibrator.vibrate(pattern, -1); 
			}catch(Exception e){
				//No vibrate if no permission
			}
		}
		v.requestFocus();
		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_anim);
		v.startAnimation(shake);
	}
	
	@Override
	public void onDestroy(){
		if (flatToast.isShowing()) flatToast.cancelImmediately();
		if (progressBar.isShowing()) progressBar.dismiss();
		super.onDestroy();
	}
	
	protected void hideKeyboard() {
		try{
			InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected Handler mHandler = new UiHandler(this) {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (getActivityReference() != null && getActivityReference().get() != null) {
                handleUiMessage(msg);
            }
        };
    };
    
    private static class UiHandler extends Handler {
        private final WeakReference<BaseFragmentActivity> mActivityReference;

        public UiHandler(BaseFragmentActivity activity) {
            mActivityReference = new WeakReference<BaseFragmentActivity>(activity);
        }

        public WeakReference<BaseFragmentActivity> getActivityReference() {
            return mActivityReference;
        }
    }
	
	/**
     * 处理更新UI任务
     * 
     * @param msg
     */
    protected void handleUiMessage(Message msg) {
    }
    
    /**
     * 发送UI更新操作
     * 
     * @param msg
     */
    protected void sendUiMessage(Message msg) {
    	mHandler.sendMessage(msg);
    }

    protected void sendUiMessageDelayed(Message msg, long delayMillis) {
    	mHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 发送UI更新操作
     * 
     * @param what
     */
    protected void sendEmptyUiMessage(int what) {
    	mHandler.sendEmptyMessage(what);
    }

    protected void sendEmptyUiMessageDelayed(int what, long delayMillis) {
    	mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }
    
    protected void removeUiMessage(int what){
    	mHandler.removeMessages(what);
    }
    
    /**
     * 判断当前activity是否可见，用于Dialog显示判断Token使用
     * @return
     */
    public boolean isActivityVisible(){
    	return isActivityVisible;
    }
    
    /**
     * 隐藏软键盘
     */
    protected void hideSoftInput() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // manager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
        if (getCurrentFocus() != null) {
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }
    
    /**
     * 显示软键盘
     */
    protected void showSoftInput() {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
	
}
