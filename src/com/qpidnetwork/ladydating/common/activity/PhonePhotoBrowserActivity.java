package com.qpidnetwork.ladydating.common.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.manager.FileCacheManager;

public class PhonePhotoBrowserActivity extends BaseFragmentActivity{

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
		
		setContentView(R.layout.activity_phone_photo_browser);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.baseToolbar);
		View adaptiveAcitionbarShawdow = (View) findViewById(R.id.adaptiveTabbarShawdow);
		if (Build.VERSION.SDK_INT >= 21) 
			adaptiveAcitionbarShawdow.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setBackgroundColor(getResources().getColor(setupThemeColor()));
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_grey600_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.photo_browser));
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color_dark));
        
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragmentReplacement, new PhonePhotoBrowserFragment()).commit();
	}

	
	private void capturePhoto() {
		captureUri = FileCacheManager.getInstance().GetTempCameraImageUrl();
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

	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch (menu.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.camera:
			capturePhoto();
			break;
		default:
			break;
		}
	}

	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.camera, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	onMenuItemSelected(item);

        return super.onOptionsItemSelected(item);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       return onCreateOptionsMenu(menu, getMenuInflater());
    }

}
