package com.qpidnetwork.ladydating.base;


import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.FlatToast;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressDialog;

public abstract class BaseFragmentActivity extends AppCompatActivity{

	private FlatToast flatToast;
	private MaterialProgressDialog progressBar;
	
	private boolean isActivityVisible = false;//判断activity是否可见，用于处理异步Dialog显示 windowToken异常
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		flatToast = new FlatToast(this);
		progressBar = new MaterialProgressDialog(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isActivityVisible = true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActivityVisible = false;
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
	    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
    
    /**
     * 判断当前activity是否可见，用于Dialog显示判断Token使用
     * @return
     */
    public boolean isActivityVisible(){
    	return isActivityVisible;
    }
	
}
