package com.qpidnetwork.ladydating.album;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.common.activity.PhonePhotoBrowserActivity;
import com.qpidnetwork.ladydating.common.activity.PhoneVideoBrowserActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.DrawableUtil;
import com.qpidnetwork.ladydating.utility.FileUtil;
import com.qpidnetwork.ladydating.utility.ImageUtil;
import com.qpidnetwork.manager.FileCacheManager;

public class EditVideoActivity extends BaseActionbarActivity{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.EDIT_VIDEO_ACTIVITY_CODE;
	
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

	public static String INPUT_EDIT_TYPE = "INPUT_EDIT_TYPE";
	public static String INPUT_ALBUM_NAME = "INPUT_ALBUM_NAME";
	public static String INPUT_EIDT_REQUIRE_TYPE = "INPUT_EIDT_REQUIRE_TYPE";
	public static String INPUT_VIDEO_DESCRIPTION = "INPUT_VIDEO_DESCRIPTION";
	public static String INPUT_VIDEO_URI = "INPUT_VIDEO_URI";
	public static String INPUT_THUMBNAIL_URI = "INPUT_THUMBNAIL_URI";
	public static String OUTPUT_PHOTO_URI = "OUTPUT_PHOTO_URI";
	public static String OUTPUT_PHOTO_DESCRIPTION  = "OUTPUT_PHOTO_DESCRIPTION";

	
	
