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
import com.qpidnetwork.ladydating.utility.UriUtil;

public class PhoneVideoBrowserActivity extends BaseActionbarActivity{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.PHONO_VIDEO_BROWSER_ACTIVITY_CODE;
	
	public static void launch(Context context){
		Intent intent = new Intent(context, PhoneVideoBrowserActivity.class);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	private FloatingActionButton captureButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragmentReplacement, new PhoneVideoBrowserFragment()).commit();
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle(getString(R.string.video_browser), getResources().getColor(R.color.text_color_dark));
		
		captureButton = (FloatingActionButton)findViewById(R.id.add);
		captureButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.add:
			captureVideo();
			break;
		}
	}

	private void captureVideo() {
	    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(takeVideoIntent, ActivityCodeUtil.CAPTURE_VIDEO_ACTIVITY_CODE);
	    }
	}
	
	private void onVideoCaptureActivityResult(Intent data){
		getIntent().setData(Uri.fromFile(new File(UriUtil.getRealPathFromURI(this, data.getData()))));
		setResult(Activity.RESULT_OK, getIntent());
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		
	    if (requestCode == ActivityCodeUtil.CAPTURE_VIDEO_ACTIVITY_CODE) {
	    	onVideoCaptureActivityResult(data);
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
