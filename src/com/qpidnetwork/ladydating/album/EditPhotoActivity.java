package com.qpidnetwork.ladydating.album;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.util.FileUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.common.activity.PhonePhotoBrowserActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.ImageUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnEditAlbumPhotoCallback;
import com.qpidnetwork.request.OnSaveAlbumPhotoCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.item.AlbumPhotoItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class EditPhotoActivity extends BaseActionbarActivity implements OnEditAlbumPhotoCallback, OnSaveAlbumPhotoCallback{
	
	public static final int ACTIVITY_CODE = ActivityCodeUtil.EDIT_PHOTO_ACTIVITY_CODE;
	
	private ImageViewLoader mDownloader;
	
	private static final int EDIT_ALBUM_PHOTO = 1;
	private static final int OPTIMIZE_PHOTO_CALLBACK = 2;
	
	public static String ALBUM_PHOTO_ITEM = "album_photo_item";
	
	public static class IDs{
		public final static int photo_error_dialog_button_ok = 0x00001f;
		public final static int photo_error_dialog_button_cancel = 0x00002f;
	}

	private static String EDIT_TYPE = "EDIT_TYPE";
	private static String ALBUM_ID = "ALBUM_ID";
	private static String ALBUM_NAME = "ALBUM_NAME";
	private static String ALBUM_PHOTO_TITLE = "ALBUM_PHOTO_TITLE";
	private static String ALBUM_PHOTO_URI = "ALBUM_PHOTO_URI";
	private static String ALBUM_PHOTO_ID = "ALBUM_PHOTO_ID";
	
	

	public static void launchWithAddPhotoMode(Context context,String albumId, String albumName, String photoUri){
		if(!TextUtils.isEmpty(albumId)&&!TextUtils.isEmpty(albumName)&&!TextUtils.isEmpty(photoUri)){
			Intent intent = new Intent(context, EditPhotoActivity.class);
			intent.putExtra(EDIT_TYPE, EditType.ADD.toString());
			intent.putExtra(ALBUM_ID, albumId);
			intent.putExtra(ALBUM_NAME, albumName);
			intent.putExtra(ALBUM_PHOTO_URI, photoUri);
			((FragmentActivity) context).startActivity(intent);
		}
	}
	
	
	public static void launchWithEditPhotoMode(Context context,String albumId,String albumName, AlbumPhotoItem albumPhotoItem){
		if(!TextUtils.isEmpty(albumId)&&!TextUtils.isEmpty(albumName)&&albumPhotoItem!=null){
			Intent intent = new Intent(context, EditPhotoActivity.class);
			intent.putExtra(EDIT_TYPE, EditType.EDIT.toString());
			intent.putExtra(ALBUM_ID, albumId);
			intent.putExtra(ALBUM_NAME, albumName);
			intent.putExtra(ALBUM_PHOTO_ID, albumPhotoItem.id);
			intent.putExtra(ALBUM_PHOTO_TITLE, albumPhotoItem.title);
			intent.putExtra(ALBUM_PHOTO_URI, albumPhotoItem.url);
			((FragmentActivity) context).startActivity(intent);
		}
	}
	
	public static enum EditType{
		ADD,
		EDIT
	}
	
	private TextView tvAlbumName;//相册名称
	private MaterialEditText photoDescription;//照片描述/photoTitle
	private ImageView photo;
	private TextView editionMessage;//提示信息
	
	private EditType editType;
	private String albumId;
	private String albumName;
	private String photoTitle = "";
	private String photoUri;
	private String photoId = "";
	
	private Point attachmentViewSize;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null ||!extras.containsKey(EDIT_TYPE) ||!extras.containsKey(ALBUM_ID) ||!extras.containsKey(ALBUM_NAME)||!extras.containsKey(ALBUM_PHOTO_URI)){
			return ;
		}
		
		editType = EditType.valueOf(extras.getString(EDIT_TYPE));
		albumId = extras.getString(ALBUM_ID);
		albumName = extras.getString(ALBUM_NAME);
		photoUri = extras.getString(ALBUM_PHOTO_URI);
		
		if(extras.containsKey(ALBUM_PHOTO_TITLE)){
			photoTitle = extras.getString(ALBUM_PHOTO_TITLE);
		}
		
		if(extras.containsKey(ALBUM_PHOTO_ID)){
			photoId = extras.getString(ALBUM_PHOTO_ID);
		}
		
		editionMessage = (TextView) findViewById(R.id.editionMessage);
		tvAlbumName = (TextView) findViewById(R.id.albumName);
		photoDescription = (MaterialEditText) findViewById(R.id.photoDescription);
		photo = (ImageView) findViewById(R.id.photo);
		
		setupAttachmentViewSize();
		
		if (editType == EditType.ADD){
			editionMessage.setVisibility(View.GONE);
			photo.setOnClickListener(this);
			verifyPhotoInfo(photoUri);
		}else{
			editionMessage.setVisibility(View.VISIBLE);
		}
		
