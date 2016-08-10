package com.qpidnetwork.ladydating.chat.picture;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.chat.picture.AlbumPhotoItem.PhotoSendStatus;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCPhotoItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.CanSendErrType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback.ResultType;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public class AlbumPhotoFragment extends BaseFragment implements
		OnItemClickListener, LiveChatManagerPhotoListener {

	private static final String ALBUM_TAG_PHOTOLIST = "photoList";
	private static final int CHECK_PHOTO_CALLBACK = 1;

	private GridView gvAlbumPhoto;
	private TextView emptyView;
	private AlbumPhotoItem[] albumPhotoList = null;
	private String targetId = "";
	private LiveChatManager mLiveChatManager;
	private LivechatAlbumAdapter mAdapter;
	
	// 方便快速查找修改
	private HashMap<String, AlbumPhotoItem> mPhotoMap = new HashMap<String, AlbumPhotoItem>();
	private int currPosition = -1; //点前点击位置

	public static AlbumPhotoFragment newInstance(
			LCPhotoListPhotoItem[] photoList, String targetId) {
		AlbumPhotoFragment fragment = new AlbumPhotoFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(ALBUM_TAG_PHOTOLIST, new PhotoListWrapper(photoList));
		bundle.putString(ChatActivity.CHAT_TARGET_ID, targetId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_album_photo, null);
		emptyView = (TextView) view.findViewById(R.id.emptyView);
		gvAlbumPhoto = (GridView) view.findViewById(R.id.gvAlbumPhoto);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		LCPhotoListPhotoItem[] tempPhotoList = null;
		if (bundle != null) {
			if (bundle.containsKey(ALBUM_TAG_PHOTOLIST)) {
				PhotoListWrapper photoWrapper = (PhotoListWrapper) bundle
						.getSerializable(ALBUM_TAG_PHOTOLIST);
				if(photoWrapper != null){
					tempPhotoList = photoWrapper.photoList;
				}
			}
			if (bundle.containsKey(ChatActivity.CHAT_TARGET_ID)) {
				targetId = bundle.getString(ChatActivity.CHAT_TARGET_ID);
			}
		}
		if (tempPhotoList != null) {
			albumPhotoList = new AlbumPhotoItem[tempPhotoList.length];
			for (int i = 0; i < tempPhotoList.length; i++) {
				albumPhotoList[i] = new AlbumPhotoItem(tempPhotoList[i]);
				mPhotoMap.put(tempPhotoList[i].photoId, albumPhotoList[i]);
			}
			emptyView.setText(getResources().getString(
					R.string.livechat_album_list_null));
			gvAlbumPhoto.setEmptyView(emptyView);
			mAdapter = new LivechatAlbumAdapter(getActivity(), albumPhotoList);
			gvAlbumPhoto.setAdapter(mAdapter);
		}
		gvAlbumPhoto.setOnItemClickListener(this);
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterPhotoListener(this);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		RequestBaseResponse response = (RequestBaseResponse) msg.obj;
		switch (msg.what) {
		case CHECK_PHOTO_CALLBACK:{
			LCPhotoItem item = (LCPhotoItem)response.body;
			if((item != null) && (currPosition >= 0)){
				String currPhotoId = albumPhotoList[currPosition].photoItem.photoId;
				if((currPhotoId.equals(item.photoId)) && (mPhotoMap.containsKey(currPhotoId))){
					mPhotoMap.get(currPhotoId).photoStatus = PhotoSendStatus.NONE;
					if (response.isSuccess) {
						//重置图片状态
						sendPhoto(currPhotoId);
					}else {
						onCheckPhotoError(currPhotoId, response.errno, response.errmsg);
					}
					mAdapter.notifyDataSetChanged();
				}
				updateItemByPosition(currPosition);
			}
		}break;

		default:
			break;
		}
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mLiveChatManager.UnregisterPhotoListener(this);
	}
	
	/**
	 * 检测图片状态错误处理
	 * @param photoId
	 * @param errNo
	 * @param errMsg
	 */
	private void onCheckPhotoError(String photoId, String errNo, String errMsg){
		int errno = Integer.valueOf(errNo);
		String notify = "";
		if(errno >= 0 && errno <= ResultType.SentAndRead.ordinal()){
			switch (ResultType.values()[errno]) {
			case CannotSend:{
				notify = getResources().getString(R.string.livechat_send_photo_error_unknow);
			}break;
			case PhotoNotExist:{
				notify = getResources().getString(R.string.livechat_send_photo_error_noverify);
			}break;
			case NotAllow:{
				notify = getResources().getString(R.string.livechat_send_photo_error_unpermit);
			}break;
			case OverUnread:{
				notify = getResources().getString(R.string.livechat_send_photo_error_unread);
			}break;
			case Sent:
			case SentAndRead:{
				mPhotoMap.get(photoId).photoStatus = PhotoSendStatus.FAIL_SENDED;
			}break;
			default:
				break;
			}
		}
		if(!TextUtils.isEmpty(notify)){
			cannotSendNotify(notify);
		}
	}

	/**
	 * viewPager 切换时重置
	 */
	public void resetStatusAndData() {
		if((albumPhotoList != null) && (currPosition >= 0) && (currPosition < albumPhotoList.length)){
			String currPhotoId = albumPhotoList[currPosition].photoItem.photoId;
			if(mPhotoMap.containsKey(currPhotoId) && (mAdapter != null)){
				mPhotoMap.get(currPhotoId).photoStatus = PhotoSendStatus.NONE;
				updateItemByPosition(currPosition);
			}
			currPosition = -1;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String currPhotoId = "";
		if(currPosition >= 0){
			currPhotoId = albumPhotoList[currPosition].photoItem.photoId;
		}
		if (currPosition != position) {
			if(!TextUtils.isEmpty(currPhotoId)){
				mPhotoMap.get(currPhotoId).photoStatus = PhotoSendStatus.NONE;
				updateItemByPosition(currPosition);
			}
			currPosition = position;
			checkAndSend();
		} else {
			if(!TextUtils.isEmpty(currPhotoId)){
				switch (mPhotoMap.get(currPhotoId).photoStatus) {
				case NONE:
					checkAndSend();
					break;
				case CHECKING:
					/* 什么也不做 */
					break;
				case FAIL_SENDED:
					onPhotoCheckError();
					break;
				default:
					break;
				}
			}
		}
		
	}
	
	/********* 更新GridView指定Item状态 ****************************/
	/**
	 * 更新指定Item状态
	 * 
	 * @param position
	 */
	private void updateItemByPosition(int position) {
		if (position >= 0) {
			View childAt = gvAlbumPhoto.getChildAt(position
					- gvAlbumPhoto.getFirstVisiblePosition());
			if (childAt != null) {
				switch (albumPhotoList[position].photoStatus) {
				case NONE:{
					childAt.findViewById(R.id.progressBar).setVisibility(View.GONE);
					childAt.findViewById(R.id.ivError).setVisibility(View.GONE);
				}break;
				case CHECKING:{
					childAt.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
					childAt.findViewById(R.id.ivError).setVisibility(View.GONE);
				}break;
				case FAIL_SENDED:{
					childAt.findViewById(R.id.progressBar).setVisibility(View.GONE);
					childAt.findViewById(R.id.ivError).setVisibility(View.VISIBLE);
				}break;
				default:
					break;
				}
			}
		}
	}

	


	/**
	 * 检测是否发送太频繁，太频繁提示
	 */
	private void checkAndSend() {
		/* 检测是否太频繁及是否开始聊天 */
		CanSendErrType errType = mLiveChatManager.CanSendMessage(targetId, MessageType.Photo);
		String currPhotoId = albumPhotoList[currPosition].photoItem.photoId;
		if (errType == CanSendErrType.OK) {
			// 检测图片是否可用
			mPhotoMap.get(currPhotoId).photoStatus = PhotoSendStatus.CHECKING;
			updateItemByPosition(currPosition);
			mLiveChatManager.CheckSendPhotoMessage(targetId, currPhotoId);
		} else {
			// 通知ChatActivity 提示用户
			if (errType == CanSendErrType.NoInChat) {
				cannotSendNotify(getResources().getString(
								R.string.livechat_can_not_send_photo_before_the_conversation_has_started));
			} else if (errType == CanSendErrType.SendMsgFrequency) {
				cannotSendNotify(getResources().getString(R.string.livechat_send_photo_frequently));
			}else{
				cannotSendNotify(getResources().getString(R.string.livechat_send_photo_error_unknow));
			}
		}
	}

	private void onPhotoCheckError() {
		MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
		dialog.setMessage(getResources().getString(
				R.string.livechat_photo_already_send));
		dialog.addButton(dialog.createButton(getString(R.string.ok), null));
		dialog.show();
	}

	/************************** ChatActivity 交互模块 ********************************/
	/**
	 * 发送图片接口
	 * 
	 * @param photoId
	 */
	private void sendPhoto(String photoId) {
		if ((getActivity() != null) && (getActivity() instanceof ChatActivity)) {
			((ChatActivity) getActivity()).sendPrivatePhoto(photoId);
		}
	}

	/**
	 * 消息发送频繁处理
	 * 
	 * @param message
	 */
	private void cannotSendNotify(String message) {
		if ((getActivity() != null) && (getActivity() instanceof ChatActivity)) {
			((ChatActivity) getActivity()).cannotSendNotify(message);
		}
	}

	/****************** Livechat Photo 相关回调 ************************************/
	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCPhotoItem photoItem) {
		if ((userItem != null) && (photoItem != null) && (targetId.equals(userItem.userId))) {
			Message msg = Message.obtain();
			msg.what = CHECK_PHOTO_CALLBACK;
			RequestBaseResponse response = new RequestBaseResponse(
					errType == LiveChatErrType.Success ? true : false, String.valueOf(result.ordinal()),
					errmsg, photoItem);
			msg.obj = response;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCPhotoItem photoItem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg,
			LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		// TODO Auto-generated method stub

	}
}
