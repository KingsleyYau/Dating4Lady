package com.qpidnetwork.ladydating.album;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.common.activity.PhonePhotoBrowserActivity;
import com.qpidnetwork.ladydating.common.activity.PhoneVideoBrowserActivity;
import com.qpidnetwork.ladydating.common.activity.PhoneVideoBrowserFragment;
import com.qpidnetwork.ladydating.customized.view.FlatToast.StikyToastType;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.request.OnDeleteAlbumCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.RequestJniAlbum.AlbumType;
import com.qpidnetwork.request.item.AlbumListItem;

public class AlbumDetailActivity extends BaseActionbarActivity implements OnDeleteAlbumCallback{
	
	public static final int ACTIVITY_CODE = ActivityCodeUtil.ALBUM_DETAIL_ACTIVITY_CODE;
	public static final int ALBUM_DELETE = 1;
	
	public static String INPUT_ALBUM_NAME = "INPUT_ALBUM_NAME";
	public static String INPUT_ALBUM_ID = "INPUT_ALBUM_ID";
	public static String INPUT_ALBUM_TYPE = "INPUT_ALBUM_TYPE";
	public static String INPUT_ALBUM_DESCRIPTION = "INPUT_ALBUM_DESCRIPTION";
	
	public static final String EDIT_ALBUM_REFRESH="edit_album_refresh";//修改相册刷新
	public static final String EDIT_ALBUM_PHOTO_REFRESH="edit_album_photo_refresh";//修改图片刷新
	public static final String EDIT_ALBUM_VIDEO_REFRESH="edit_album_video_refresh";//修改视频刷新
	
	private BroadcastReceiver mBroadcastReceiver;
	
	public static void launch(Context context, AlbumListItem item){
		Intent intent = new Intent(context, AlbumDetailActivity.class);
		intent.putExtra(INPUT_ALBUM_NAME, item.title);
		intent.putExtra(INPUT_ALBUM_ID, item.id);
		intent.putExtra(INPUT_ALBUM_DESCRIPTION, item.desc);
		if(null != item.type)
			intent.putExtra(INPUT_ALBUM_TYPE, item.type.toString());
		context.startActivity(intent);
	}
	
	private AlbumPhotoListFragment albumPhotoListFragment;//图片相册列表
	private AlbumVideoListFragment albumVideoListFragment;//视频相册列表
	private String inputAlbumName = "";
	private String inputAlbumId = "";
	private AlbumType inputAlbumType;
	private String inputAlbumDescription = "";
	private TextView tvAlbumType;
	private RelativeLayout rlAddItem;
	
	//相册列表状态类型
	public static enum Category{
		PAST,
		UNDER_REVIEW,
		REQUIRED_EDIT,
		REJECTED
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		initReceiver();
		Bundle extras = getIntent().getExtras();
		
		if (extras == null ||!extras.containsKey(INPUT_ALBUM_NAME) ||!extras.containsKey(INPUT_ALBUM_ID) ||!extras.containsKey(INPUT_ALBUM_TYPE)){
//			throw new NullPointerException("No album name, album ID or album type.");
			return;
		}
		
		inputAlbumName = extras.getString(INPUT_ALBUM_NAME);
		inputAlbumId = extras.getString(INPUT_ALBUM_ID);
		inputAlbumType = AlbumType.valueOf(extras.getString(INPUT_ALBUM_TYPE));
		inputAlbumDescription = extras.getString(INPUT_ALBUM_DESCRIPTION);
		
		setActionBarName(inputAlbumName);
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		
		//根据类型设置显示
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		tvAlbumType = (TextView)this.findViewById(R.id.tvAlbumType);
		rlAddItem = (RelativeLayout) this.findViewById(R.id.rlAddItem);
		rlAddItem.setOnClickListener(this);
		if(inputAlbumType==AlbumType.Photo){
			albumPhotoListFragment = new AlbumPhotoListFragment();
			ft.replace(R.id.fragmentReplacement, albumPhotoListFragment);
			tvAlbumType.setText(getResources().getString(R.string.album_add_photo));
		}else if(inputAlbumType == AlbumType.Video){
			albumVideoListFragment = new AlbumVideoListFragment();
			ft.replace(R.id.fragmentReplacement, albumVideoListFragment);
			tvAlbumType.setText(getResources().getString(R.string.album_add_video));
		}
		ft.commit();
		
	}
	