//		photo.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
		if(!TextUtils.isEmpty(photoUri)){
			mDownloader = new ImageViewLoader(this);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(photoUri);
			mDownloader.DisplayImage(photo, photoUri, localPath, null);
		}
		
		
		tvAlbumName.setText(getString(R.string.album_name) + ": " + albumName);
		photoDescription.getEditText().setText(photoTitle);
		
		
		
		//action bar
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle(getString(R.string.upload_photo), getResources().getColor(R.color.text_color_dark));
		
		
	}
	
	private void setupAttachmentViewSize(){
		
		attachmentViewSize = new Point();
		int width = (DeviceUtil.getScreenSize().x - Converter.dp2px(24 + 24));
		int height = width / 16 * 12;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		photo.setLayoutParams(params);
		attachmentViewSize.x = width;
		attachmentViewSize.y = height;

	}
	
	/** thumbnail size must be > 720 px **/
	private boolean verifyPhotoInfo(String photoUri){
		//检测图片是否Jpg
		if(!FileUtil.getMimeType(new File(photoUri)).equals("image/jpeg")){
	    	MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.the_photo_type_must_be_jpg));
			dialog.addButton(dialog.createButton(getString(R.string.ok), this, IDs.photo_error_dialog_button_ok));
			dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
			dialog.show();
			return false;
		}
		
		//检测图片宽高
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(photoUri, options);
	    if (options.outHeight < 800 || options.outWidth < 800){
	    	MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.the_photo_resolution_must_be_heigher_than_800px_please_choose_another_photo));
			dialog.addButton(dialog.createButton(getString(R.string.ok), this, IDs.photo_error_dialog_button_ok));
			dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
			dialog.show();
			return false;
	    }
		
	    this.photoUri = photoUri;
	    photo.setScaleType(ScaleType.CENTER_CROP);
		photo.setImageBitmap(ImageUtil.decodeSampledBitmapFromFile(photoUri, attachmentViewSize.x, attachmentViewSize.y));
		
		
		return true;
				
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.photo:
		case IDs.photo_error_dialog_button_ok:
			PhonePhotoBrowserActivity.launch(this);
			break;
		}
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_album_add_photo;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	private void doSubmit(){
		
		hideKeyboard();
		photoDescription.setErrorEnabled(false);
		
		if (photoDescription.getEditText().getText().length() < 10 ||
				photoDescription.getEditText().getText().length() > 25){
			this.shakeView(photoDescription, true);
			photoDescription.setError(getString(R.string.english_letter_and_space_onley_between_12_to_25_characters));
			photoDescription.setErrorEnabled(true);
			return;
		}
		
		if (photoTitle.equals(photoDescription.getEditText().getText().toString())){
			this.shakeView(photoDescription, true);
			photoDescription.setError(getString(R.string.your_photo_description_has_not_been_changed_yet));
			photoDescription.setErrorEnabled(true);
			return;
		}
		
		if (editType == EditType.ADD && photoUri == null){
			this.shakeView(findViewById(R.id.photoViewContainer), true);
			return;
		}
		
		showProgressDialog(getResources().getString(R.string.album_photo_summitting));
		
		if(editType == EditType.EDIT){
			RequestJniAlbum.EditAlbumPhoto(photoId, photoDescription.getEditText().getText().toString(), this);
		}else{
			OptimizePhotoBeforeSend();
//			RequestJniAlbum.AddAlbumPhoto(albumId, photoDescription.getEditText().getText().toString(), photoUri, this);
		}
	}
	
	/**
	 * 照片宽/高必须在800-3200之间（太小或则尺寸过于异常时）
	 */
	private void OptimizePhotoBeforeSend(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean isCanSend = true;
				if(!TextUtils.isEmpty(photoUri)
						&& new File(photoUri).exists()){
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(photoUri, opts);
					if(opts.outHeight < 800 || opts.outHeight <800){
						isCanSend = false;
					}
					
					//计算缩放可能性
					int inSampleSize = 1;
					if(isCanSend){
						 while(opts.outWidth/inSampleSize > 3200
								 || opts.outHeight/inSampleSize >3200){
							 inSampleSize *= 2;
						 }
						 if(opts.outWidth/inSampleSize < 800
								 || opts.outHeight/inSampleSize < 800){
							 isCanSend = false;
						 }
					}
					
					//按照指定比例缩放后可用
					if(isCanSend){
						opts.inSampleSize = inSampleSize;
						opts.inJustDecodeBounds = false;
						Bitmap tempBitmap = BitmapFactory.decodeFile(photoUri, opts);
						if(tempBitmap !=  null){
							photoUri = FileCacheManager.getInstance().GetTempPath() + System.currentTimeMillis() + ".jpg";
							FileUtil.writeBitmapToFile(tempBitmap, photoUri);
							tempBitmap.recycle();
						}else{
							isCanSend = false;
						}
					}
					
					Message msg = Message.obtain();
					msg.what = OPTIMIZE_PHOTO_CALLBACK;
					msg.arg1 = isCanSend?1:0;
					sendUiMessage(msg);
				}				
			}
		}).start();
	}
	
	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch(menu.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.done:
			doSubmit();
			break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.done, menu);
		return true;
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		
		if (requestCode == PhonePhotoBrowserActivity.ACTIVITY_CODE){
			if (data == null || data.getData() == null) return;
			verifyPhotoInfo(data.getData().getPath());
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case EDIT_ALBUM_PHOTO:{
			dismissProgressDialog();
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if(response.isSuccess){
				sendBroadcast(new Intent(AlbumDetailActivity.EDIT_ALBUM_PHOTO_REFRESH));
				finish();
			}else{
				if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4015")){
					//4015 相册超过30张照片
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_photo_over_thirty_error));
				}else if(!TextUtils.isEmpty(response.errno)
						&& (response.errno.equals("4022")
								|| response.errno.equals("4023"))){
					//4022  女士没有上传照片权限
					//4023  机构没有上传照片权限
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_photo_no_permission_error));
				}else if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4017")){
					//4017  照片md5名称已存在该相册
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_photo_existed_error));
				}else if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4020")){
					//4020  照片不是“不通过”状态
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_photo_edit_status_error));
				}else if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4021")){
					//4021  照片不存在
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_photo_eidt_notexist_error));
				}else{
					//4016  上传照片失败(移动照片)
					//4018  添加照片失败(插库异常)
					//4019  修改照片失败(编辑照片插库异常)
					//网络原因等普通错误
					showConfirmAndRetryNotifuDialog(this.getResources().getString(R.string.album_photo_normal_error));
				}
			}
		}break;
		
		case OPTIMIZE_PHOTO_CALLBACK:{
			//发送前优化照片回调
			if(msg.arg1 == 1){
				RequestJniAlbum.AddAlbumPhoto(albumId, photoDescription.getEditText().getText().toString(), photoUri, this);
			}else{
				dismissProgressDialog();
				showConfirmNotifyDialog(this.getResources().getString(R.string.album_photo_size_error));
			}
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 仅仅提示Dialog
	 * @param description
	 */
	private void showConfirmNotifyDialog(String description){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(description);
		dialog.addButton(dialog.createButton(this.getString(R.string.ok), null));
		if(isActivityVisible()){
			dialog.show();
		}
	}
	
	/**
	 * 
	 * @param description
	 */
	private void showConfirmAndRetryNotifuDialog(String description){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(description);
		dialog.addButton(dialog.createButton(this.getString(R.string.retry), new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doSubmit();
			}
		}));
		dialog.addButton(dialog.createButton(this.getString(R.string.cancel), null));
		if(isActivityVisible()){
			dialog.show();
		}
	}


	@Override
	public void onEditAlbumPhoto(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = EDIT_ALBUM_PHOTO;
		RequestBaseResponse response  = new RequestBaseResponse(isSuccess, errno, errmsg, null); 
		msg.obj = response;
		sendUiMessage(msg);
	}


	@Override
	public void onSaveAlbumPhoto(boolean isSuccess, String errno,
			String errmsg, String photoId) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = EDIT_ALBUM_PHOTO;
		RequestBaseResponse response  = new RequestBaseResponse(isSuccess, errno, errmsg, null); 
		msg.obj = response;
		sendUiMessage(msg);
	}

}
