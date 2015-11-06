package com.qpidnetwork.ladydating.album;


import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;
import com.qpidnetwork.ladydating.utility.Converter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.TextView;

public class AlbumEditActivity extends BaseActionbarActivity implements MaterialDropDownMenu.OnClickCallback{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.ALBUM_EDIT_ACTIVITY_CODE;
	
	public static String INPUT_EDIT_TYPE = "INPUT_EDIT_TYPE";
	public static String INPUT_ALBUM_ID = "INPUT_ALBUM_ID";
	public static String INPUT_ALBUM_TYPE = "INPUT_ALBUM_TYPE";
	public static String INPUT_ALBUM_NAME = "INPUT_ALBUM_NAME";
	public static String INPUT_ALBUM_DESCRIPTION = "INPUT_ALBUM_DESCRIPTION";
	
	public static enum EditType{
		CREATE,
		EDIT
	}
	
	public static enum AlbumType{
		PHOTO,
		VIDEO
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
			albumType.setBackgroundResource(R.drawable.rectangle_rounded_angle_grey_bg);
			albumType.setText(albumTypeMenuText[inputAlbumType.ordinal()]);
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
		
		this.showProgressDialog("Loading");
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
		inputAlbumType = AlbumType.values()[which];
		albumType.setText(albumTypeMenuText[which]);
		albumType.setTextColor(getResources().getColor(R.color.text_color_dark));
	}

}