	/**
	 * 注册广播
	 */
	private void initReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				switch (action) {
				case EDIT_ALBUM_REFRESH:{
					String albumName = intent.getStringExtra(INPUT_ALBUM_NAME);
					if(!TextUtils.isEmpty(albumName)){
						setActionBarName(albumName);
					}
					showAutoDismissToast(StikyToastType.DONE, "Done");
				}break;
				case EDIT_ALBUM_PHOTO_REFRESH:{
					AlbumPhotoListFragment photoFragment = (AlbumPhotoListFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentReplacement);
					QpidApplication.updateAlbumsNeed = true;
					if(photoFragment!=null){
						photoFragment.QueryAlbumItem();
						showAutoDismissToast(StikyToastType.DONE, "Done");
					}
				}break;
				case EDIT_ALBUM_VIDEO_REFRESH:{
					AlbumVideoListFragment videofragment = (AlbumVideoListFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentReplacement);
					QpidApplication.updateAlbumsNeed = true;
					if(videofragment!=null){
						videofragment.QueryAlbumItem();
						showAutoDismissToast(StikyToastType.DONE, "Done");
					}
				}break;
				default:
					break;
				}
				
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(EDIT_ALBUM_REFRESH);
		filter.addAction(EDIT_ALBUM_PHOTO_REFRESH);
		filter.addAction(EDIT_ALBUM_VIDEO_REFRESH);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	
	/**
	 * @param albumName
	 * 设置actionBar title
	 */
	protected void setActionBarName(String albumName) {
		// TODO Auto-generated method stub
		this.setActionbarTitle(albumName, getResources().getColor(R.color.text_color_dark));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.rlAddItem:
			if (inputAlbumType == AlbumType.Photo){
				PhonePhotoBrowserActivity.launch(this);
			}else{
				PhoneVideoBrowserActivity.launch(this);
			}
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
		case R.id.editAlbum://Album编辑
			AlbumEditActivity.launchWithEditMode(this, AlbumType.valueOf(inputAlbumType.toString()), inputAlbumId, inputAlbumName, inputAlbumDescription);
			break;
		case R.id.deleteAlbum:
			AlbumDelete();//Album删除
			break;
		default:
		}
	}

	/**
	 * 删除Album
	 */
	private void AlbumDelete() {
		// TODO Auto-generated method stub
		if((albumPhotoListFragment != null && !albumPhotoListFragment.isEmpty())
				||(albumVideoListFragment != null && !albumVideoListFragment.isEmpty())){//不能删除有项目的相册
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(this.getString(R.string.album_delete_dialog));
			dialog.addButton(dialog.createButton(this.getString(R.string.ok), null));
			dialog.show();
		}else{
			showProgressDialog(getResources().getString(R.string.album_deleting));
			if(!TextUtils.isEmpty(inputAlbumId)&&inputAlbumType!=null){
				RequestJniAlbum.DeleteAlbum(inputAlbumId, inputAlbumType, this);
			}
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
			EditPhotoActivity.launchWithAddPhotoMode(this,inputAlbumId,inputAlbumName, data.getData().getPath());
		}
		
		if (requestCode == PhoneVideoBrowserActivity.ACTIVITY_CODE){
			if (data == null ) return;
			VideoItem item = (VideoItem) data.getParcelableExtra(PhoneVideoBrowserFragment.SELECT_VIDEO_ITEM);
			if(item!=null){
				EditVideoActivity.launchWithEditVideoMode(this,inputAlbumId,inputAlbumName,item);
			}
		}
		
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case ALBUM_DELETE:{
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if(response.isSuccess){//删除成功
				this.dismissProgressDialog();
				QpidApplication.updateAlbumsNeed = true;
				finish();
			}else{//删除失败
				if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4013")){
					//4013  相册非空
					MaterialDialogAlert dialog = new MaterialDialogAlert(this);
					dialog.setMessage(this.getString(R.string.album_delete_dialog));
					dialog.addButton(dialog.createButton(this.getString(R.string.ok), null));
					dialog.show();
				}else{
					//4014  删除相册失败 及其他异常
					MaterialDialogAlert dialog = new MaterialDialogAlert(this);
					dialog.setMessage(this.getString(R.string.album_delete_normal_error));
					dialog.addButton(dialog.createButton(this.getString(R.string.retry), new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							AlbumDelete();
						}
					}));
					dialog.addButton(dialog.createButton(this.getString(R.string.cancel), null));
					dialog.show();
				}
			}
		}break;

		default:
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}
	



	@Override
	public void onDeleteAlbum(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		msg.what = ALBUM_DELETE;
		msg.obj = response;
		sendUiMessage(msg);//5130jh12
	}

	
}