	public static void launchWithAddVideoMode(Context context, String albumName, String videoUri){
		Intent intent = new Intent(context, EditVideoActivity.class);
		intent.putExtra(INPUT_EDIT_TYPE, EditType.ADD.toString());
		intent.putExtra(INPUT_ALBUM_NAME, albumName);
		intent.putExtra(INPUT_VIDEO_URI, videoUri);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	public static void launchWithEditVideoMode(Context context, String albumName, String photoDescription, RequireEditType requireEditType){
		Intent intent = new Intent(context, EditVideoActivity.class);
		intent.putExtra(INPUT_EDIT_TYPE, EditType.EDIT.toString());
		intent.putExtra(INPUT_ALBUM_NAME, albumName);
		intent.putExtra(INPUT_VIDEO_DESCRIPTION, photoDescription);
		intent.putExtra(INPUT_EIDT_REQUIRE_TYPE, requireEditType.toString());
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	
	
	private TextView albumName;
	private MaterialEditText videoDescription;
	private ImageView videoView;
	private TextView durationView;
	private ImageView thumbView;
	private TextView editionMessage;
	
	
	private EditType inputEditType;
	private String inputAlbumName;
	private String inputVideoDescription = "";
	private String inputVideoUri;
	
	private RequireEditType editRequireType;
	private Point attachmentViewSize;
	private String videoUri;
	private String thumbnailUri;

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null ||
			!extras.containsKey(INPUT_EDIT_TYPE) ||
			!extras.containsKey(INPUT_ALBUM_NAME)){
			throw new NullPointerException("Please don't launch this activity directly, please use launchWithAddPhotoMode() or launchWithEditPhotoMode() instead.");
		}
		
		inputEditType = EditType.valueOf(extras.getString(INPUT_EDIT_TYPE));
		
		if (extras.containsKey(INPUT_ALBUM_NAME))
			inputAlbumName = extras.getString(INPUT_ALBUM_NAME);
		
		if (extras.containsKey(INPUT_VIDEO_DESCRIPTION))
			inputVideoDescription = extras.getString(INPUT_VIDEO_DESCRIPTION);
			
		if (extras.containsKey(INPUT_VIDEO_URI))
			inputVideoUri = extras.getString(INPUT_VIDEO_URI);

		if (extras.containsKey(INPUT_EIDT_REQUIRE_TYPE))
			editRequireType = RequireEditType.valueOf(extras.getString(INPUT_EIDT_REQUIRE_TYPE));
		
		editionMessage = (TextView) findViewById(R.id.editionMessage);
		albumName = (TextView) findViewById(R.id.albumName);
		videoDescription = (MaterialEditText) findViewById(R.id.videoDescription);
		videoView = (ImageView) findViewById(R.id.video);
		durationView = (TextView) findViewById(R.id.durationView);
		thumbView = (ImageView) findViewById(R.id.thumbnial);
		albumName.setText(getString(R.string.album_name) + ": " + inputAlbumName);
		videoDescription.getEditText().setText(inputVideoDescription);
		
		videoView.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
		thumbView.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
		setupAttachmentViewSize();
		
		if (inputEditType == EditType.ADD){
			editionMessage.setVisibility(View.GONE);
			videoView.setOnClickListener(this);
			verifyVideoInfo(inputVideoUri);
		}else{
			editionMessage.setVisibility(View.VISIBLE);
			
			findViewById(R.id.videoViewContainer).setVisibility(View.GONE);
			((TextView)findViewById(R.id.thumbRequirementDeclare)).setText(R.string.required);
			
			switch(editRequireType){
			case DESCRIPTION:
				findViewById(R.id.thumbnial ).setVisibility(View.GONE);
				break;
			case THUMBNAIL:
				videoDescription.setEnabled(false);
				videoDescription.getEditText().setEnabled(false);
				break;
			default:
				break;
			}
			//Load photo from cache or from the cloud.
			
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
	
	

	private boolean verifyVideoInfo(String videoUri){
		
		MaterialDialogAlert dialog = new MaterialDialogAlert(this);
		dialog.setMessage(getString(R.string.length_of_the_video_should_be_between_8_18_seconds_please_choose_another_video));
		dialog.addButton(dialog.createButton(getString(R.string.ok), this, IDs.video_error_dialog_button_ok));
		dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
		dialog.setCancelable(false);
		
		ContentResolver cr = this.getContentResolver();
		String[] projection = new String[]{
				MediaStore.Video.VideoColumns.DATA,
				MediaStore.Video.VideoColumns.DURATION};
		
		String selection = projection[0] + " = ?";
		String[] selectionArr = new String[]{videoUri};
		Cursor cur = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArr, null);
		
		if (cur.moveToFirst()){
			
			/** Video is found in the media storage **/
			int duration = cur.getInt(cur.getColumnIndex(projection[1])) / 1000;
			cur.close();
			
			if (duration < 8 || duration > 18){
				/* Video length is not valid, it should be 8 - 18 seconds */
				dialog.setMessage(getString(R.string.length_of_the_video_should_be_between_8_18_seconds_please_choose_another_video));
				dialog.show();
				return false;
			}else{
				if (createVideoThumbnail(videoUri)){
					/* Video has a valid length and a thumbnail a created successfully */
					videoView.setScaleType(ScaleType.CENTER_CROP);
					videoView.setImageBitmap(ImageUtil.decodeSampledBitmapFromFile(thumbnailUri, attachmentViewSize.x, attachmentViewSize.y));
					thumbView.setScaleType(ScaleType.CENTER);
					thumbView.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
					durationView.setText(duration + " " + getString(R.string.seconds));
					this.videoUri = videoUri;
					return true;
				}else{
					/* cannot create a valid thumbnial */
					dialog.setMessage(getString(R.string.the_video_resolution_must_be_heigher_than_720px_please_choose_another_video));
					dialog.show();
					return false;
				}
			}
		}else{
			/* Video can not be found in the video storage, it maybe not exist. */
			cur.close();
			dialog.setMessage(getString(R.string.this_video_maybe_corrupted_or_is_malformed_please_choose_another_video));
			dialog.show();
			return false;
		}
		
		
		
	}
	
	private boolean createVideoThumbnail(String videoUri){
		
		Bitmap bmp = ThumbnailUtils.createVideoThumbnail(videoUri, ThumbnailKind.RawKind);
		
		if (bmp == null){
			return false;
		}else{
			if (bmp.getHeight() < 720 || bmp.getWidth() < 720) return false;
			thumbnailUri = FileCacheManager.getInstance().GetTempPath() + System.currentTimeMillis() + ".bmp2jpg";
			boolean saved = FileUtil.writeBitmapToFile(bmp, thumbnailUri, 100, Bitmap.CompressFormat.JPEG);
			bmp.recycle();
			return saved;
		}

	}
	
	/** thumbnail size must be > 720 px **/
	private boolean verifyThumbnailInfo(String thumbUri){
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(thumbUri, options);
	    if (options.outHeight < 720 || options.outWidth < 720){
	    	MaterialDialogAlert dialog = new MaterialDialogAlert(this);
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
		
		

		getIntent().putExtra(OUTPUT_PHOTO_URI, thumbnailUri);
		getIntent().putExtra(OUTPUT_PHOTO_DESCRIPTION, videoDescription.getEditText().getText().toString());
		setResult(RESULT_OK, getIntent());
		finish();
	}
	
	private void doEditVideo(){

		if (editRequireType == RequireEditType.DESCRIPTION || editRequireType == RequireEditType.BOTH){
			if (inputVideoDescription.equals(videoDescription.getEditText().getText().toString())){
				this.shakeView(videoDescription, true);
				videoDescription.setError(getString(R.string.your_video_description_has_not_been_changed_yet));
				videoDescription.setErrorEnabled(true);
				return;
			}
		}
		
		if (editRequireType == RequireEditType.THUMBNAIL || editRequireType == RequireEditType.BOTH){
			if (thumbnailUri == null){
				this.shakeView(findViewById(R.id.thumbViewContainer), true);
				return;
			}
		}
		
		/**
		 * do something
		 */
	}
	
	private void doSubmit(){
		hideKeyboard();
		videoDescription.setErrorEnabled(false);
		
		if (inputEditType == EditType.ADD){
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
			if (data == null || data.getData() == null) return;
			verifyVideoInfo(data.getData().getPath());
		}
		
		if (requestCode == PhonePhotoBrowserActivity.ACTIVITY_CODE){
			if (data == null || data.getData() == null) return;
			verifyThumbnailInfo(data.getData().getPath());
		}
	}

}
