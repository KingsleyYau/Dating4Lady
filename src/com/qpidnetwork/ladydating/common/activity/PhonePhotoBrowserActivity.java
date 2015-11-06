package com.qpidnetwork.ladydating.common.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.manager.FileCacheManager;

public class PhonePhotoBrowserActivity extends BaseActionbarActivity{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.PHONO_PHOTO_BROWSER_ACTIVITY_CODE;
	
	public static void launch(Context context){
		Intent intent = new Intent(context, PhonePhotoBrowserActivity.class);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	private FloatingActionButton captureButton;
	private String captureUri;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragmentReplacement, new PhonePhotoBrowserFragment()).commit();
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle(getString(R.string.photo_browser), getResources().getColor(R.color.text_color_dark));
		captureButton = (FloatingActionButton)findViewById(R.id.add);
		captureButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.add:
			capturePhoto();
			break;
		}
	}

	
	private void capturePhoto() {
		captureUri = FileCacheManager.getInstance().GetTempCameraImageUrl() + System.currentTimeMillis();
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(captureUri)));
	    startActivityForResult(intent, ActivityCodeUtil.CAPTURE_PHOTO_ACTIVITY_CODE);
	}

	private void onCapturePhotoActivityResult(Intent data){
		getIntent().setData(Uri.fromFile(new File(captureUri)));
		setResult(Activity.RESULT_OK, getIntent());
		finish();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		
		if (requestCode == ActivityCodeUtil.CAPTURE_PHOTO_ACTIVITY_CODE) {
			onCapturePhotoActivityResult(data);
	    }
		

	}
	
	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_phone_photo_browser;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		if (menu.getItemId() == android.R.id.home) finish();
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}

}
