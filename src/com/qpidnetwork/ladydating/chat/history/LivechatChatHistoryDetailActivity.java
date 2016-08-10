package com.qpidnetwork.ladydating.chat.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.authorization.LoginParam;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.LCChatListItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class LivechatChatHistoryDetailActivity extends BaseActionbarActivity{
	
	private static final String LIVE_CHAT_HISTORY_ITEM = "chatHistoryItem";
	
	private CircleImageView ivPhoto;
	private TextView tvName;
	private TextView tvNum;
	private TextView tvInviteId;
	private TextView tvAgencyId;
	private TextView tvTranslatorId;
	private TextView tvTranslatorName;
	private TextView tvStartTime;
	private TextView tvDuaring;
	
	private String photoUrl;
	private LCChatListItem chatHistoryItem;
	
	public static void launchLivechatHistoryDetailActivity(Context context, LCChatListItem chatHistoryItem, String photoUrl){
		Intent intent = new Intent(context, LivechatChatHistoryDetailActivity.class);
		intent.putExtra(LIVE_CHAT_HISTORY_ITEM, chatHistoryItem);
		intent.putExtra(ChatActivity.CHAT_TARGET_PHOTO_URL, photoUrl);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setActionbarTitle(getString(R.string.livechat_chat_history_title), getResources().getColor(R.color.text_color_dark));
        this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
        
		initViews();
		initDatas();
	}
	
	private void initViews(){
		ivPhoto = (CircleImageView)findViewById(R.id.ivPhoto);
		tvName = (TextView)findViewById(R.id.tvName);
		tvNum = (TextView)findViewById(R.id.tvNum);
		tvInviteId = (TextView)findViewById(R.id.tvInviteId);
		tvAgencyId = (TextView)findViewById(R.id.tvAgencyId);
		tvTranslatorId = (TextView)findViewById(R.id.tvTranslatorId);
		tvTranslatorName = (TextView)findViewById(R.id.tvTranslatorName);
		tvStartTime = (TextView)findViewById(R.id.tvStartTime);
		tvDuaring = (TextView)findViewById(R.id.tvDuaring);
	}
	
	private void initDatas(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(ChatActivity.CHAT_TARGET_PHOTO_URL)){
				photoUrl = bundle.getString(ChatActivity.CHAT_TARGET_PHOTO_URL);
			}
			if(bundle.containsKey(LIVE_CHAT_HISTORY_ITEM)){
				chatHistoryItem = (LCChatListItem)bundle.getSerializable(LIVE_CHAT_HISTORY_ITEM);
			}
		}
		
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(photoUrl);
		new ImageViewLoader(this).DisplayImage(ivPhoto, photoUrl, localPath, null);
		if(chatHistoryItem != null){
			LoginParam params = LoginManager.getInstance().GetLoginParam();
			tvName.setText(chatHistoryItem.manName);
			tvNum.setText(chatHistoryItem.manId);
			String detailInvite = getResources().getString(R.string.livechat_chat_history_detail, chatHistoryItem.inviteId);
			tvInviteId.setText(detailInvite);
			if(params != null && params.item != null){
				tvAgencyId.setText(params.item.agent);
			}
			tvTranslatorId.setText(chatHistoryItem.transId);
			tvTranslatorName.setText(chatHistoryItem.transName);
			tvStartTime.setText(chatHistoryItem.startTime);
			tvDuaring.setText(chatHistoryItem.duringTime);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_chat_history_detail;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		switch(menu.getItemId()){
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
		return true;
	}

}
