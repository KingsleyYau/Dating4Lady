package com.qpidnetwork.ladydating.album;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.common.activity.PhonePhotoBrowserActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.DrawableUtil;
import com.qpidnetwork.ladydating.utility.ImageUtil;

public class EditPhotoActivity extends BaseActionbarActivity{
	
	public static final int ACTIVITY_CODE = ActivityCodeUtil.EDIT_PHOTO_ACTIVITY_CODE;
	
	public static class IDs{
		public final static int photo_error_dialog_button_ok = 0x00001f;
		public final static int photo_error_dialog_button_cancel = 0x00002f;
	}

	public static String INPUT_EDIT_TYPE = "INPUT_EDIT_TYPE";
	public static String INPUT_ALBUM_NAME = "INPUT_ALBUM_NAME";
	public static String INPUT_PHOTO_DESCRIPTION = "INPUT_PHOTO_DESCRIPTION";
	public static String INPUT_PHOTO_URI = "INPUT_PHOTO_URI";
	public static String INPUT_REMOTE_PHOTO_URL = "INPUT_REMOTE_PHOTO_URL";
	public static String OUTPUT_PHOTO_URI = "OUTPUT_PHOTO_URI";
	public static String OUTPUT_PHOTO_DESCRIPTION  = "OUTPUT_PHOTO_DESCRIPTION";

	
	
	public static void launchWithAddPhotoMode(Context context, String albumName, String photoUri){
		Intent intent = new Intent(context, EditPhotoActivity.class);
		intent.putExtra(INPUT_EDIT_TYPE, EditType.ADD.toString());
		intent.putExtra(INPUT_ALBUM_NAME, albumName);
		intent.putExtra(INPUT_PHOTO_URI, photoUri);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	public static void launchWithEditPhotoMode(Context context, String albumName, String photoDescription, String remotePhotoUrl){
		Intent intent = new Intent(context, EditPhotoActivity.class);
		intent.putExtra(INPUT_EDIT_TYPE, EditType.EDIT.toString());
		intent.putExtra(INPUT_ALBUM_NAME, albumName);
		intent.putExtra(INPUT_REMOTE_PHOTO_URL, remotePhotoUrl);
		intent.putExtra(INPUT_PHOTO_DESCRIPTION, photoDescription);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	public static enum EditType{
		ADD,
		EDIT
	}
	
	private TextView albumName;
	private MaterialEditText photoDescription;
	private ImageView photo;
	private TextView editionMessage;
	
	private EditType inputEditType;
	private String inputAlbumName;
	private String inputPhotoDescription = "";
	private String inputPhotoUri;
	private String inputRemotePhotoUrl;
	
	private Point attachmentViewSize;
	private String photoUri;
	
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
		
		if (extras.containsKey(INPUT_PHOTO_DESCRIPTION))
			inputPhotoDescription = extras.getString(INPUT_PHOTO_DESCRIPTION);
			
		if (extras.containsKey(INPUT_PHOTO_URI))
			inputPhotoUri = extras.getString(INPUT_PHOTO_URI);
		
		if (extras.containsKey(INPUT_REMOTE_PHOTO_URL))
			inputRemotePhotoUrl = extras.getString(INPUT_REMOTE_PHOTO_URL);
		
		editionMessage = (TextView) findViewById(R.id.editionMessage);
		albumName = (TextView) findViewById(R.id.albumName);
		photoDescription = (MaterialEditText) findViewById(R.id.photoDescription);
		photo = (ImageView) findViewById(R.id.photo);
		
		photo.setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, getResources().getColor(R.color.blue)));
		setupAttachmentViewSize();
		
		albumName.setText(getString(R.string.album_name) + ": " + inputAlbumName);
		photoDescription.getEditText().setText(inputPhotoDescription);
		
		if (inputEditType == EditType.ADD){
			editionMessage.setVisibility(View.GONE);
			photo.setOnClickListener(this);
			verifyPhotoInfo(inputPhotoUri);
		}else{
			editionMessage.setVisibility(View.VISIBLE);
			//Load photo from cache or from the cloud.
		}
		
		//action bar
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle(getString(R.string.upload_photo), getResources().getColor(R.color.text_color_dark));
		
		
	}
	
	private void setupAttachmentViewSize(){
		
		attachmentViewSize = new Point();
		int width = (DeviceUtil.getScreenSize().x - Converter.dp2px(24 + 24)) / 3 * 2;
		int height = width / 16 * 12;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		photo.setLayoutParams(params);
		attachmentViewSize.x = width;
		attachmentViewSize.y = height;

	}
	
	/** thumbnail size must be > 720 px **/
	private boolean verifyPhotoInfo(String photoUri){
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(photoUri, options);
	    if (options.outHeight < 720 || options.outWidth < 720){
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
		
		if (inputPhotoDescription.equals(photoDescription.getEditText().getText().toString())){
			this.shakeView(photoDescription, true);
			photoDescription.setError(getString(R.string.your_photo_description_has_not_been_changed_yet));
			photoDescription.setErrorEnabled(true);
			return;
		}
		
		if (inputEditType == EditType.ADD && photoUri == null){
			this.shakeView(findViewById(R.id.photoViewContainer), true);
			return;
		}

		getIntent().putExtra(OUTPUT_PHOTO_URI, photoUri);
		getIntent().putExtra(OUTPUT_PHOTO_DESCRIPTION, photoDescription.getEditText().getText().toString());
		setResult(RESULT_OK, getIntent());
		finish();
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

}
