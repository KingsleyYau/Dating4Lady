package com.qpidnetwork.ladydating.album;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.customized.view.ViewPagerFixed;
import com.qpidnetwork.request.item.AlbumPhotoItem;
import com.qpidnetwork.request.item.AlbumVideoItem;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-23
 */
public class AlbumPreviewActivity extends BaseFragmentActivity implements OnClickListener{

	public static final String ALBUM_PHOTO = "album_photo";
	public static final String ALBUM_VIDEO = "album_video";
	public static final String PREVIEW_TYPE = "preview_type";
	public static final String CURRENT_SELECT_POSITION = "currPosition";
	
	private ArrayList<AlbumPhotoItem> mAlbumPhotoList;
	private ArrayList<AlbumVideoItem> mAlbumVideoList;
	private PreviewType previewType;
	private AlbumPreviewAdapter mAdapter;
	private int currPosition = 0;
	
	public enum PreviewType{
		PhotoPreview,VideoPreview
	}
	
	private ViewPagerFixed mViewPager;
	private ImageButton buttonCancel;
	
	public static void launchNoramlPhotoActivity(Context context, ArrayList<AlbumPhotoItem> albumPhotoList,int currPosition) {
		Intent intent = new Intent(context, AlbumPreviewActivity.class);
		intent.putExtra(ALBUM_PHOTO, (Serializable)albumPhotoList);
		intent.putExtra(PREVIEW_TYPE, PreviewType.PhotoPreview.ordinal());
		intent.putExtra(CURRENT_SELECT_POSITION, currPosition);
		context.startActivity(intent);
	}
	
	public static void launchNoramlVideoActivity(Context context, ArrayList<AlbumVideoItem> albumVideoList,int currPosition){
		Intent intent = new Intent(context, AlbumPreviewActivity.class);
		intent.putExtra(ALBUM_VIDEO, (Serializable)albumVideoList);
		intent.putExtra(PREVIEW_TYPE, PreviewType.VideoPreview.ordinal());
		intent.putExtra(CURRENT_SELECT_POSITION, currPosition);
		context.startActivity(intent);
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_privatephoto_preview);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		
		mAlbumPhotoList = new ArrayList<>();
		mAlbumVideoList = new ArrayList<>();
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(PREVIEW_TYPE)){
				previewType = PreviewType.values()[bundle.getInt(PREVIEW_TYPE)];
			}
			if (bundle.containsKey(ALBUM_PHOTO)) {
				mAlbumPhotoList = (ArrayList<AlbumPhotoItem>) bundle.getSerializable(ALBUM_PHOTO);
			}
			if (bundle.containsKey(ALBUM_VIDEO)) {
				mAlbumVideoList = (ArrayList<AlbumVideoItem>) bundle.getSerializable(ALBUM_VIDEO);
			}
			if (bundle.containsKey(CURRENT_SELECT_POSITION)){
				currPosition = bundle.getInt(CURRENT_SELECT_POSITION);
			}
		}
		
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(this);
		mAdapter = new AlbumPreviewAdapter(this, previewType, mAlbumPhotoList, mAlbumVideoList);
		mViewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(currPosition);
		
		if (Build.VERSION.SDK_INT >= 21) {
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this,48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this,48);
			((RelativeLayout.LayoutParams) buttonCancel.getLayoutParams()).topMargin = UnitConversion.dip2px(this, 18);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonCancel:
			finish();
			break;
		}
	}

}
