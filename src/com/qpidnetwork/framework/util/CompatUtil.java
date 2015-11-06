package com.qpidnetwork.framework.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * 高低版本兼容工具类
 * @author Hunter 
 * 2015.7.6
 */
public class CompatUtil {
	
	/**
	 * 解决4.4及以上版本与4.4一下版本读取系统相册差异化问题
	 * @return
	 */
	public static Intent getSelectPhotoFromAlumIntent(){
		Intent intent = new Intent();
		intent.setType("image/*");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		}else{
			intent.setAction(Intent.ACTION_GET_CONTENT);
		}
		return intent;
	}
	
	/**
	 * 解决4.4及以上版本与4.4一下版本读取系统相册差异化问题
	 * @return
	 */
	public static String getSelectedPhotoPath(Context context, Uri contentUri){
		String filePath = "";
		if((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) && (DocumentsContract.isDocumentUri(context, contentUri))){
		    String wholeID = DocumentsContract.getDocumentId(contentUri);
		    if(wholeID.contains(":")){
			    String id = wholeID.split(":")[1];
			    String[] column = { MediaStore.Images.Media.DATA };
			    String sel = MediaStore.Images.Media._ID + "= ?";
			    try{
				    Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
				            sel, new String[] { id }, null);
				    if(cursor != null){
				    	cursor.moveToFirst();
				    	int columnIndex = cursor.getColumnIndex(column[0]);
				    	filePath = cursor.getString(columnIndex);
				    	cursor.close();
				    }
			    }catch(Exception e){
			    	
			    }
		    }
		    
		}else{
		    String[] projection = { MediaStore.Images.Media.DATA };
		    try{
			    Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
			    
			    if( cursor != null ) {
			    	cursor.moveToFirst();
			    	int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			    	filePath = cursor.getString(column_index);
			    	cursor.close();
			    }
		    }catch(Exception e){
	    	
		    }
		}
		return filePath;
	}
	
}
