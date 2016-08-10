package com.qpidnetwork.ladydating.album;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore.Video;
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
import com.qpidnetwork.ladydating.common.activity.PhoneVideoBrowserActivity;
import com.qpidnetwork.ladydating.common.activity.PhoneVideoBrowserFragment;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.DrawableUtil;
import com.qpidnetwork.ladydating.utility.ImageUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnEditAlbumVideoCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.RequestJniAlbum.ReviewReason;
import com.qpidnetwork.request.item.AlbumVideoItem;

public class EditVideoActivity extends BaseActionbarActivity implements OnEditAlbumVideoCallback{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.EDIT_VIDEO_ACTIVITY_CODE;
	
	private static final int EDIT_ALBUM_VIDEO = 1;
	private static final int OPTIMIZE_PHOTO_CALLBACK = 2;
	
	public static class IDs{
		public final static int video_error_dialog_button_ok = 0x00001f;
		public final static int video_error_dialog_button_cancel = 0x00002f;
		public final static int thumb_error_dialog_button_ok = 0x00003f;
		public final static int thumb_error_dialog_button_cancel = 0x00004f;
	}
	
	public static class ThumbnailKind{
		public final static int MicroKind = Video.Thumbnails.MICRO_KIND;   //Create 96 * 96 thumbnail.
		public final static int MiniKind = Video.Thumbnails.MINI_KIND;     //Create 512 * ? thumbnail.
		public final static int RawKind = 0x009f;                          //Create thumbnail with video original size.
		
	}
	
	public static enum EditType{
		ADD,
		EDIT
	}
	
	public static enum RequireEditType{
		DESCRIPTION,
		THUMBNAIL,
		BOTH
	}

	private static String EDIT_TYPE = "EDIT_TYPE";
	private static String ALBUM_ID = "ALBUM_ID";
	private static String ALBUM_NAME = "ALBUM_NAME";
	private static String ALBUM_VIDEO_TITLE = "ALBUM_VIDEO_TITLE";
	private static String ALBUM_VIDEO_REVIEW_REASON = "ALBUM_VIDEO_REVIEW_REASON";
	
	/**
	 * @param context
	 * @param albumId
	 * @param albumName
	 * @param albumVideoItem
	 * 
	 * 编辑视频
	 */
	public static void launchWithEditVideoMode(Context context,String albumId,String albumName,AlbumVideoItem albumVideoItem){
		if(!TextUtils.isEmpty(albumId)&&!TextUtils.isEmpty(albumName)&&albumVideoItem!=null){
			Intent intent = new Intent(context, EditVideoActivity.class);
			intent.putExtra(EDIT_TYPE, EditType.EDIT.toString());
			intent.putExtra(ALBUM_ID, albumId);
			intent.putExtra(ALBUM_NAME, albumName);
			intent.putExtra(ALBUM_VIDEO_TITLE, albumVideoItem.title);
			intent.putExtra(ALBUM_VIDEO_REVIEW_REASON, albumVideoItem.reviewReason.ordinal());
			((FragmentActivity) context).startActivity(intent);
		}
	}
	
	/**
	 * @param context
	 * @param videoItem
	 * 
	 * 添加视频
	 */
	public static void launchWithEditVideoMode(Context context,String albumId,String albumName, VideoItem videoItem){
		Intent intent = new Intent(context, EditVideoActivity.class);
		Bundle mBundle = new Bundle();  
		intent.putExtra(EDIT_TYPE, EditType.ADD.toString());
		intent.putExtra(ALBUM_ID, albumId);
		intent.putExtra(ALBUM_NAME, albumName);
        mBundle.putParcelable(PhoneVideoBrowserFragment.SELECT_VIDEO_ITEM, videoItem); 
		intent.putExtras(mBundle);
		((FragmentActivity) context).startActivity(intent);
	}
	
	
	
	private TextView tvAlbumName;
	private MaterialEditText videoDescription;
	private ImageView videoView;
	private TextView durationView;
	private ImageView thumbView;
	private TextView editionMessage;
	
	
	private EditType editType;//操作类型:添加|修改
	private String albumId;//相册id
	private String albumName;//相册名称
	private String videoTitle = "";//视频名称
	
	private String videoUri;//视频路径
	
