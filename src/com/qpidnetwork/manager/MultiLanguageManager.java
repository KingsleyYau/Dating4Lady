package com.qpidnetwork.manager;

import java.util.Locale;

import com.qpidnetwork.ladydating.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class MultiLanguageManager {
	
	public static final String ACTION_CHANGE_LOACL_LANGUAGE = "multiLanguageChanged";
	
	/*存储app语言默认设置*/
	public static final String PRERENCE_LANGUAGE_CHOICE = "multiLanguageChoiced";
	public static final String PREFERENCE_KEY_LANGUAGE = "defaultLanguage";
	
	private Context mContext;
	private SharedPreferences languagePre;
	
	public enum LanguageType{
		ENGLISH,
		TRADITIONAL_CHINESE
	}
	
	public MultiLanguageManager(Context context){
		this.mContext = context;
		languagePre = mContext.getSharedPreferences(PRERENCE_LANGUAGE_CHOICE, Context.MODE_PRIVATE);
		initLocalLanguage();
	}
	
	/**
	 * 初始化设置用于默认设置App语言
	 */
	private void initLocalLanguage(){
		LanguageType type = getDefultLanguageChoice();
		changeLocale(type);
	}
	
	/**
	 * 设置App默认语言
	 * @param type
	 */
	public void setLoaclLanguage(LanguageType type){
		changeLocale(type);
		saveDefaultLanguageChoice(type);
		sendLanguageChoiceBroadcast();
	}
	
	/**
	 * 设置app语言类型
	 * @param type 语言类型
	 */
	private void changeLocale(LanguageType type) {
		switch (type) {
		case ENGLISH:
			switchLanguage(Locale.ENGLISH);
			break;
		case TRADITIONAL_CHINESE:
			switchLanguage(Locale.TRADITIONAL_CHINESE);
			break;
		default:
			switchLanguage(Locale.ENGLISH);
			break;
		}
	}
	
	/**
	 * 修改本地语言设置
	 * @param locale 
	 */
	private void switchLanguage(Locale locale) {
		Resources resources = mContext.getResources();// 获得res资源对象
		Configuration config = resources.getConfiguration();// 获得设置对象
		DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
		config.locale = locale; // 本地语言设置
		resources.updateConfiguration(config, dm);
	}
	
	/**
	 * 发送语言设置修改广播，界面接收处理界面元素
	 */
	private void sendLanguageChoiceBroadcast(){
		Intent localIntent = new Intent(ACTION_CHANGE_LOACL_LANGUAGE);
		mContext.sendBroadcast(localIntent);
	}
	
	/**
	 * 存储本地默认语言设置
	 * @param type
	 */
	private void saveDefaultLanguageChoice(LanguageType type){
		Editor edit = languagePre.edit();
		edit.putInt(PREFERENCE_KEY_LANGUAGE, type.ordinal());
		edit.commit();
	}
	
	/**
	 * 获取本地默认设置语言
	 * @return
	 */
	public LanguageType getDefultLanguageChoice(){
		LanguageType type = LanguageType.ENGLISH;
		int defLanguage = languagePre.getInt(PREFERENCE_KEY_LANGUAGE, -1);
		if(defLanguage != -1){
			type = LanguageType.values()[defLanguage];
		}
		return type;
	}
	
	/**
	 * 根据选择的语种类型，返回显示使用描述
	 * @param type
	 * @return
	 */
	public String getLanguageDescByType(LanguageType type){
		String desc = "";
		String[] languageDesc = mContext.getResources().getStringArray(R.array.languagelist);
		if(type.ordinal() < languageDesc.length){
			desc = languageDesc[type.ordinal()];
		}
		return desc;
	}

}
