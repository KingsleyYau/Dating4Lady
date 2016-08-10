package com.qpidnetwork.ladydating.album;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.request.OnCreateAlbumCallback;
import com.qpidnetwork.request.OnEditAlbumCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.RequestJniAlbum.AlbumType;

public class AlbumEditActivity extends BaseActionbarActivity implements MaterialDropDownMenu.OnClickCallback,OnCreateAlbumCallback,OnEditAlbumCallback{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.ALBUM_EDIT_ACTIVITY_CODE;
	public static final int CREATE_ALBUM = 1;
	public static final int EDIT_ALBUM = 2;
	
	public static String INPUT_EDIT_TYPE = "INPUT_EDIT_TYPE";
	public static String INPUT_ALBUM_ID = "INPUT_ALBUM_ID";
	public static String INPUT_ALBUM_TYPE = "INPUT_ALBUM_TYPE";
	public static String INPUT_ALBUM_NAME = "INPUT_ALBUM_NAME";
	public static String INPUT_ALBUM_DESCRIPTION = "INPUT_ALBUM_DESCRIPTION";
	public static String INPUT_ALBUM_LISTENER = "INPUT_ALBUM_LISTENER";
	
	public static enum EditType{
		CREATE,
		EDIT
	}
	
	
	
	public static void launch(Context context){
		Intent intent = new Intent(context, AlbumEditActivity.class);
		intent.putExtra(INPUT_EDIT_TYPE, EditType.CREATE.toString());
		context.startActivity(intent);
	}
	
	public static void launchWithEditMode(Context context, AlbumType albumType, String albumId, String albumName, String albumDescription){
		Intent intent = new Intent(context, AlbumEditActivity.class);
		intent.putExtra(INPUT_EDIT_TYPE, EditType.EDIT.toString());
		intent.putExtra(INPUT_ALBUM_TYPE, albumType.toString());
		intent.putExtra(INPUT_ALBUM_ID, albumId);
		intent.putExtra(INPUT_ALBUM_NAME, albumName);
		intent.putExtra(INPUT_ALBUM_DESCRIPTION, albumDescription);
		context.startActivity(intent);
	}
	
	
	private EditType inputEditType = EditType.CREATE;
	private AlbumType inputAlbumType;
	private String inputAlbumId;
	private String inputAlbumName;
	private String inputAlbumDescription;
	