	private ReviewReason mReviewReason = ReviewReason.REASON_OTHERS;
	private Point attachmentViewSize;
	private String thumbnailUri;

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null ||!extras.containsKey(EDIT_TYPE) ||!extras.containsKey(ALBUM_ID) ||!extras.containsKey(ALBUM_NAME)){
			return ;
		}
		
		editType = EditType.valueOf(extras.getString(EDIT_TYPE));
		albumId = extras.getString(ALBUM_ID);
		albumName = extras.getString(ALBUM_NAME);
		if(extras.containsKey(ALBUM_VIDEO_TITLE)){
			videoTitle = extras.getString(ALBUM_VIDEO_TITLE);
		}
		
		if(extras.containsKey(ALBUM_VIDEO_REVIEW_REASON)){
			mReviewReason = ReviewReason.values()[extras.getInt(ALBUM_VIDEO_REVIEW_REASON)];
		}
		
		editionMessage = (TextView) findViewById(R.id.editionMessage);
		tvAlbumName = (TextView) findViewById(R.id.albumName);
		videoDescription = (MaterialEditText) findViewById(R.id.videoDescription);
		videoView = (ImageView) findViewById(R.id.video);
		durationView = (TextView) findViewById(R.id.durationView);
		thumbView = (ImageView) findViewById(R.id.thumbnial);
		tvAlbumName.setText(getString(R.string.album_name) + ": " + albumName);
		videoDescription.getEditText().setText(videoTitle);
		
		videoView.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
		thumbView.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
		setupAttachmentViewSize();
		
		if (editType == EditType.ADD){
			editionMessage.setVisibility(View.GONE);
			videoView.setOnClickListener(this);
			findViewById(R.id.videoViewContainer).setVisibility(View.VISIBLE);
			VideoItem videoItem = (VideoItem) extras.getParcelable(PhoneVideoBrowserFragment.SELECT_VIDEO_ITEM);
			
			//根据打回原因分别处理可编辑项目
			if(mReviewReason == ReviewReason.REASON_REVISED_DESC_NOSTANDARD){
				//描述不規範,仅能修改描述
				thumbView.setClickable(false);
			}else if(mReviewReason == ReviewReason.REASON_REVISED_COVER_NOSTANDARD){
				//仅能修改缩略图
				videoDescription.setFocusable(false);
				videoDescription.setEnabled(false);
			}else if(mReviewReason == ReviewReason.REASON_REVISED_COVERANDDESC_NOSTANDARD){
				//封面及描述均不規範,均可修改
			}else{
				//其他错误不能修改
				thumbView.setClickable(false);
				videoDescription.setFocusable(false);
				videoDescription.setEnabled(false);
			}
			
			if(videoItem!=null){
				verifyVideo(videoItem);
			}
		}else{
			editionMessage.setVisibility(View.VISIBLE);
			findViewById(R.id.videoViewContainer).setVisibility(View.GONE);
			((TextView)findViewById(R.id.thumbRequirementDeclare)).setText(R.string.required);
		}
		
		//action bar
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle(getString(R.string.upload_video), getResources().getColor(R.color.text_color_dark));
		
		thumbView.setOnClickListener(this);
		videoView.setOnClickListener(this);

	}
	
	private void setupAttachmentViewSize(){
		attachmentViewSize = new Point();
		int width = (DeviceUtil.getScreenSize().x - Converter.dp2px(16 + 24 + 24 + 4)) / 2;
		int height = width / 16 * 12;
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		videoView.setLayoutParams(params);
		thumbView.setLayoutParams(params);
		attachmentViewSize.x = width;
		attachmentViewSize.y = height;
	}
	
	private boolean verifyVideo(VideoItem videoItem){
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(getString(R.string.length_of_the_video_should_be_between_8_15_seconds_please_choose_another_video));
		dialog.addButton(dialog.createButton(getString(R.string.ok), this, IDs.video_error_dialog_button_ok));
		dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
		dialog.setCancelable(false);
		
		
		if(videoItem.duraion < 8 || videoItem.duraion > 15){
			dialog.setMessage(getString(R.string.length_of_the_video_should_be_between_8_15_seconds_please_choose_another_video));
			dialog.show();
			return false;
		}else if(videoItem.size >= 40*1000*1000){
			dialog.setMessage(getString(R.string.album_video_size_too_large));
			dialog.show();
			return false;
		}else{
			if(createVideoThumbnail(videoItem.videoUri)){
				videoView.setScaleType(ScaleType.CENTER_CROP);
				String localPath = FileCacheManager.getInstance().CacheVideoThumbnailFromVideoUri(videoItem.videoUri);
				Bitmap bitmap = BitmapFactory.decodeFile(localPath, null);
				if(bitmap != null){
					videoView.setImageBitmap(bitmap);
				}
				thumbView.setScaleType(ScaleType.CENTER);
				thumbView.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
				durationView.setVisibility(View.VISIBLE);
				durationView.setText(videoItem.duraion + "s");
				this.videoUri = videoItem.videoUri;
				return true;
			}else{
				dialog.setMessage(getString(R.string.the_video_resolution_must_be_heigher_than_720px_please_choose_another_video));
				dialog.show();
				return false;
			}
		}
	}
	
	
	
	
	private boolean createVideoThumbnail(String videoUri){
		if(TextUtils.isEmpty(thumbnailUri)){
			//本来无需要生成
			Bitmap bmp = ThumbnailUtils.createVideoThumbnail(videoUri, ThumbnailKind.RawKind);
			
			if (bmp == null){
				return false;
			}else{
				if (bmp.getHeight() < 720 || bmp.getWidth() < 720) 
					return false;
				thumbnailUri = FileCacheManager.getInstance().GetTempPath() + System.currentTimeMillis() + ".jpg";
				boolean saved = FileUtil.writeBitmapToFile(bmp, thumbnailUri, 100, Bitmap.CompressFormat.JPEG);
				bmp.recycle();
				return saved;
			}
		}
		return true;
	}
	
	/** thumbnail size must be > 720 px **/
	private boolean verifyThumbnailInfo(String thumbUri){
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(thumbUri, options);
	    MaterialDialogAlert dialog = new MaterialDialogAlert(this);
	    if (options.outHeight < 720 || options.outWidth < 720){
			dialog.setMessage(getString(R.string.the_video_thumbnail_resolution_must_be_heigher_than_720px_please_choose_another_photo));
			dialog.addButton(dialog.createButton(getString(R.string.ok), this, IDs.thumb_error_dialog_button_ok));
			dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
			dialog.show();
			return false;
	    }
		
		Bitmap bmp = ImageUtil.decodeSampledBitmapFromFile(thumbUri, attachmentViewSize.x, attachmentViewSize.y);
		thumbView.setScaleType(ScaleType.CENTER_CROP);
		thumbView.setImageBitmap(bmp);
		thumbnailUri = thumbUri;
		
		return true;
				
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.video:
		case IDs.video_error_dialog_button_ok:
			PhoneVideoBrowserActivity.launch(this);
			break;
		case R.id.thumbnial:
		case IDs.thumb_error_dialog_button_ok:
			PhonePhotoBrowserActivity.launch(this);
			break;
			
		}
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_album_add_video;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	
	private void doAddVideo(){

		if (videoDescription.getEditText().getText().length() < 10 ||
				videoDescription.getEditText().getText().length() > 25){
			this.shakeView(videoDescription, true);
			videoDescription.setError(getString(R.string.english_letter_and_space_onley_between_12_to_25_characters));
			videoDescription.setErrorEnabled(true);
			return;
		}
		
		if (videoUri == null){
			this.shakeView(findViewById(R.id.videoViewContainer), true);
			return;
		}
		
		if (thumbnailUri == null){
			this.shakeView(findViewById(R.id.thumbViewContainer), true);
			return;
		}
		
		String title = videoDescription.getEditText().getText().toString();
		if(!TextUtils.isEmpty(albumId)&&!TextUtils.isEmpty(title)){
			showProgressDialog(getResources().getString(R.string.album_video_thumbnail_checking));
			OptimizePhotoBeforeSend();
//			VideoUploadManager.getInstance().UploadVideo(albumId, title, videoUri, thumbnailUri);
//			
//			//直接finish，不需要提示。
//			finish();
			
			//提示用户在通知栏查看进度
			/*MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setCancelable(false);
			dialog.setMessage(String.format(this.getResources().getString(R.string.album_video_upload_tips), title));
			dialog.addButton(dialog.createButton(this.getString(R.string.ok), new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			}));
			dialog.show();*/
		}
	}
	
	private void doEditVideo(){
			
			if (videoDescription.getEditText().getText().length() < 10 ||videoDescription.getEditText().getText().length() > 25){
				this.shakeView(videoDescription, true);
				videoDescription.setError(getString(R.string.english_letter_and_space_onley_between_12_to_25_characters));
				videoDescription.setErrorEnabled(true);
				return;
			}
		
			if (thumbnailUri == null){
				this.shakeView(findViewById(R.id.thumbViewContainer), true);
				return;
			}
			if(!TextUtils.isEmpty(albumId)&&!TextUtils.isEmpty(thumbnailUri)){
				showProgressDialog(getResources().getString(R.string.album_photo_summitting));
//				RequestJniAlbum.EditAlbumVideo(albumId, videoDescription.getEditText().getText().toString(), thumbnailUri, this);
				OptimizePhotoBeforeSend();
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
				if(!TextUtils.isEmpty(thumbnailUri)
						&& new File(thumbnailUri).exists()){
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(thumbnailUri, opts);
					if(opts.outHeight < 720 || opts.outHeight < 720){
						isCanSend = false;
					}
					
					//计算缩放可能性
					int inSampleSize = 1;
					if(isCanSend){
						 while(opts.outWidth/inSampleSize > 1080
								 || opts.outHeight/inSampleSize > 1080){
							 inSampleSize *= 2;
						 }
						 if(opts.outWidth/inSampleSize < 720
								 || opts.outHeight/inSampleSize < 720){
							 isCanSend = false;
						 }
					}
					
					//按照指定比例缩放后可用
					if(isCanSend){
						opts.inSampleSize = inSampleSize;
						opts.inJustDecodeBounds = false;
						Bitmap tempBitmap = BitmapFactory.decodeFile(thumbnailUri, opts);
						if(tempBitmap !=  null){
							thumbnailUri = FileCacheManager.getInstance().GetTempPath() + System.currentTimeMillis() + ".jpg";
							FileUtil.writeBitmapToFile(tempBitmap, thumbnailUri);
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
	
	private void doSubmit(){
		hideKeyboard();
		videoDescription.setErrorEnabled(false);
		
		if (editType == EditType.ADD){
			doAddVideo();
		}else{
			doEditVideo();
		}
		
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
		
		if (requestCode == PhoneVideoBrowserActivity.ACTIVITY_CODE){
			if (data == null ) return;
			VideoItem item = (VideoItem) data.getParcelableExtra(PhoneVideoBrowserFragment.SELECT_VIDEO_ITEM);
			if(item!=null){
				verifyVideo(item);
			}
		}
		
		if (requestCode == PhonePhotoBrowserActivity.ACTIVITY_CODE){
			if (data == null || data.getData() == null) return;
			verifyThumbnailInfo(data.getData().getPath());
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case EDIT_ALBUM_VIDEO:{
			dismissProgressDialog();
			RequestBaseResponse response = (RequestBaseResponse) msg.obj;
			if(response.isSuccess){//修改成功
				sendBroadcast(new Intent(AlbumDetailActivity.EDIT_ALBUM_VIDEO_REFRESH));
				finish();
			}else{
				if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4030")){
					//4030  视频不存在
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_video_edit_video_notexist_failed));
				}else if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4027")){
					//4027  视频封面MD5名称已存在
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_video_edit_thumb_exist_failed));
				}else if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4031")){
					//4031  视频不是“不通过”状态
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_video_edit_status_error));
				}else if(!TextUtils.isEmpty(response.errno)
						&& response.errno.equals("4033")){
					//4033  视频封面格式不正确(大小、类型)
					showConfirmNotifyDialog(this.getResources().getString(R.string.album_video_edit_thumb_format_failed));
				}else{
					//4029  没有修改数据
					//4032  视频封面上传失败
					//4034  视频修改失败
					//网络原因等普通错误
					showConfirmAndRetryNotifuDialog(this.getResources().getString(R.string.album_video_edit_normal_failed));
				}
			}
		}break;
			
		case OPTIMIZE_PHOTO_CALLBACK:{
			if(msg.arg1 == 1){
				if (editType == EditType.ADD){
					dismissProgressDialog();
					String title = videoDescription.getEditText().getText().toString();
					VideoUploadManager.getInstance().UploadVideo(albumId, title, videoUri, thumbnailUri);
					
					//直接finish，不需要提示。
					finish();
				}else{
					RequestJniAlbum.EditAlbumVideo(albumId, videoDescription.getEditText().getText().toString(), thumbnailUri, this);
				}
			}else{
				dismissProgressDialog();
				showConfirmNotifyDialog(this.getResources().getString(R.string.album_video_size_error));
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
	public void onEditAlbumVideo(boolean isSuccess, String errno, String errmsg) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = EDIT_ALBUM_VIDEO;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
		msg.obj = response;
		sendUiMessage(msg);
	}
}
