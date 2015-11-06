package com.qpidnetwork.ladydating.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.common.activity.PhonePhotoBrowserActivity;
import com.qpidnetwork.ladydating.common.activity.PhoneVideoBrowserActivity;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.UriUtil;

public class AlbumDetailActivity extends BaseActionbarActivity{
	
	public static final int ACTIVITY_CODE = ActivityCodeUtil.ALBUM_DETAIL_ACTIVITY_CODE;
	
	public static String INPUT_ALBUM_NAME = "INPUT_ALBUM_NAME";
	public static String INPUT_ALBUM_ID = "INPUT_ALBUM_ID";
	public static String INPUT_ALBUM_TYPE = "INPUT_ALBUM_TYPE";
	public static String INPUT_ALBUM_DESCRIPTION = "INPUT_ALBUM_DESCRIPTION";
	
	public static void launch(Context context, String albumName, String albumId, AlbumType albumType, String albumDescription){
		Intent intent = new Intent(context, AlbumDetailActivity.class);
		intent.putExtra(INPUT_ALBUM_NAME, albumName);
		intent.putExtra(INPUT_ALBUM_ID, albumId);
		intent.putExtra(INPUT_ALBUM_TYPE, albumType.toString());
		intent.putExtra(INPUT_ALBUM_DESCRIPTION, albumDescription);
		context.startActivity(intent);
	}
	
	
	public static enum AlbumType{
		PHOTO,
		VIDEO
	}
	
	private FloatingActionButton addPhoto;
	
	private AlbumDetailListFragment albumItemListFragment;
	private String inputAlbumName = "";
	private String inputAlbumId = "";
	private AlbumType inputAlbumType;
	private String inputAlbumDescription = "";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		
		if (extras == null ||
			!extras.containsKey(INPUT_ALBUM_NAME) ||
			!extras.containsKey(INPUT_ALBUM_ID) ||
			!extras.containsKey(INPUT_ALBUM_TYPE)){
			throw new NullPointerException("No album name, album ID or album type.");
		}
		
		inputAlbumName = extras.getString(INPUT_ALBUM_NAME);
		inputAlbumId = extras.getString(INPUT_ALBUM_ID);
		inputAlbumType = AlbumType.valueOf(extras.getString(INPUT_ALBUM_TYPE));
		inputAlbumDescription = extras.getString(INPUT_ALBUM_DESCRIPTION);
		
		this.setActionbarTitle(inputAlbumName, getResources().getColor(R.color.text_color_dark));
		
		
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		
		albumItemListFragment = new AlbumDetailListFragment();
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragmentReplacement, albumItemListFragment);
		ft.commit();
		
		addPhoto = (FloatingActionButton) findViewById(R.id.add);
		addPhoto.setOnClickListener(this);
		
	}

	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.add:
			if (inputAlbumType == AlbumType.PHOTO){
				PhonePhotoBrowserActivity.launch(this);
			}else{
				PhoneVideoBrowserActivity.launch(this);
			}
			
			//EditVideoActivity.launchWithAddPhotoMode(this, "album name bla", "");
			break;
		default:
		}
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_album_detail_list;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch(menu.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.editAlbum:
			AlbumEditActivity.launchWithEditMode(this, AlbumEditActivity.AlbumType.valueOf(inputAlbumType.toString()), inputAlbumId, inputAlbumName, inputAlbumDescription);
			break;
		default:
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.delete_and_edit_album, menu);
		return true;
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		
		if (requestCode == PhonePhotoBrowserActivity.ACTIVITY_CODE){
			if (data == null || data.getData() == null) return;
			EditPhotoActivity.launchWithAddPhotoMode(this, inputAlbumName, data.getData().getPath());
		}
		
		if (requestCode == PhoneVideoBrowserActivity.ACTIVITY_CODE){
			if (data == null || data.getData() == null) return;
			EditVideoActivity.launchWithAddVideoMode(this, inputAlbumName, data.getData().getPath());
		}
		
		if (requestCode == EditPhotoActivity.ACTIVITY_CODE){
			albumItemListFragment.onAddPhotoActivityCallback(data);
		}
		
		if (requestCode == EditVideoActivity.ACTIVITY_CODE){
			albumItemListFragment.onAddPhotoActivityCallback(data);
		}
	}
	
}
