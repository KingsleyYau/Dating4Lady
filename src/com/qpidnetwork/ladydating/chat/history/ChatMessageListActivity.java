package com.qpidnetwork.ladydating.chat.history;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.chat.MessageListView;
import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.item.LCChatListItem;

public class ChatMessageListActivity extends BaseActionbarActivity{
	
	private static final int GET_MESSAGE_LIST_CALLBACK = 1;
	
	private MessageListView mMsgListView;
	private TextView tvViewDetial;
	
	private String photoUrl;
	private LCChatListItem chatHistoryItem;
	
	public static void launchChatMessageListActivity(Context context, LCChatListItem chatHistoryItem, String photoUrl){
		Intent intent = new Intent(context, ChatMessageListActivity.class);
		intent.putExtra(LivechatChatHistoryDetailActivity.LIVE_CHAT_HISTORY_ITEM, chatHistoryItem);
		intent.putExtra(ChatActivity.CHAT_TARGET_PHOTO_URL, photoUrl);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setActionbarTitle( R.string.livechat_chat_history, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		mMsgListView = (MessageListView)findViewById(R.id.msgList);
		tvViewDetial = (TextView)findViewById(R.id.tvViewDetial);
		tvViewDetial.setOnClickListener(this);
		initData();
	}
	
	private void initData(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(ChatActivity.CHAT_TARGET_PHOTO_URL)){
				photoUrl = bundle.getString(ChatActivity.CHAT_TARGET_PHOTO_URL);
			}
			if(bundle.containsKey(LivechatChatHistoryDetailActivity.LIVE_CHAT_HISTORY_ITEM)){
				chatHistoryItem = (LCChatListItem)bundle.getSerializable(LivechatChatHistoryDetailActivity.LIVE_CHAT_HISTORY_ITEM);
			}
		}
		QueryChatMessageList();
	}
	
	private void QueryChatMessageList(){
		if(chatHistoryItem != null){
			showProgressDialog(getResources().getString(R.string.processing));
			LiveChatManager.getInstance().GetUserHistoryMessageByInviteId(chatHistoryItem.manId, chatHistoryItem.inviteId, new OnGetChatMessageListCallback() {
				
				@Override
				public void OnGetChatMessageList(boolean isSuccess, String errno,
						String errmsg, List<LCMessageItem> msgList) {
					// TODO Auto-generated method stub
					Message msg = Message.obtain();
					msg.what = GET_MESSAGE_LIST_CALLBACK;
					RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, msgList);
					msg.obj = response;
					sendUiMessage(msg);
				}
			});
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		dismissProgressDialog();
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
		switch (msg.what) {
		case GET_MESSAGE_LIST_CALLBACK:{
			if(response.isSuccess){
				//更新为已读
				if(chatHistoryItem != null){
					ChatHistoryDB.getInstance(this).UpdateChatHistory(chatHistoryItem.manId, chatHistoryItem.inviteId, true);
				}
				
				List<LCMessageItem> msgList = (List<LCMessageItem>)response.body;
				mMsgListView.replaceAllRow(msgList);
			}else{
				Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
			}
		}break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tvViewDetial:{
			LivechatChatHistoryDetailActivity.launchLivechatHistoryDetailActivity(this, chatHistoryItem, photoUrl);
		}break;

		default:
			break;
		}
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_chat_messagelist;
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
		default:
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}

}
