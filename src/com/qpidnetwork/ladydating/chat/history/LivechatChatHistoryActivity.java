package com.qpidnetwork.ladydating.chat.history;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnLCGetChatListCallback;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.item.LCChatListItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class LivechatChatHistoryActivity extends BaseActionbarActivity{

	private static final int GET_CHAT_HISTORY_CALLBACK = 1;
	private static final int UPDATE_UNREAD_CALLBACK = 2;
	
	private ListView lvHistory;
	private TextView tvName;
	private TextView tvNum;
	private CircleImageView ivPhoto;
	
	private String manId = "";
	private String manName = "";
	private String manPhotoUrl = "";
	private LCChatListItem[] mChatHistoryList;
	
	private ChatHistoryDB mChatHistoryDB;
	private LivechatHistoryAdapter mAdapter;
	
	public static void launchLivechatHistoryActivity(Context context, String man_id, String manName, String manPhotoUrl){
		Intent intent = new Intent(context, LivechatChatHistoryActivity.class);
		intent.putExtra(ChatActivity.CHAT_TARGET_ID, man_id);
		intent.putExtra(ChatActivity.CHAT_TARGET_NAME, manName);
		intent.putExtra(ChatActivity.CHAT_TARGET_PHOTO_URL, manPhotoUrl);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setActionbarTitle(getString(R.string.livechat_chat_history_title), getResources().getColor(R.color.text_color_dark));
        this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
        
		lvHistory = (ListView)findViewById(R.id.lvHistory);
		tvName = (TextView)findViewById(R.id.tvName);
		tvNum = (TextView)findViewById(R.id.tvNum);
		ivPhoto = (CircleImageView)findViewById(R.id.ivPhoto);
		initData();
	}
	
	private void initData(){
		mChatHistoryDB = ChatHistoryDB.getInstance(this);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(ChatActivity.CHAT_TARGET_ID)){
				manId = bundle.getString(ChatActivity.CHAT_TARGET_ID);
			}
			if(bundle.containsKey(ChatActivity.CHAT_TARGET_NAME)){
				manName = bundle.getString(ChatActivity.CHAT_TARGET_NAME);
			}
			if(bundle.containsKey(ChatActivity.CHAT_TARGET_PHOTO_URL)){
				manPhotoUrl = bundle.getString(ChatActivity.CHAT_TARGET_PHOTO_URL);
			}
		}
		tvName.setText(manName);
		tvNum.setText(manId);
		String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(manPhotoUrl);
		new ImageViewLoader(this).DisplayImage(ivPhoto, manPhotoUrl, localPath, null);
		lvHistory.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mAdapter.updateUnreadFlagByPosition(position);
//				LivechatChatHistoryDetailActivity.launchLivechatHistoryDetailActivity(LivechatChatHistoryActivity.this, mChatHistoryList[position], manPhotoUrl);
				ChatMessageListActivity.launchChatMessageListActivity(LivechatChatHistoryActivity.this, mChatHistoryList[position], manPhotoUrl);
			}
		});
		getChatHistory();
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);

		switch (msg.what) {
		case GET_CHAT_HISTORY_CALLBACK:{
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
			if(response.isSuccess){
				mChatHistoryList = (LCChatListItem[])response.body;
				if(mChatHistoryList != null){
					mAdapter = new LivechatHistoryAdapter(this, mChatHistoryList);
					lvHistory.setAdapter(mAdapter);
					updateUnreadStatus(mChatHistoryList);
				}
			}else{
				Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
			}
		}break;
		
		case UPDATE_UNREAD_CALLBACK:{
			Boolean[] unreadFlags = (Boolean[])msg.obj;
			mAdapter.updateUnreadFlags(unreadFlags);
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 本地数据库同步未读状态
	 */
	private void updateUnreadStatus(final LCChatListItem[] chatHistoryList){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HashMap<String, Boolean> inviteMap = mChatHistoryDB.getAllInviteReadFlag(manId);
				Boolean[] unreadFlags = new Boolean[chatHistoryList.length];
				for(int i=0; i < chatHistoryList.length; i++){
					if(inviteMap.containsKey(chatHistoryList[i].inviteId)){
						unreadFlags[i] = inviteMap.get(chatHistoryList[i].inviteId);
					}else{
						unreadFlags[i] = false;
					}
				}
				Message msg = Message.obtain();
				msg.what = UPDATE_UNREAD_CALLBACK;
				msg.obj = unreadFlags;
				sendUiMessage(msg);
			}
		}).start();

	}
	
	private void getChatHistory(){
		RequestJniLivechat.GetChatList(manId, new OnLCGetChatListCallback() {
			
			@Override
			public void OnLCGetChatList(boolean isSuccess, String errno, String errmsg,
					LCChatListItem[] list) {
				Message msg = Message.obtain();
				msg.what = GET_CHAT_HISTORY_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, list);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_chat_history;
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
		case R.id.markasread:{
			mChatHistoryDB.markAllAsReadByManId(manId);
			if(mChatHistoryList != null){
				Boolean[] unreadFlags = new Boolean[mChatHistoryList.length];
				for(int i=0; i < mChatHistoryList.length; i++){
					unreadFlags[i] = false;
				}
				mAdapter.updateUnreadFlags(unreadFlags);
			}
		}break;
		default:
			break;
		}		
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.chathistory_markasread, menu);
		return true;
	}


}
