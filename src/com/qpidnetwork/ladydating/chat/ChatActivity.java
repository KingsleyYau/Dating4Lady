package com.qpidnetwork.ladydating.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack.OperateType;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.contact.ContactManager;
import com.qpidnetwork.ladydating.chat.emotion.NormalEmotionFragment;
import com.qpidnetwork.ladydating.chat.history.LivechatChatHistoryActivity;
import com.qpidnetwork.ladydating.chat.noramlexp.NormalExprssionFragment;
import com.qpidnetwork.ladydating.chat.picture.LivechatAlbumListFragment;
import com.qpidnetwork.ladydating.chat.translate.TransLangBean;
import com.qpidnetwork.ladydating.chat.translate.TranslateCallbackItem;
import com.qpidnetwork.ladydating.chat.translate.TranslateManager;
import com.qpidnetwork.ladydating.chat.translate.TranslateManager.OnTranslateMessageCallback;
import com.qpidnetwork.ladydating.chat.video.LivechatVideoListFragment;
import com.qpidnetwork.ladydating.chat.voice.VoiceRecordFragment;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogSingleChoice;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.customized.view.VerifyCodeDialog;
import com.qpidnetwork.ladydating.customized.view.VerifyCodeDialog.OnButtonOkClickListener;
import com.qpidnetwork.ladydating.home.HomeActivity;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCPhotoItem;
import com.qpidnetwork.livechat.LCSelfInfo;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.CanSendErrType;
import com.qpidnetwork.livechat.LCVideoItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback.ResultType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.EmotionConfigItem;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class ChatActivity extends BaseFragmentActivity implements OnClickListener, 
						LiveChatManagerOtherListener, LiveChatManagerMessageListener,
						LiveChatManagerEmotionListener, LiveChatManagerPhotoListener,
						LiveChatManagerVoiceListener, OnTranslateMessageCallback, 
						LiveChatManagerVideoListener{

	public static final String SEND_EMTOTION_ACTION = "livechat.sendemotion";
	public static final String EMOTION_ID = "emotion_id";

	public static final String CHAT_TARGET_ID = "targetId";
	public static final String CHAT_TARGET_NAME = "targetName";
	public static final String CHAT_TARGET_PHOTO_URL = "targetPhotoUrl";

	private static final int MIN_HEIGHT = 200;// 单位dp
	
	private static final int RECEIVE_CHAT_MESSAGE = 0;
	private static final int PRIVATE_SHOW_PHOTO_DOWNLOADED = 1;
	private static final int RECEIVE_CHECK_SEND_MESSAGE_ERROR = 2;
	private static final int GET_HISTORY_MESSAGE_UPDATE = 3;
	private static final int SEND_MESSAGE_CALLBACK = 4;
	private static final int TRANSLATE_TEXT_CALLBACK = 5;
	private static final int RECEIVE_IDENTIFY_CODE = 6;
	private static final int REPLY_IDENTIFY_CODE_CALLBACK = 7;
	private static final int RECEIVE_PHOTO_VIEW_IDENTIFY = 8;
	private static final int VIDEO_THUMB_PHOTO_DOWNLOAD = 9;
	private static final int RECEIVE_VIDEO_VIEW_IDENTIFY = 10;
	private static final int END_CHAT = 11;

	// fragments
	private NormalExprssionFragment normalExprssionFragment;
	private VoiceRecordFragment voiceRecordFragment;
	private NormalEmotionFragment emotionFragment;
	private LivechatAlbumListFragment mLivechatAlbumListFragment;
	private LivechatVideoListFragment mLivechatVideoListFragment;

	/* title */
	private LinearLayout llBack;
	private CircleImageView ivPhoto;
	private TextView tvUnread;
	private TextView tvName;
	private ImageButton btnMore;
	private MaterialDropDownMenu dropDown;

	private EditText etMessage;
	private ImageButton btnExpression;
	private ImageButton btnSend;
	private MessageListView msgList;

	private ImageButton btnTakePhoto;
	private ImageButton btnVideo;
	private ImageButton btnSelectPhoto;
	private ImageButton btnVoice;
	private ImageButton btnEmotion;

	/* 底部pane */
	private FrameLayout flBottom;
	private boolean isResizeInOnLayout = false;

	/* 广播用于activity间数据传递 */
	private BroadcastReceiver mBroadcastReceiver;
	
	/* 当前聊天对象信息 */
	private String targetId;
	private String targetName;
	private String targetUrl;
	private int unreadCount = 0;// 未读消息条数

	/* 数据管理区 */
	private LCUserItem chatTarget; // 存储当前聊天对象
	private LiveChatManager mLiveChatManager;
	private ContactManager mContactManager;// 联系人列表管理类，此处主要用于在聊设置，及未读条数显示
	private TranslateManager mTranslateManager; //管理翻译结果及翻译设定

	/* 处理弹出小菜单功能时，返回键首先关闭小菜单功能，再关闭窗口及点击msgList空白区域，关闭小菜单功能 */
	public boolean isMenuOpen = false;
	
	private boolean isCurrActivityVisible = true;// 简单判断当前Activity是否可见，用于下载私密照返回处理是否更新
	
	private VerifyCodeDialog mDialog;//当前验证码dialog
	
	//用于解决消息输入框6.0删除表情需要多次问题
	private ArrayList<ImageSpan> mEmoticonsToRemove = new ArrayList<ImageSpan>();
	
	public static void launchChatActivity(Context context, String manId, String userName, String photoUrl){
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(CHAT_TARGET_ID, manId);
		intent.putExtra(CHAT_TARGET_NAME, userName);
		intent.putExtra(CHAT_TARGET_PHOTO_URL, photoUrl);
		context.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_livechat_chat);
		
		mLiveChatManager = LiveChatManager.getInstance();
		mTranslateManager = TranslateManager.getInstance();
		initViews();
		initData();
		initLivechatConfig();
		initReceive();
		initKeyboardDetect();
		
	}

	private void initViews() {

		/* title */
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvUnread = (TextView) findViewById(R.id.tvUnread);
		ivPhoto = (CircleImageView) findViewById(R.id.ivPhoto);
		tvName = (TextView) findViewById(R.id.tvName);
		btnMore = (ImageButton) findViewById(R.id.btnMore);

		msgList = (MessageListView) findViewById(R.id.msgList);
		msgList.setOnTouchListener(onMessageListTouchListener);

		/* 文字信息编辑 */
		etMessage = (EditText) findViewById(R.id.etMessage);
		etMessage.addTextChangedListener(edtInputWatcher);
		etMessage.setOnTouchListener(edtInputTouch);

		btnExpression = (ImageButton) findViewById(R.id.btnExpression);
		btnExpression.setOnClickListener(this);
		btnSend = (ImageButton) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
		// btnSend.setEnabled(etMessage.getText().length() > 0 ? true : false);

		if (Build.VERSION.SDK_INT >= 21) {
			findViewById(R.id.back_button_icon).getLayoutParams().height = UnitConversion
					.dip2px(this, 48);
			findViewById(R.id.back_button_icon).getLayoutParams().width = UnitConversion
					.dip2px(this, 48);
			btnMore.getLayoutParams().height = UnitConversion
					.dip2px(this, 48);
			btnMore.getLayoutParams().width = UnitConversion
					.dip2px(this, 48);
			((LinearLayout.LayoutParams)btnMore.getLayoutParams()).rightMargin = 0;
			
		}

		/* 工具栏操作区 */
		btnTakePhoto = (ImageButton) findViewById(R.id.btnTakePhoto);
		btnVideo  = (ImageButton) findViewById(R.id.btnVideo);
		btnSelectPhoto = (ImageButton) findViewById(R.id.btnSelectPhoto);
		btnVoice = (ImageButton) findViewById(R.id.btnVoice);
		btnEmotion = (ImageButton) findViewById(R.id.btnEmotion);

		btnTakePhoto.setOnClickListener(this);
		btnVideo.setOnClickListener(this);
		btnSelectPhoto.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
		btnEmotion.setOnClickListener(this);
		btnMore.setOnClickListener(this);
		ivPhoto.setOnClickListener(this);

		/* 底部pane */
		flBottom = (FrameLayout) findViewById(R.id.flBottom);
	}
	
	private void initData(){

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(CHAT_TARGET_ID)) {
				targetId = bundle.getString(CHAT_TARGET_ID);
			}
			if (bundle.containsKey(CHAT_TARGET_NAME)) {
				targetName = bundle.getString(CHAT_TARGET_NAME);
			}
			if (bundle.containsKey(CHAT_TARGET_PHOTO_URL)) {
				targetUrl = bundle.getString(CHAT_TARGET_PHOTO_URL);
			}
		}

		if ((StringUtil.isEmpty(targetId))) {
			finish();
			return;
		} else {
			/* 初始化正在聊天对象，方便统计未读 */
			mContactManager = ContactManager.getInstance();
			mContactManager.mManId = targetId;
			mContactManager.clearContactUnreadCount(targetId);
			
			/* 初始化未读条数 */
			unreadCount = mContactManager.getAllUnreadCount();
			if (unreadCount > 0) {
				tvUnread.setText("" + unreadCount);
				tvUnread.setVisibility(View.VISIBLE);
			} else {
				tvUnread.setVisibility(View.GONE);
			}

			/* 初始化Title */
			tvName.setText(targetName);
			if (!StringUtil.isEmpty(targetUrl)) {
				String localPath = FileCacheManager.getInstance()
						.CacheImagePathFromUrl(targetUrl);
				new ImageViewLoader(this).DisplayImage(ivPhoto, targetUrl,
						localPath, null);
			}

			/* 是否在聊天列表（即有消息来往） */
			chatTarget = mLiveChatManager.GetUserWithId(targetId);
			if (chatTarget != null) {
				/* 加载历史消息 */
				if (chatTarget.getMsgList().size() > 0) {
					showMsgBeans(chatTarget.getMsgList(), true);
				}
			}
		}
	}
	
	/**
	 * 初始化配置LivechatManager，监听消息请求及推送事件
	 */
	private void initLivechatConfig() {
		/* 绑定监听回调事件 */
		mLiveChatManager.RegisterMessageListener(this);
		mLiveChatManager.RegisterEmotionListener(this);
		mLiveChatManager.RegisterPhotoListener(this);
		mLiveChatManager.RegisterVoiceListener(this);
		mLiveChatManager.RegisterOtherListener(this);
		mLiveChatManager.RegisterVideoListener(this);
		/*翻译绑定*/
		mTranslateManager.RegisterTranslateListener(this);
	}

	/**
	 * 设置监控软键盘弹出时，计算软键盘高度，以此确定子view高度
	 */
	private void initKeyboardDetect() {
		final LinearLayout llRoot = (LinearLayout) findViewById(R.id.llRoot);
		llRoot.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						Rect r = new Rect();
						llRoot.getRootView().getWindowVisibleDisplayFrame(r);
						
						WindowManager windowManager = getWindowManager();  
						Display display = windowManager.getDefaultDisplay();   
						int screenHeight = display.getHeight(); 

						int heightDifference = screenHeight
								- (r.bottom - r.top);
						if (heightDifference - r.top >= 0) {
							/*
							 * 之前并未保存，第一次获取virtual keyboard 高度，保存到本地，以方便设置Bottom
							 * view 高度， 、 否则可能是因为中英文切换导致virtual
							 * keyboard改变，非真实高度不保存
							 */
							if (heightDifference - r.top > 0) {
								saveKeyboardHeight(heightDifference - r.top);
							}
							/* 根据键盘高度，设置底部高度 */
							if (isResizeInOnLayout) {

								/* 此处判断弹出软键盘时的菜单控制 */
								if (heightDifference - r.top == 0) {
									/* 软键盘收起来了 */
									isMenuOpen = false;
								} else if (heightDifference - r.top > 0) {
									/* 软键盘打开 */
									isMenuOpen = true;
									LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBottom
											.getLayoutParams();
									params.height = heightDifference - r.top;
									flBottom.setLayoutParams(params);
									flBottom.setVisibility(View.GONE);
									getWindow()
											.setSoftInputMode(
													WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
									msgList.scrollToBottom(true);
								}
							}
						}
					}
				});
		if (getKeyboardHeight() < 0) {
			/* 之前未能获取虚拟键盘高度，需通过弹出键盘，计算虚拟键盘高度 */
			etMessage.requestFocus();
			isResizeInOnLayout = true;
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		} else {
			hideSoftInput();
			isMenuOpen = false;
			getWindow()
					.setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
									| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
	}

	private void initReceive() {
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(SEND_EMTOTION_ACTION)) {
					String emotionId = intent.getExtras().getString(EMOTION_ID);
					sendEmotionItem(emotionId);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(SEND_EMTOTION_ACTION);
		registerReceiver(mBroadcastReceiver, filter);
	}

	/**
	 * 编辑框选中
	 */
	private OnTouchListener edtInputTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isResizeInOnLayout = true;
				msgList.scrollToBottom(true);// 消息列表滚到底部，防止多过一页，键盘弹起时部分被覆盖
				resetMenuButtonBg();
				btnExpression
						.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
				btnExpression.setTag("false");
			}
			return false;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		isCurrActivityVisible = true;
		mContactManager.mManId = targetId;
		//清除所有Livechat 通知消息
		LiveChatNotification.newInstance(this).Cancel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isCurrActivityVisible = false;