	private MaterialDropDownMenu albumTypeSelectMenu;
	private TextView albumType;
	private MaterialEditText albumName;
	private MaterialEditText albumDescription;
	private String[] albumTypeMenuText;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			if (extras.containsKey(INPUT_EDIT_TYPE)) inputEditType = EditType.valueOf(extras.getString(INPUT_EDIT_TYPE));
			if (extras.containsKey(INPUT_ALBUM_TYPE)) inputAlbumType = AlbumType.valueOf(extras.getString(INPUT_ALBUM_TYPE));
			if (extras.containsKey(INPUT_ALBUM_ID)) inputAlbumId = extras.getString(INPUT_ALBUM_ID);
			if (extras.containsKey(INPUT_ALBUM_NAME)) inputAlbumName = extras.getString(INPUT_ALBUM_NAME);
			if (extras.containsKey(INPUT_ALBUM_DESCRIPTION)) inputAlbumDescription = extras.getString(INPUT_ALBUM_DESCRIPTION);
		}
		
		this.setActionbarTitle((inputEditType == EditType.CREATE) ? R.string.create_album : R.string.edit_album, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		
		albumTypeMenuText = new String[]{getString(R.string.photo_album), getString(R.string.video_album)};
		albumType = (TextView) findViewById(R.id.albumType);
		albumName = (MaterialEditText) findViewById(R.id.albumName);
		albumDescription = (MaterialEditText) findViewById(R.id.albumDescription);

		setupView();
	}
	
	private void setupView(){
		if (inputEditType == EditType.EDIT){
			if(inputAlbumType != null){
				if(inputAlbumType == AlbumType.Photo){
					albumType.setText(albumTypeMenuText[0]);
				}else if(inputAlbumType == AlbumType.Video){
					albumType.setText(albumTypeMenuText[1]);
				}
			}
			if (inputAlbumName != null)albumName.getEditText().setText(inputAlbumName);
			if (albumDescription != null)albumDescription.getEditText().setText(inputAlbumDescription);
		}else{
			Point menuSize = new Point(Converter.dp2px(180), LayoutParams.WRAP_CONTENT);
			albumTypeSelectMenu = new MaterialDropDownMenu(this, albumTypeMenuText, this, menuSize);
			albumType.setOnClickListener(this);
		}
	}
	
	private void doSubmit(){
		
		albumName.setErrorEnabled(false);
		albumDescription.setErrorEnabled(false);
		String album_name = albumName.getEditText().getText().toString();
		String album_desc = albumDescription.getEditText().getText().toString();
		
		if (inputAlbumType == null){
			shakeView(albumType, true);
			albumType.setTextColor(Color.RED);
			return;
		}
		
		if (album_name.length() == 0){
			shakeView(albumName, true);
			albumName.setError(getString(R.string.required));
			albumName.setErrorEnabled(true);
			return;
		}
		
		if (album_desc.length() == 0){
			shakeView(albumDescription, true);
			albumDescription.setError(getString(R.string.required));
			albumDescription.setErrorEnabled(true);
			return;
		}
		
		
		if(inputEditType == EditType.CREATE){
			this.showProgressDialog(getResources().getString(R.string.album_creating));
			RequestJniAlbum.CreateAlbum(inputAlbumType, album_name, album_desc,this);
		}else if(inputEditType == EditType.EDIT){
			this.showProgressDialog(getResources().getString(R.string.album_edit_processing));
			RequestJniAlbum.EditAlbum(inputAlbumId, inputAlbumType, album_name, album_desc, this);
		}
		
		
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		case R.id.albumType:
			albumTypeSelectMenu.showAsDropDown(v);
			break;
		}
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_edit_album;
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
		case R.id.done:
			doSubmit();
			break;
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
	}
	
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		this.dismissProgressDialog();
		RequestBaseResponse response = (RequestBaseResponse) msg.obj;
		switch (msg.what) {
		case CREATE_ALBUM:
			if(response.isSuccess){
				QpidApplication.updateAlbumsNeed = true;
				finish();
			}else{//创建失败待续
				if(isActivityVisible()){
					if(!TextUtils.isEmpty(response.errno)
							&& response.errno.equals("4010")){
						//相册已存在
						MaterialDialogAlert dialog = new MaterialDialogAlert(this);
						dialog.setMessage(this.getString(R.string.album_create_edit_error_existed));
						dialog.addButton(dialog.createButton(this.getString(R.string.ok), null));
						dialog.show();
					}else{
						//4011  创建相册失败	或 其他异常
						MaterialDialogAlert dialog = new MaterialDialogAlert(this);
						dialog.setMessage(this.getString(R.string.album_create_normal_error));
						dialog.addButton(dialog.createButton(this.getString(R.string.retry), new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								doSubmit();
							}
						}));
						dialog.addButton(dialog.createButton(this.getString(R.string.cancel), null));
						dialog.show();
					}
				}
			}
			break;
		case EDIT_ALBUM:
			if(response.isSuccess){
				QpidApplication.updateAlbumsNeed = true;
				finish();
				sendBroadRefresh();
			}else{//编辑失败
				if(isActivityVisible()){
					if(!TextUtils.isEmpty(response.errno)
							&& response.errno.equals("4010")){
						//相册已存在
						MaterialDialogAlert dialog = new MaterialDialogAlert(this);
						dialog.setMessage(this.getString(R.string.album_create_edit_error_existed));
						dialog.addButton(dialog.createButton(this.getString(R.string.ok), null));
						dialog.show();
					}else{
						//4012  编辑相册失败	或 其他异常
						MaterialDialogAlert dialog = new MaterialDialogAlert(this);
						dialog.setMessage(this.getString(R.string.album_edit_normal_error));
						dialog.addButton(dialog.createButton(this.getString(R.string.retry), new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								doSubmit();
							}
						}));
						dialog.addButton(dialog.createButton(this.getString(R.string.cancel), null));
						dialog.show();
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void sendBroadRefresh() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(AlbumDetailActivity.EDIT_ALBUM_REFRESH);
		intent.putExtra(AlbumDetailActivity.INPUT_ALBUM_NAME, albumName.getEditText().getText().toString());
		intent.putExtra(AlbumDetailActivity.INPUT_ALBUM_DESCRIPTION, albumDescription.getEditText().getText().toString());
		sendBroadcast(intent);
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.done, menu);
		return true;
	}

	
	/**
	 * Album type drop down menu onItemClick
	 */
	@Override
	public void onClick(AdapterView<?> adptView, View v, int which) {
		// TODO Auto-generated method stub
		if(which == 0){
			inputAlbumType = AlbumType.Photo;
			albumType.setText(albumTypeMenuText[0]);
		}else{
			inputAlbumType = AlbumType.Video;
			albumType.setText(albumTypeMenuText[1]);
		}
		albumType.setTextColor(getResources().getColor(R.color.text_color_dark));
	}

	@Override
	public void onCrateAlbum(boolean isSuccess, String errno, String errmsg,
			String albumId) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(isSuccess,errno,errmsg,albumId);
		msg.what = CREATE_ALBUM;
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void onEditAlbum(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(isSuccess,errno,errmsg,null);
		msg.what = EDIT_ALBUM;
		msg.obj = response;
		sendUiMessage(msg);
	}

}
