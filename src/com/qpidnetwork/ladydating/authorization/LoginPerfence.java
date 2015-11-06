package com.qpidnetwork.ladydating.authorization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class LoginPerfence {
	/**
	 * 缓存登录参数
	 * @param context	上下文
	 * @param item		个人资料
	 */
	public static void SaveLoginParam(Context context, LoginParam item) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			if( item != null ) {
		        oos.writeObject(item);  
			}
	        String personBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));  
	        SharedPreferences.Editor editor = mSharedPreferences.edit();  
	        editor.putString("LoginParam", personBase64);  
	        editor.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	/**
	 * 获取缓存登录参数
	 * @param context	上下文
	 * @return			个人资料
	 */
	public static LoginParam GetLoginParam(Context context) {
		LoginParam item = null;
		
        try {  
            SharedPreferences mSharedPreferences = context.getSharedPreferences("base64", Context.MODE_PRIVATE);  
            String personBase64 = mSharedPreferences.getString("LoginParam", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(), Base64.DEFAULT);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            item = (LoginParam) ois.readObject();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
		return item;
	}
	
	public static void SaveStringPreference(Context context, String key, String string) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("common_string", Context.MODE_PRIVATE); 
	    SharedPreferences.Editor editor = mSharedPreferences.edit();  
	    editor.putString(key, string);  
	    editor.commit();
	}
	
	
	public static String GetStringPreference(Context context, String key) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("common_string", Context.MODE_PRIVATE); 
		String value = mSharedPreferences.getString(key, "");  
		return value;
	}
}
