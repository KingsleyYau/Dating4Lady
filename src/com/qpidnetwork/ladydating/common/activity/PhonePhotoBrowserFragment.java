package com.qpidnetwork.ladydating.common.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.qpidnetwork.framework.util.FileUtil;
import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.utility.Converter;

public class PhonePhotoBrowserFragment extends BaseGridViewFragment implements OnItemClickListener{
	
	private static final int GET_PHOTOLIST_CALLBACK = 1;

	private ArrayList<PhonePhotoBrowserAdapter.PhotoItem> mPhotoList;
	private PhonePhotoBrowserAdapter adapter;
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mPhotoList = new ArrayList<PhonePhotoBrowserAdapter.PhotoItem>();
		adapter = new PhonePhotoBrowserAdapter(getActivity(), mPhotoList);
		queryPhotoList();
		getRefreshLayout().setEnabled(false);
	}

	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		gridView.setNumColumns(3);
		//gridView.setPadding(0, UnitConversion.dip2px(mContext, 5), 0, 0);
		gridView.setBackgroundColor(Color.WHITE);
		gridView.setVerticalSpacing(Converter.dp2px(2));
		gridView.setHorizontalSpacing(Converter.dp2px(2));
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(this);
		gridView.setAdapter(adapter);
	}
	
	private void queryPhotoList(){
		getProgressBar().setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ArrayList<PhonePhotoBrowserAdapter.PhotoItem> photoList = new ArrayList<PhonePhotoBrowserAdapter.PhotoItem>();
				String[] projection = {MediaStore.Images.ImageColumns.DATA};
				Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  projection, null, null, null );
				try{
					if (cursor.getCount() == 0) {
						cursor.close();
						return;
					}
					
					
					while(cursor.moveToNext()){
						String photoUri = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
						if(!TextUtils.isEmpty(photoUri)
								&& FileUtil.getSuffix(new File(photoUri)).equals("jpg")
								&& checkPhotoSize(photoUri)){
							//过滤非jpg文件
							photoList.add(new PhonePhotoBrowserAdapter.PhotoItem(photoUri));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					cursor.close();
					Message msg = Message.obtain();
					msg.what = GET_PHOTOLIST_CALLBACK;
					msg.obj = photoList;
					sendUiMessage(msg);
				}
			}
		}).start();;
	}
	
	/**
	 * 检测Photo尺寸过滤宽/高小于720的图片
	 * @param photoUri
	 * @return
	 */
	private boolean checkPhotoSize(String photoUri){
		boolean isValid = false;
		if(!TextUtils.isEmpty(photoUri)
				&& new File(photoUri).exists()){
			BitmapFactory.Options newOpts = new BitmapFactory.Options(); 
			newOpts.inJustDecodeBounds = true; 
			BitmapFactory.decodeFile(photoUri, newOpts);
			if(newOpts.outWidth >= 720 
					&& newOpts.outHeight >= 720){
				isValid = true;
			} 
		}
		return isValid;
	}

	@Override
	protected void handleUiMessage(Message msg) {
		getProgressBar().setVisibility(View.GONE);
		switch (msg.what) {
		case GET_PHOTOLIST_CALLBACK:{
			ArrayList<PhonePhotoBrowserAdapter.PhotoItem> videoList = (ArrayList<PhonePhotoBrowserAdapter.PhotoItem>)msg.obj;
			mPhotoList.clear();
			mPhotoList.addAll(videoList);
			adapter.notifyDataSetChanged();
		}break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		getActivity().getIntent().setData(Uri.fromFile(new File(mPhotoList.get(arg2).photoUri)));
		getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
		getActivity().finish();
		
	}

}