//		/* 当前界面不可见时，置空当前联系人，可接收push推送 */
//		mContactManager.mManId = "";
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		msgList.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		mLiveChatManager.UnregisterEmotionListener(this);
		mLiveChatManager.UnregisterMessageListener(this);
		mLiveChatManager.UnregisterOtherListener(this);
		mLiveChatManager.UnregisterPhotoListener(this);
		mLiveChatManager.UnregisterVoiceListener(this);
		mLiveChatManager.UnregisterVideoListener(this);
		
		/*翻译解绑*/
		mTranslateManager.UnregisterTranslateListener(this);

		/* 清除正在聊天对象 */
		mContactManager.mManId = "";
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivPhoto:

			break;
		case R.id.llBack:
			if (isMenuOpen) {
				/* 拦截返回键事件，菜单打开时优先关闭menu */
				closeSubMenu();
			} else {
				/* 解决onDestroy调用慢，导致在聊未读状态错误问题 */
				mContactManager.mManId = "";
				finish();
			}
			break;
		case R.id.btnSend:
			sendTextMsg();
			break;
		case R.id.btnExpression:
			onTranslateClick();
			break;
		case R.id.btnTakePhoto:
			onMenuButtonClick(MenuBtnType.USE_CAMERA);
			break;
		case R.id.btnVideo:
			onMenuButtonClick(MenuBtnType.SELECT_VIDEO);
			break;
		case R.id.btnSelectPhoto:
			onMenuButtonClick(MenuBtnType.SELECT_PHOTO);
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_Photo)
					, getString(R.string.LiveChatF_Label_Photo));
			break;
		case R.id.btnVoice:
			onMenuButtonClick(MenuBtnType.RECORD_VOICE);
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_Voice)
					, getString(R.string.LiveChatF_Label_Voice));
			break;
		case R.id.btnEmotion:
			onMenuButtonClick(MenuBtnType.EMOTION_PAN);
			
			// 统计event
			onAnalyticsEvent(getString(R.string.LiveChatF_Category)
					, getString(R.string.LiveChatF_Action_AnimatedEmotions)
					, getString(R.string.LiveChatF_Label_AnimatedEmotions));
			break;
		case R.id.btnMore:
			String[] menu = new String[] { getString(R.string.livechat_chat_history),
					getString(R.string.livechat_chat_translate_setting)};
			if (dropDown != null) {
				dropDown.showAsDropDown(btnMore);
				return;
			}
			dropDown = new MaterialDropDownMenu(ChatActivity.this, menu,
					new MaterialDropDownMenu.OnClickCallback() {

						@Override
						public void onClick(AdapterView<?> adptView, View v,
								int which) {
							switch (which) {
								case 0: {
									LivechatChatHistoryActivity.launchLivechatHistoryActivity(ChatActivity.this, targetId, targetName, targetUrl);
								}break;
								case 1:{
									doTranslateSetting();
								}break;
							}
						}

					}, new Point((int) (220.0f * getResources().getDisplayMetrics().density),LayoutParams.WRAP_CONTENT));

			dropDown.showAsDropDown(btnMore);

			break;
		default:
			break;
		}
	}

	/**
	 * 普通表情菜单点击响应
	 */
	private void onMenuButtonClick(MenuBtnType menutype) {
		/* 隐藏软键盘 */
		isResizeInOnLayout = false;
		isMenuOpen = true;
		msgList.scrollToBottom(true);// 消息列表滚到底部，防止多过一页，键盘弹起时部分被覆盖
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		hideSoftInput();

		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBottom.getLayoutParams();
		int keyboardHeight = (getKeyboardHeight() > UnitConversion.dip2px(this, MIN_HEIGHT)) ? getKeyboardHeight() : UnitConversion.dip2px(this, MIN_HEIGHT);
		params.height = keyboardHeight;
		//if (getKeyboardHeight() > UnitConversion.dip2px(this, MIN_HEIGHT)) {
		//	params.height = getKeyboardHeight();
		//} else {
		//	params.height = UnitConversion.dip2px(this, MIN_HEIGHT);
		//}

		flBottom.setLayoutParams(params);
		flBottom.setVisibility(View.VISIBLE);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		/**
		 * set tap selected status
		 */
		resetMenuButtonBg();

		switch (menutype) {
		case NORMAL_EXPRESSION:

			if (normalExprssionFragment == null)
				normalExprssionFragment = new NormalExprssionFragment();

			if (btnExpression.getTag() == null
					|| btnExpression.getTag().equals("false")) {
				btnExpression.setImageResource(R.drawable.ic_keyboard_grey600_24dp);
				transaction.replace(R.id.flPane, new NormalExprssionFragment());
				btnExpression.setTag("true");
			} else {
				btnExpression.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
				btnExpression.setTag("false");
				showSoftInput();
				isResizeInOnLayout = true;
			}

			break;
		case SELECT_PHOTO:
			if (normalExprssionFragment == null)
				normalExprssionFragment = new NormalExprssionFragment();
			transaction.replace(R.id.flPane, normalExprssionFragment);
			btnSelectPhoto.setImageResource(R.drawable.ic_tag_faces_blue_24dp);
			break;
		case SELECT_VIDEO:
			if (mLivechatVideoListFragment == null)
				mLivechatVideoListFragment = LivechatVideoListFragment.newInstance(targetId);
			transaction.replace(R.id.flPane, mLivechatVideoListFragment);
			btnVideo.setImageResource(R.drawable.ic_video_blue_24dp);
			break;
		case RECORD_VOICE:
			if (voiceRecordFragment == null)
				voiceRecordFragment = new VoiceRecordFragment();
			transaction.replace(R.id.flPane, voiceRecordFragment);
			btnVoice.setImageResource(R.drawable.ic_mic_blue_24dp);
			break;
		case EMOTION_PAN:
			if (emotionFragment == null)
				emotionFragment = new NormalEmotionFragment();
			transaction.replace(R.id.flPane, emotionFragment);
			btnEmotion.setImageResource(R.drawable.ic_premium_emotion_blue_24dp);
			break;
		case USE_CAMERA:
			if (mLivechatAlbumListFragment == null) 
				mLivechatAlbumListFragment = LivechatAlbumListFragment.newInstance(targetId);
			transaction.replace(R.id.flPane, mLivechatAlbumListFragment);
			btnTakePhoto.setImageResource(R.drawable.ic_photo_blue_24dp);
			break;
		default:
			break;
		}
		transaction.commitAllowingStateLoss();
	}
	

	/**
	 * 基础控件监听设置
	 */
	private TextWatcher edtInputWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			if (count > 0) {
                int end = start + count;
                Editable message = etMessage.getEditableText();
                ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);

                for (ImageSpan span : list) {
                    // Get only the emoticons that are inside of the changed
                    // region.
                    int spanStart = message.getSpanStart(span);
                    int spanEnd = message.getSpanEnd(span);
                    if ((spanStart < end) && (spanEnd > start)) {
                        // Add to remove list
                        mEmoticonsToRemove.add(span);
                    }
                }
            }
		}

		public void afterTextChanged(android.text.Editable s) {
			Editable message = etMessage.getEditableText();

            for (ImageSpan span : mEmoticonsToRemove) {
                int start = message.getSpanStart(span);
                int end = message.getSpanEnd(span);

                // Remove the span
                message.removeSpan(span);

                // Remove the remaining emoticon text.
                if (start != end) {
                    message.delete(start, end);
                }
            }
            mEmoticonsToRemove.clear();
		};

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// btnSend.setEnabled(etMessage.getText().length() > 0 ? true :
			// false);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void handleUiMessage(Message msg) {

		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case RECEIVE_CHAT_MESSAGE:{
			LCMessageItem item = (LCMessageItem) msg.obj;
			/* 不是发给当前聊天且在联系人列表，更新未读条数 */
			if (!item.fromId.equals(targetId)
					&& (mLiveChatManager.IsInContactList(item.fromId))) {
				unreadCount++;
				tvUnread.setVisibility(View.VISIBLE);
				tvUnread.setText("" + unreadCount);
			}
			appendMsg(item);
		}break;
		case RECEIVE_CHECK_SEND_MESSAGE_ERROR:{
			/* 底层判断无试聊券，无钱发送邀请失败处理 */
			LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
			List<LCMessageItem> msgCallbackList = (List<LCMessageItem>) msg.obj;
			for (LCMessageItem msgItem : msgCallbackList) {
				LiveChatCallBackItem livechatCallbackItem = new LiveChatCallBackItem(
						msg.arg1, "", "", msgItem);
				msgList.updateSendMessageCallback(livechatCallbackItem);
			}
		}break;
		case PRIVATE_SHOW_PHOTO_DOWNLOADED:{
			/* 私密照大图下载完成更新列表显示 */
			msgList.onPrivatePhotoDownload((LCMessageItem) msg.obj);
		}break;
		case VIDEO_THUMB_PHOTO_DOWNLOAD:{
			/* 私密照大图下载完成更新列表显示 */
			msgList.onVideoPhotoDownload((LCVideoItem) msg.obj);
		}break;
		case SEND_MESSAGE_CALLBACK:
			/* 发送消息成功与否回调更新界面处理 */
			msgList.updateSendMessageCallback((LiveChatCallBackItem) msg.obj);
			break;

		case GET_HISTORY_MESSAGE_UPDATE:
			showMsgBeans(((LCUserItem) msg.obj).getMsgList(), true);
			break;
			
		case TRANSLATE_TEXT_CALLBACK:{
			TranslateCallbackItem item = (TranslateCallbackItem) msg.obj;
			if(!TextUtils.isEmpty(item.seq)){
				//消息翻译处理
				msgList.updateViewByTranslateCallbak(item);
			}else{
				//发送翻译处理
				String content = etMessage.getText().toString();
				if(item.originalText.equals(content)){
					dismissProgressDialog();
					ExpressionImageGetter imageGetter = new ExpressionImageGetter(this, UnitConversion.dip2px(
							this, 28), UnitConversion.dip2px(this, 28));
					etMessage.setText(imageGetter.getExpressMsgHTML(item.tranlatedText));
				}
			}
		}break;
		case RECEIVE_IDENTIFY_CODE: {
			byte[] data = (byte[])msg.obj;
			mDialog = new VerifyCodeDialog(this);
			mDialog.setVerifyBitmap(data);
			mDialog.setOnButtonClick(new OnButtonOkClickListener() {
				
				@Override
				public void onClick(String verifyCode) {
					showProgressDialog(getString(R.string.processing));
					mLiveChatManager.ReplyIdentifyCode(verifyCode);
				}
			});
			if(isCurrActivityVisible){
				mDialog.show();
			}
		}break;
		case REPLY_IDENTIFY_CODE_CALLBACK: {
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			dismissProgressDialog();
			if(response.isSuccess){
				if(mDialog != null){
					mDialog.dismiss();
				}
			}else{
				if(mDialog != null){
					shakeView(mDialog.getVerifyEditText(), true);
				}
			}
		}break;	
		case RECEIVE_PHOTO_VIEW_IDENTIFY: {
			mLiveChatManager.BuildAndInsertSystemMsg(targetId, StringUtil.mergeMultiString(targetName, " ", getString(R.string.livechat_photo_view_identify, (String)msg.obj)));
		}break;
		case RECEIVE_VIDEO_VIEW_IDENTIFY: {
			mLiveChatManager.BuildAndInsertSystemMsg(targetId, StringUtil.mergeMultiString(targetName, " ", getString(R.string.livechat_video_view_identify, (String)msg.obj)));
		}break;
		case END_CHAT: {
			finish();
		}break;
		default:
			break;
		}
	
	}

	/**
	 * 默认读取配置中虚拟键盘高度，如果不等于-1，表示已记录不在更新
	 * 
	 * @return
	 */
	private int getKeyboardHeight() {
		SharedPreferences preference = getSharedPreferences("virtualKeyboard",
				MODE_PRIVATE);
		return preference.getInt("keyboardheight", -1);
	}

	/**
	 * 提交保存键盘高度
	 * 
	 * @param height
	 */
	private void saveKeyboardHeight(int height) {
		SharedPreferences preference = getSharedPreferences("virtualKeyboard",
				MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putInt("keyboardheight", height);
		editor.commit();
	}

	private enum MenuBtnType {
		NORMAL_EXPRESSION, SELECT_PHOTO, RECORD_VOICE, EMOTION_PAN, USE_CAMERA, SELECT_VIDEO
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && isMenuOpen) {
			/* 拦截返回键事件，菜单打开时优先关闭menu */
			closeSubMenu();
			return true;
		}
		/* 解决onDestroy调用慢，导致在聊未读状态错误问题 */
		mContactManager.mManId = "";
		return super.onKeyDown(keyCode, event);
	}

	private OnTouchListener onMessageListTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_UP) {
				closeSubMenu();
			}
			return false;
		}
	};
	
	/**
	 * 点击菜单弹出设置菜单
	 */
	private void doTranslateSetting(){
		String[] items = null;
		int currSelect = -1;
		String defaultLang = mTranslateManager.getDefaultTranslateLang(targetId);
		List<TransLangBean> transList = mTranslateManager.parseDestLangList();
		if(transList != null){
			items = new String[transList.size() + 1];
			int i;
			for(i=0; i<transList.size(); i++){
				items[i] = transList.get(i).langDesc;
				if(transList.get(i).langLabel.equals(defaultLang)){
					currSelect = i;
				}
			}
			items[i] = getString(R.string.livechat_chat_translate_setting_notrans);
			if(currSelect < 0){
				currSelect = i;
			}
			MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(this, items,
					new MaterialDialogSingleChoice.OnClickCallback() {

						@Override
						public void onClick(AdapterView<?> adptView, View v,
								int which) {
							onTranslateSetting(which);
						}
					}, currSelect);
			dialog.setTitle(getString(R.string.livechat_chat_translate_setting_title));
			dialog.show();
		}
	}
	
	/**
	 * 默认翻译设定
	 * @param position
	 */
	private void onTranslateSetting(int position){
		List<TransLangBean> transList = mTranslateManager.parseDestLangList();
		if((position >= 0) && (position < transList.size())){
			mTranslateManager.saveDefaultTranslateLang(targetId, transList.get(position).langLabel);
			updateAllTextTranslate(transList.get(position).langLabel);
		}else{
			mTranslateManager.saveDefaultTranslateLang(targetId, "");
			clearTranslation();
		}
	}
	
	/**
	 * 关闭翻译功能
	 */
	private void clearTranslation(){
		msgList.clearTranslation();
	}
	
	/**
	 * 翻译所有已收消息
	 * @param transLang
	 */
	private void updateAllTextTranslate(String transLang){
		msgList.translateAllInTextMsg(transLang);
	}
	
	/**
	 * 翻译点击
	 */
	private void onTranslateClick(){
		String content = etMessage.getText().toString();
		if(!TextUtils.isEmpty(content)){
			showProgressDialog(getString(R.string.livechat_translating));
			mTranslateManager.translateText("", "en", content);
		}else{
			shakeView(etMessage, true);
		}
	}
	

	/**
	 * 关闭子菜单
	 */
	private void closeSubMenu() {
		isMenuOpen = false;
		hideSoftInput();
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flBottom
				.getLayoutParams();
		params.height = 0;
		flBottom.setLayoutParams(params);

		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.flPane);
		if (fragment != null) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.remove(fragment);
			transaction.commitAllowingStateLoss();
		}

		/* 还原按钮未选中图标 */
		resetMenuButtonBg();
	}

	/**
	 * 功能按钮切换的过程中，还原按钮图标状态
	 */
	private void resetMenuButtonBg() {
		btnTakePhoto.setImageResource(R.drawable.ic_photo_grey600_24dp);
		btnVideo.setImageResource(R.drawable.ic_video_grey600_24dp);
		btnSelectPhoto.setImageResource(R.drawable.ic_tag_faces_grey600_24dp);
		
		btnVoice.setImageResource(R.drawable.ic_mic_grey600_24dp);
		btnEmotion.setImageResource(R.drawable.ic_premium_emotion_24dp);
	}
	
	/**
	 * 当开始录音或回收时，关掉所有录音
	 */
	public void stopAllVoicePlaying() {
		if (msgList != null) {
			msgList.stopPlaying();
		}
	}
	
	/**
	 * 选择表情
	 * 
	 * @param val
	 */
	@SuppressWarnings("deprecation")
	public void selectEmotion(int val) {
		int imgId = 0;
		try {
			imgId = R.drawable.class.getDeclaredField("e" + val).getInt(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (imgId != 0) {
			int textSize = getResources().getDimensionPixelSize(
					R.dimen.expre_drawable_size);
			Drawable drawable = getResources().getDrawable(imgId);
			drawable.setBounds(0, 0, textSize, textSize);
			ImageSpan imgSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
			String code = "[img:" + val + "]";
			SpannableString ss = new SpannableString(code);
			ss.setSpan(imgSpan, 0, code.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			etMessage.getEditableText().insert(etMessage.getSelectionStart(),
					ss);
		}
	}
	
	/**
	 * 获取聊天历史返回，更新消息列表
	 * 
	 * @param userItem
	 */
	private void onGetHistoryMessageCallback(LCUserItem userItem) {
		if ((userItem != null) && (userItem.userId != null)
				&& (userItem.userId.equals(targetId))) {
			/* 当前用户的消息历史返回才处理 */
			Message msg = Message.obtain();
			msg.what = GET_HISTORY_MESSAGE_UPDATE;
			msg.obj = userItem;
			sendUiMessage(msg);
		}
	}
	
	/**
	 * 发送消息成功与否回调
	 * 
	 * @param errType
	 * @param item
	 */
	private void onSendMessageUpdate(LiveChatCallBackItem item) {
		Message msg = Message.obtain();
		msg.what = SEND_MESSAGE_CALLBACK;
		msg.obj = item;
		sendUiMessage(msg);
	}
	
	/**
	 * 收到聊天信息回调主界面处理
	 * 
	 * @param item
	 */
	private void onReceiveMsgUpdate(LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = RECEIVE_CHAT_MESSAGE;
		msg.obj = item;
		sendUiMessage(msg);
	}
	
	/**
	 * 底层检测试聊有钱与否，返回出错信息列表处理
	 * 
	 * @param errType
	 * @param msgList
	 */
	private void onReceiveMsgList(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		Message msg = Message.obtain();
		msg.what = RECEIVE_CHECK_SEND_MESSAGE_ERROR;
		msg.arg1 = errType.ordinal();
		msg.obj = msgList;
		sendUiMessage(msg);
	}
	
	/**
	 * Livechat 私密照下大图成功，更新列表
	 * 
	 * @param item
	 */
	private void onGetShowPhotoSuccess(LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = PRIVATE_SHOW_PHOTO_DOWNLOADED;
		msg.obj = item;
		sendUiMessage(msg);
	}
	
	/********************* message relative *************************************/
	
	/**
	 * 显示消息列表
	 * 
	 * @param msgBeans
	 * @param smooth
	 */
	private void showMsgBeans(List<LCMessageItem> msgBeans, boolean smooth) {
		msgList.replaceAllRow(msgBeans);
		msgList.scrollToBottom(smooth);
	}
	
	/**
	 * 检测是否指定翻译，翻译是否在线，是否绑定，否则不能发送消息
	 * @return
	 */
	private boolean checkForTranslte(){
		boolean canSend = true;
		LCSelfInfo info = mLiveChatManager.GetSelfInfo();
		if((info != null) && (info.mNeedTrans)){
			//需要翻译
			if((info.mTransStatus == UserStatusType.USTATUS_ONLINE) && (info.mTransBind)){
				//翻译在线且已经绑定
				canSend = true;
			}else{
				canSend = false;
			}
		}
		return canSend;
	}
	
	public boolean checkMsgBeforeSend(MessageType type){
		if(!checkForTranslte()){
			mLiveChatManager.BuildAndInsertSystemMsg(targetId, getString(R.string.livechat_translate_offline_unbind));
			return false;
		}
		
		CanSendErrType errType = mLiveChatManager.CanSendMessage(targetId, type);
		if(errType != CanSendErrType.OK){
			String message = "";
			switch (type) {
			case Text:
				message = getString(R.string.livechat_send_textmsg_frequently);
				break;
			case Emotion:
				message = getString(R.string.livechat_send_emotion_frequently);
				break;
			case Voice:{
				if(errType == CanSendErrType.NoInChat){
					message = getString(R.string.livechat_can_not_send_voice_message_before_the_conversation_has_started);
				}else if(errType == CanSendErrType.SendMsgFrequency){
					message = getString(R.string.livechat_send_voice_frequently);
				}
				
			}break;
			case Photo:{
				if(errType == CanSendErrType.NoInChat){
					message = getString(R.string.livechat_can_not_send_photo_before_the_conversation_has_started);
				}else if(errType == CanSendErrType.SendMsgFrequency){
					message = getString(R.string.livechat_send_photo_frequently);
				}
				
			}break;
			case Video:{
				if(errType == CanSendErrType.NoInChat){
					message = getString(R.string.livechat_can_not_send_video_before_the_conversation_has_started);
				}else if(errType == CanSendErrType.SendMsgFrequency){
					message = getString(R.string.livechat_send_video_frequently);
				}
				
			}break;
			default:
				break;
			}
			cannotSendNotify(message);
			return false;
		}
		return true;
	}
	
	/**
	 * 发送文本及普通表情
	 */
	private void sendTextMsg() {
		String content = etMessage.getText().toString();
		if (content.length() < 1) {
			shakeView(etMessage, true);
			return;
		}
		if(checkMsgBeforeSend(MessageType.Text)){
			LCMessageItem item = mLiveChatManager.SendMessage(chatTarget.userId,
					content);
			appendMsg(item);
			if (item != null) {
				etMessage.setText("");
			}
		}
	}
	
	/**
	 * 发送高级表情
	 * @param emotionId
	 */
	private void sendEmotionItem(String emotionId) {
		if(checkMsgBeforeSend(MessageType.Emotion)){
			LCMessageItem item = mLiveChatManager.SendEmotion(chatTarget.userId,
					emotionId);
			appendMsg(item);
		}
	}
	
	/**
	 * 发送privatePhoto
	 * 
	 * @param photoPath
	 *            图片本地地址
	 */
	public void sendPrivatePhoto(String photoId) {
		if(checkMsgBeforeSend(MessageType.Photo)){
			LCMessageItem item = mLiveChatManager.SendPhoto(chatTarget.userId,
					photoId);
			appendMsg(item);
		}
	}
	
	/**
	 * 发送privatePhoto
	 * 
	 * @param photoPath
	 *            图片本地地址
	 */
	public void sendVideo(String videoId) {
		if(checkMsgBeforeSend(MessageType.Video)){
			LCMessageItem item = mLiveChatManager.SendVideo(chatTarget.userId, videoId);
			appendMsg(item);
		}
	}
	
	/**
	 * 发送
	 * @param savePath
	 * @param recordTime
	 */
	public void sendVoiceItem(String savePath, long recordTime) {
		
		if(recordTime < 1){
			//录音时长小于1秒，提示不发送
			Toast.makeText(this, getString(R.string.livechat_record_voice_too_short), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(checkMsgBeforeSend(MessageType.Voice)){
			LCMessageItem item = mLiveChatManager.SendVoice(chatTarget.userId,
					savePath, "aac", (int) recordTime);
			appendMsg(item);
		}
	}
	
	/**
	 * 通知界面刷新消息列表
	 * @param msgBean
	 * @return
	 */
	public View appendMsg(LCMessageItem msgBean) {
		// 更新视图
		if (msgBean != null) {
			mContactManager.onSendMessage(msgBean);
			View rowView = msgList.addRow(msgBean);
			msgList.scrollToBottom(true);
			return rowView;
		} else {
			MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			dialog.setMessage(getString(R.string.livechat_kickoff_by_sever_update));
			dialog.addButton(dialog.createButton(getString(R.string.login),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							LoginManager.getInstance().Logout(OperateType.MANUAL);
							Intent jumpIntent = new Intent(ChatActivity.this, HomeActivity.class);
							jumpIntent.putExtra(HomeActivity.NEW_INTENT_LOGOUT, true);
							jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(jumpIntent);
							finish();
						}
					}));
			dialog.addButton(dialog.createButton(
					getString(R.string.cancel), null));

			dialog.show();
			return null;
		}
	}
	
	/**
	 * 消息无法发送提示
	 * @param message
	 */
	public void cannotSendNotify(String message){
		mLiveChatManager.BuildAndInsertSystemMsg(targetId, message);
	}


	/************************************ livechat client callback *************************************************************/
	@Override
	public void OnSendVoice(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), errno, errmsg, item);
			onSendMessageUpdate(callBack);
		}		
	}


	@Override
	public void OnGetVoice(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		
	}


	@Override
	public void OnRecvVoice(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}		
	}


	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), errno, errmsg, item);
			onSendMessageUpdate(callBack);
		}		
	}


	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if (errType == LiveChatErrType.Success) {
			/* 在购买私密照界面购买清晰图成功，回调更新界面 */
			if (!isCurrActivityVisible && item.getPhotoItem().charge) {
				if ((item != null) && (item.getUserItem() != null)
						&& (item.getUserItem().userId != null)
						&& (item.getUserItem().userId.equals(targetId))) {
					onGetShowPhotoSuccess(item);
				}
			}
		}		
	}


	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}		
	}


	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg,
			LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, EmotionConfigItem item) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), null, errmsg, item);
			onSendMessageUpdate(callBack);
		}		
	}


	@Override
	public void OnRecvEmotion(LCMessageItem item) {
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}		
	}


	@Override
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), null, errmsg, item);
			onSendMessageUpdate(callBack);
		}		
	}


	@Override
	public void OnRecvMessage(LCMessageItem item) {
		if (chatTarget != null && item != null) {
			if (item.fromId.equals(chatTarget.userId)) {
				onReceiveMsgUpdate(item);
			}
		}		
	}


	@Override
	public void OnRecvWarning(LCMessageItem item) {
		/* 普通warning及以下错误（余额不足等） */
		if (item.getUserItem().userId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}		
	}


	@Override
	public void OnRecvEditMsg(String fromId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		/* 系统通知，以消息的形式（试聊结束等） */
		if (item.fromId.equals(chatTarget.userId)) {
			onReceiveMsgUpdate(item);
		}		
	}


	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		/* 底层检测是否有试聊券，是否有钱聊天，如果没有直接此处返回错误 */
		onReceiveMsgList(errType, msgList);		
	}


	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		/* 拿历史消息返回，需更新消息列表 */
		if (success) {
			onGetHistoryMessageCallback(userItem);
		}		
	}


	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) {
		Message msg = Message.obtain();
		msg.what = REPLY_IDENTIFY_CODE_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(errType == LiveChatErrType.Success ? true:false, "", errmsg, null);
		msg.obj = response;
		sendUiMessage(msg);
	}


	@Override
	public void OnRecvIdentifyCode(byte[] data) {
		Message msg = Message.obtain();
		msg.what = RECEIVE_IDENTIFY_CODE;
		msg.obj = data;
		sendUiMessage(msg);
	}


	@Override
	public void OnRecvTalkEvent(LCUserItem item) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnContactListChange() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg, int seq,
			LiveChatTalkUserListItem[] list) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnTransStatusChange() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		if((userItem != null) && (targetId.equals(userItem.userId))){
			Message msg = Message.obtain();
			msg.what = RECEIVE_PHOTO_VIEW_IDENTIFY;
			msg.obj = photoDesc;
			sendUiMessage(msg);
		}
		
	}

	/**************************  translate callback ****************************/
	@Override
	public void OnTranslateText(String seq, boolean isSuccess,
			String orignalText, String text) {
		Message msg = Message.obtain();
		msg.what = TRANSLATE_TEXT_CALLBACK;
		TranslateCallbackItem item = new TranslateCallbackItem(isSuccess, seq, orignalText, text);
		msg.obj = item;
		sendUiMessage(msg);
	}

	/************************** Video Relative ****************************/
	@Override
	public void OnCheckSendVideo(
			LiveChatErrType errType,
			com.qpidnetwork.request.OnLCCheckSendVideoCallback.ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCVideoItem videoItem) {
		
	}


	@Override
	public void OnSendVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if ((item != null) && (item.getUserItem() != null)
				&& (item.getUserItem().userId != null)
				&& (item.getUserItem().userId.equals(targetId))) {
			LiveChatCallBackItem callBack = new LiveChatCallBackItem(
					errType.ordinal(), errno, errmsg, item);
			onSendMessageUpdate(callBack);
		}
	}


	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem item) {
		if ((errType == LiveChatErrType.Success) && (item != null)) {
				Message msg = Message.obtain();
				msg.what = VIDEO_THUMB_PHOTO_DOWNLOAD;
				msg.obj = item;
				sendUiMessage(msg);
		}
	}


	@Override
	public void OnGetVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
	}


	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) {
		
	}


	@Override
	public void OnRecvShowVideo(LCUserItem userItem, String videoId,
			String videoDesc) {
		if((userItem != null) && (targetId.equals(userItem.userId))){
			Message msg = Message.obtain();
			msg.what = RECEIVE_VIDEO_VIEW_IDENTIFY;
			msg.obj = videoDesc;
			sendUiMessage(msg);
		}
	}
}
