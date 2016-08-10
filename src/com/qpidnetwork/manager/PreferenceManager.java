package com.qpidnetwork.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;

import com.qpidnetwork.ladydating.authorization.LoginParam;

/**
 * Preference 数据存储管理类
 * 
 * @author Hunter
 * @since 2015.10.23
 */
public class PreferenceManager {

	private Context mContext;
	private SharedPreferences mSharedPreferences;
	
	/*用于多用户设置区分处理*/
	private String lady_id = "";

	public PreferenceManager(Context context) {
		mContext = context;
		mSharedPreferences = mContext.getSharedPreferences(
				PreferenceDefine.QPIDNETWORK_LADY_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}
	
	/**
	 * 用于多用户处理操作
	 * @param userId
	 */
	public void setCurrentUserId(String userId){
		if(!TextUtils.isEmpty(userId)){
			this.lady_id = userId;
		}
	}

	private void save(String key, String value) {
		if (mSharedPreferences != null) {
			Editor edit = mSharedPreferences.edit();
			edit.putString(key, value);
			edit.commit();
		}
	}

	private void save(String key, boolean value) {
		if (mSharedPreferences != null) {
			Editor edit = mSharedPreferences.edit();
			edit.putBoolean(key, value);
			edit.commit();
		}
	}

	private String getString(String key) {
		String value = null;
		if (mSharedPreferences != null) {
			value = mSharedPreferences.getString(key, "");
		}
		return value;
	}

	private boolean getBoolean(String key) {
		boolean value = false;
		if (mSharedPreferences != null) {
			value = mSharedPreferences.getBoolean(key, false);
		}
		return value;
	}

	// 第一次登陆标志位
	public void saveFirstStartFlags(boolean isFirstStart) {
		save(PreferenceDefine.QPIDNETWORK_LADY_FIRST_START, isFirstStart);
	}

	public boolean isFirstStartUp() {
		return getBoolean(PreferenceDefine.QPIDNETWORK_LADY_FIRST_START);
	}

	// 登陆成功参数存储
	public void saveLoginParam(LoginParam item) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if (item != null) {
				oos.writeObject(item);
			}
			String personBase64 = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT));
			save(PreferenceDefine.QPIDNETWORK_LADY_LOGIN_PARAM, personBase64);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LoginParam getLoginParam() {
		LoginParam item = null;

		try {
			String personBase64 = getString(PreferenceDefine.QPIDNETWORK_LADY_LOGIN_PARAM);
			if (!TextUtils.isEmpty(personBase64)) {
				byte[] base64Bytes = Base64.decode(personBase64.getBytes(),
						Base64.DEFAULT);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						base64Bytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				item = (LoginParam) ois.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return item;
	}

	// 存储Notification默认设置
	public void saveNotificationSwitchSetting(boolean isOn) {
		save(PreferenceDefine.QPIDNETWORK_LADY_NOTIFICATION_SWITCH + lady_id, isOn);
	}

	public boolean getNotificationSwitchSetting() {
		boolean value = true;
		if (mSharedPreferences != null) {
			value = mSharedPreferences.getBoolean(
					PreferenceDefine.QPIDNETWORK_LADY_NOTIFICATION_SWITCH + lady_id, true);
		}
		return value;
	}
	
	//存储针对指定男士翻译语言设置
	public void saveDefaulteTranslateForMan(String manId, String translateLang) {
		save(PreferenceDefine.QPIDNETWORK_LADY_TRANSLATE_LANG + lady_id + manId, translateLang);
	}
	
	/**
	 * 翻译语言简称（与同步配置返回翻译语言设置一致）
	 * @return
	 */
	public String getDefaulteTranslateForMan(String manId) {
		String translateLang = "";
		if (mSharedPreferences != null) {
			translateLang = mSharedPreferences.getString(
					PreferenceDefine.QPIDNETWORK_LADY_TRANSLATE_LANG + lady_id + manId, "");
		}
		return translateLang;
	}
}
