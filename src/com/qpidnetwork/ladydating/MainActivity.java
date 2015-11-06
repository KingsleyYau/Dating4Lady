package com.qpidnetwork.ladydating;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.qpidnetwork.ladydating.auth.LoginActivity;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnOtherEmotionConfigCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.OnSynConfigCallback;
import com.qpidnetwork.request.OnVersionCheckCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.RequestJniOther.ActionType;
import com.qpidnetwork.request.item.EmotionConfigItem;
import com.qpidnetwork.request.item.SynConfigItem;
import com.qpidnetwork.request.item.VersionCheckItem;


public class MainActivity extends BaseFragmentActivity implements OnClickListener{

	private Button btnLogin;
	private Button btnCheckVer;
	private Button btnSynConfig;
	private Button btnEmotion;
	private Button btnUploadCrash;
	private Button btnModifyPsd;
	private Button btnPhoneInfo;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface_test);
        
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnCheckVer = (Button)findViewById(R.id.btnCheckVer);
        btnCheckVer.setOnClickListener(this);
        btnSynConfig = (Button)findViewById(R.id.btnSynConfig);
        btnSynConfig.setOnClickListener(this);
        btnEmotion = (Button)findViewById(R.id.btnEmotion);
        btnEmotion.setOnClickListener(this);
        btnUploadCrash = (Button)findViewById(R.id.btnUploadCrash);
        btnUploadCrash.setOnClickListener(this);
        btnModifyPsd = (Button)findViewById(R.id.btnModifyPsd);
        btnModifyPsd.setOnClickListener(this);
        btnPhoneInfo = (Button)findViewById(R.id.btnPhoneInfo);
        btnPhoneInfo.setOnClickListener(this);
        
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLogin:
			onLoginClick();
			break;
		case R.id.btnCheckVer:
			onCheckClick();
			break;
		case R.id.btnSynConfig:
			onSynConfig();
			break;
		case R.id.btnEmotion:
			onEmotionConfig();
			break;
		case R.id.btnUploadCrash:
			onUploadCrash();
			break;
		case R.id.btnModifyPsd:
			onModifyPsd();
			break;
		case R.id.btnPhoneInfo:
			onPhoneInfo();
			break;
		default:
			break;
		}
	}
	
	private void onLoginClick(){

	}
	
	private void onCheckClick(){
		RequestJniOther.VersionCheck(new OnVersionCheckCallback() {
			
			@Override
			public void OnVersionCheck(boolean isSuccess, String errno, String errmsg,
					VersionCheckItem item) {
				Log.i("hunter", "VersionCheck is : " + (isSuccess?"true":"false"));				
			}
		});
	}

	private void onSynConfig(){
		RequestJniOther.SynConfig(new OnSynConfigCallback() {
			
			@Override
			public void OnSynConfig(boolean isSuccess, String errno, String errmsg,
					SynConfigItem item) {
				Log.i("hunter", "SynConfig is : " + (isSuccess?"true":"false"));					
			}
		});
	}

	private void onEmotionConfig(){
		RequestJniOther.EmotionConfig(new OnOtherEmotionConfigCallback() {
			
			@Override
			public void OnOtherEmotionConfig(boolean isSuccess, String errno,
					String errmsg, EmotionConfigItem item) {
				Log.i("hunter", "EmotionConfig is : " + (isSuccess?"true":"false"));
			}
		});
	}
	private void onUploadCrash(){
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		RequestJniOther.UploadCrashLog(RequestJni.GetDeviceId(tm), FileCacheManager.newInstance(this).GetCrashInfoPath(), FileCacheManager.newInstance(this).GetCrashInfoPath(), new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Log.i("hunter", "UploadCrashLog is : " + (isSuccess?"true":"false"));				
			}
		});
	}
	private void onModifyPsd(){
		RequestJniOther.ModifyPassword("12345", "12345", new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Log.i("hunter", "ModifyPassword is : " + (isSuccess?"true":"false"));
			}
		});
	}
	private void onPhoneInfo(){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = RequestJni.GetDeviceId(tm);
		
		try{
			PackageInfo packetInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String userAccount = "P2:P580502";
			
			RequestJniOther.PhoneInfo(userAccount, packetInfo.versionName, ActionType.SETUP, 0, dm.widthPixels, dm.heightPixels, deviceId, new OnRequestCallback() {
				
				@Override
				public void OnRequest(boolean isSuccess, String errno, String errmsg) {
					Log.i("hunter", "ModifyPassword is : " + (isSuccess?"true":"false"));
				}
			});
		}catch(Exception e){
			
		}
	}
}
