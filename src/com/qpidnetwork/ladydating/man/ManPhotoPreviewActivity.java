package com.qpidnetwork.ladydating.man;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.ladydating.customized.view.TouchImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;

public class ManPhotoPreviewActivity extends BaseFragmentActivity implements ImageViewLoaderCallback{
	
	private static final String MAN_PHOTO_URL = "manPhotoUrl";
	private static final int GET_PHOTO_SUCCESS = 1;
	private static final int GET_PHOTO_FAILED = 2;
	private String mPhotoUrl;
	
	private MaterialProgressBar progress;
	private TouchImageView imageView;
	private ImageButton buttonCancel;
	private ImageViewLoader mDownloader;
	
	public static void launchManPhotoPreviewActivity(Context context, String photoUrl){
		Intent intent = new Intent(context, ManPhotoPreviewActivity.class);
		intent.putExtra(MAN_PHOTO_URL, photoUrl);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_man_photo_preview);
		imageView = (TouchImageView)findViewById(R.id.imageView);
		progress = (MaterialProgressBar)findViewById(R.id.progress);
		buttonCancel = (ImageButton)findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		initData();
	}
	
	private void initData(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(MAN_PHOTO_URL)){
				mPhotoUrl = bundle.getString(MAN_PHOTO_URL);
			}
		}
		imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
		if(!TextUtils.isEmpty(mPhotoUrl)){
			mDownloader= new ImageViewLoader(this);
			progress.setVisibility(View.VISIBLE);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(mPhotoUrl);
			mDownloader.DisplayImage(imageView, mPhotoUrl, localPath, this);
		}else{
			progress.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		progress.setVisibility(View.GONE);
		switch (msg.what) {
		case GET_PHOTO_SUCCESS:
			
			break;
		case GET_PHOTO_FAILED:
			
			break;
		default:
			break;
		}
	}

	@Override
	public void OnDisplayNewImageFinish() {
		// TODO Auto-generated method stub
		sendEmptyUiMessage(GET_PHOTO_SUCCESS);
	}

	@Override
	public void OnLoadPhotoFailed() {
		// TODO Auto-generated method stub
		sendEmptyUiMessage(GET_PHOTO_FAILED);
	}
}
