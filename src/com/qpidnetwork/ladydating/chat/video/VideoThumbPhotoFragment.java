package com.qpidnetwork.ladydating.chat.video;

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
import com.qpidnetwork.ladydating.chat.video.VideoItem.VideoSendStatus;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.CanSendErrType;
import com.qpidnetwork.livechat.LCVideoItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback.ResultType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

public class VideoThumbPhotoFragment extends BaseFragment implements
		OnItemClickListener, LiveChatManagerVideoListener {

	private static final String ALBUM_TAG_VIDEOLIST = "videoList";
	private static final int CHECK_VIDEO_CALLBACK = 1;

	private GridView gvVideoPhoto;
	private TextView emptyView;

	private VideoItem[] videoList = null;
	private String targetId = "";
	private LiveChatManager mLiveChatManager;
	private LivechatVideoListAdapter mAdapter;

	// 方便快速查找修改
	private HashMap<String, VideoItem> mVideoMap = new HashMap<String, VideoItem>();
	private int currPosition = -1; // 点前点击位置

	public static VideoThumbPhotoFragment newInstance(
			LCVideoListVideoItem[] videoList, String targetId) {
		VideoThumbPhotoFragment fragment = new VideoThumbPhotoFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(ALBUM_TAG_VIDEOLIST, videoList);
		bundle.putString(ChatActivity.CHAT_TARGET_ID, targetId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_video_thumb_photo, null);
		emptyView = (TextView) view.findViewById(R.id.emptyView);
		gvVideoPhoto = (GridView) view.findViewById(R.id.gvVideoPhoto);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		LCVideoListVideoItem[] tempVideoList = null;
		if (bundle != null) {
			if (bundle.containsKey(ALBUM_TAG_VIDEOLIST)) {
				tempVideoList = (LCVideoListVideoItem[]) bundle
						.getSerializable(ALBUM_TAG_VIDEOLIST);
			}
			if (bundle.containsKey(ChatActivity.CHAT_TARGET_ID)) {
				targetId = bundle.getString(ChatActivity.CHAT_TARGET_ID);
			}
		}
		if (tempVideoList != null) {
			videoList = new VideoItem[tempVideoList.length];
			for (int i = 0; i < tempVideoList.length; i++) {
				videoList[i] = new VideoItem(tempVideoList[i]);
				mVideoMap.put(tempVideoList[i].videoId, videoList[i]);
			}
			emptyView.setText(getResources().getString(
					R.string.livechat_album_list_null));
			gvVideoPhoto.setEmptyView(emptyView);
			mAdapter = new LivechatVideoListAdapter(getActivity(), videoList);
			gvVideoPhoto.setAdapter(mAdapter);
		}
		gvVideoPhoto.setOnItemClickListener(this);
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterVideoListener(this);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		RequestBaseResponse response = (RequestBaseResponse) msg.obj;
		switch (msg.what) {
		case CHECK_VIDEO_CALLBACK:{
			LCVideoItem item = (LCVideoItem)response.body;
			if((item != null) && (currPosition >= 0)){
				String currVideoId = videoList[currPosition].videoItem.videoId;
				if((currVideoId.equals(item.videoId)) && (mVideoMap.containsKey(currVideoId))){
					mVideoMap.get(currVideoId).videoStatus = VideoSendStatus.NONE;
					if (response.isSuccess) {
						//重置图片状态
						sendVideo(currVideoId);
					}else {
						onCheckVideoError(currVideoId, response.errno, response.errmsg);
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
		mLiveChatManager.UnregisterVideoListener(this);
	}
	
	/**
	 * 检测图片状态错误处理
	 * @param videoId
	 * @param errNo
	 * @param errMsg
	 */
	private void onCheckVideoError(String videoId, String errNo, String errMsg){
		int errno = Integer.valueOf(errNo);
		String notify = "";
		if(errno >= 0 && errno <= ResultType.SentAndRead.ordinal()){
			switch (ResultType.values()[errno]) {
			case CannotSend:{
				notify = getResources().getString(R.string.livechat_send_video_error_unknow);
			}break;
			case VideoNotExist:{
				notify = getResources().getString(R.string.livechat_send_video_error_noverify);
			}break;
			case NotAllow:{
				notify = getResources().getString(R.string.livechat_send_video_error_unpermit);
			}break;
			case OverUnread:{
				notify = getResources().getString(R.string.livechat_send_video_error_unread);
			}break;
			case Sent:
			case SentAndRead:{
				mVideoMap.get(videoId).videoStatus = VideoSendStatus.FAIL_SENDED;
			}break;
			default:
				break;
			}
		}
		if(!TextUtils.isEmpty(notify)){
			cannotSendNotify(notify);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String currVideoId = "";
		if (currPosition >= 0) {
			currVideoId = videoList[currPosition].videoItem.videoId;
		}
		if (currPosition != position) {
			if (!TextUtils.isEmpty(currVideoId)) {
				mVideoMap.get(currVideoId).videoStatus = VideoSendStatus.NONE;
				updateItemByPosition(currPosition);
			}
			currPosition = position;
			checkAndSend();
		} else {
			if (!TextUtils.isEmpty(currVideoId)) {
				switch (mVideoMap.get(currVideoId).videoStatus) {
				case NONE:
					checkAndSend();
					break;
				case CHECKING:
					/* 什么也不做 */
					break;
				case FAIL_SENDED:
					onVideoCheckError();
					break;
				default:
					break;
				}
			}
		}

	}

	/**
	 * viewPager 切换时重置
	 */
	public void resetStatusAndData() {
		if ((videoList != null) && (currPosition >= 0)
				&& (currPosition < videoList.length)) {
			String currVideoId = videoList[currPosition].videoItem.videoId;
			if (mVideoMap.containsKey(currVideoId) && (mAdapter != null)) {
				mVideoMap.get(currVideoId).videoStatus = VideoSendStatus.NONE;
				updateItemByPosition(currPosition);
			}
			currPosition = -1;
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
			View childAt = gvVideoPhoto.getChildAt(position
					- gvVideoPhoto.getFirstVisiblePosition());
			if (childAt != null) {
				switch (videoList[position].videoStatus) {
				case NONE: {
					childAt.findViewById(R.id.progressBar).setVisibility(
							View.GONE);
					childAt.findViewById(R.id.ivError).setVisibility(View.GONE);
				}
					break;
				case CHECKING: {
					childAt.findViewById(R.id.progressBar).setVisibility(
							View.VISIBLE);
					childAt.findViewById(R.id.ivError).setVisibility(View.GONE);
				}
					break;
				case FAIL_SENDED: {
					childAt.findViewById(R.id.progressBar).setVisibility(
							View.GONE);
					childAt.findViewById(R.id.ivError).setVisibility(
							View.VISIBLE);
				}
					break;
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
		CanSendErrType errType = mLiveChatManager.CanSendMessage(targetId,
				MessageType.Video);
		String currVideoId = videoList[currPosition].videoItem.videoId;
		if (errType == CanSendErrType.OK) {
			// 检测图片是否可用
			mVideoMap.get(currVideoId).videoStatus = VideoSendStatus.CHECKING;
			updateItemByPosition(currPosition);
			mLiveChatManager.CheckSendVideo(targetId, currVideoId);
		} else {
			// 通知ChatActivity 提示用户
			if (errType == CanSendErrType.NoInChat) {
				cannotSendNotify(getResources()
						.getString(
								R.string.livechat_can_not_send_video_before_the_conversation_has_started));
			} else if (errType == CanSendErrType.SendMsgFrequency) {
				cannotSendNotify(getResources().getString(
						R.string.livechat_send_video_frequently));
			} else {
				cannotSendNotify(getResources().getString(
						R.string.livechat_send_video_error_unknow));
			}
		}
	}

	private void onVideoCheckError() {
		MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
		dialog.setMessage(getResources().getString(
				R.string.livechat_video_already_send));
		dialog.addButton(dialog.createButton(getString(R.string.ok), null));
		dialog.show();
	}

	/************************** ChatActivity 交互模块 ********************************/
	 /**
	 * 发送图片接口
	 *
	 * @param videoId
	 */
	 private void sendVideo(String videoId) {
		 if ((getActivity() != null) && (getActivity() instanceof ChatActivity)) {
			 ((ChatActivity) getActivity()).sendVideo(videoId);
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

	/********************* Livechat Video 相关回掉 *********************************/

	@Override
	public void OnCheckSendVideo(
			LiveChatErrType errType,
			com.qpidnetwork.request.OnLCCheckSendVideoCallback.ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCVideoItem videoItem) {
		if ((userItem != null) && (videoItem != null)
				&& (targetId.equals(userItem.userId))) {
			Message msg = Message.obtain();
			msg.what = CHECK_VIDEO_CALLBACK;
			RequestBaseResponse response = new RequestBaseResponse(
					errType == LiveChatErrType.Success ? true : false,
					String.valueOf(result.ordinal()), errmsg, videoItem);
			msg.obj = response;
			sendUiMessage(msg);
		}
	}

	@Override
	public void OnSendVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvShowVideo(LCUserItem userItem, String videoId,
			String videoDesc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String videoId,
			String videoDesc) {
		// TODO Auto-generated method stub

	}
}
