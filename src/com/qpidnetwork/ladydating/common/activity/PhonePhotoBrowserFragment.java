package com.qpidnetwork.ladydating.common.activity;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.utility.Converter;

public class PhonePhotoBrowserFragment extends BaseGridViewFragment implements OnItemClickListener{

	private ArrayList<PhonePhotoBrowserAdapter.PhotoItem> photoList;
	private PhonePhotoBrowserAdapter adapter;
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		photoList = new ArrayList<PhonePhotoBrowserAdapter.PhotoItem>();
		adapter = new PhonePhotoBrowserAdapter(getActivity(), photoList);
		getProgressBar().setVisibility(View.GONE);
		queryPhotoList();
		getRefreshLayout().setEnabled(false);
	}

	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		gridView.setNumColumns(2);
		gridView.setVerticalSpacing(Converter.dp2px(2));
		gridView.setHorizontalSpacing(Converter.dp2px(1));
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(this);
		gridView.setAdapter(adapter);

		
	}
	
	private void queryPhotoList(){
		String[] projection = {MediaStore.Images.ImageColumns.DATA};
		Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  projection, null, null, null );
		
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		
		
		while(cursor.moveToNext()){
			photoList.add(new PhonePhotoBrowserAdapter.PhotoItem(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))));
			//Log.v("image", cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
		}
		
		cursor.close();
		adapter.notifyDataSetChanged();

		
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		getActivity().getIntent().setData(Uri.fromFile(new File(photoList.get(arg2).photoUri)));
		getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
		getActivity().finish();
		
	}

}
