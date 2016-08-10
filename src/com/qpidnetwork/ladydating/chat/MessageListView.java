package com.qpidnetwork.ladydating.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.downloader.EmotionPlayImageDownloader;
import com.qpidnetwork.ladydating.chat.downloader.EmotionPlayImageDownloader.OnEmotionPlayImageDownloadListener;
import com.qpidnetwork.ladydating.chat.downloader.LivechatVideoThumbPhotoDownloader;
import com.qpidnetwork.ladydating.chat.downloader.LivechatVoiceDownloader;
import com.qpidnetwork.ladydating.chat.downloader.PrivatePhotoDownloader;
import com.qpidnetwork.ladydating.chat.emotion.EmotionPlayer;
import com.qpidnetwork.ladydating.chat.picture.LivechatPrivatePhotoPreviewActivity;
import com.qpidnetwork.ladydating.chat.picture.PrivatePhotoPriviewBean;
import com.qpidnetwork.ladydating.chat.translate.TranslateCallbackItem;
import com.qpidnetwork.ladydating.chat.translate.TranslateManager;
import com.qpidnetwork.ladydating.chat.video.VideoPlayActivity;
import com.qpidnetwork.ladydating.chat.voice.VoicePlayerManager;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LCMessageItem.StatusType;
import com.qpidnetwork.livechat.LCTextItem;
import com.qpidnetwork.livechat.LCVideoItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;

public class MessageListView extends ScrollLayout implements
		View.OnClickListener, OnEmotionPlayImageDownloadListener {

	private ReadWriteLock lock = new ReentrantReadWriteLock();

	private List<LCMessageItem> beanList = new ArrayList<LCMessageItem>();
	private HashMap<Integer, Integer> mPositionMap = new HashMap<Integer, Integer>();// 消息Id
																						// 与
																						// position索引，方便界面更新及MessgaeItem更新
	private LayoutInflater mLayoutInflater;
	private ExpressionImageGetter imageGetter;/* 表情图片获取 */

	private LiveChatManager mLiveChatManager;
	private Context mContext;

	private VoicePlayerManager mVoicePlayerManager;
	
	private boolean isDestroyed = false; // 是否被回收，防止退出时高级表情仍回调导致高表播放，资源无法回收

	private List<LCMessageItem> currVisibleList = new ArrayList<LCMessageItem>();// 记录当前可见的item
	private boolean isScrolled = false;// 存储当前列表是否滚动过，解决未滚动情况下收到高级表情，下载完不播放问题
	
	private TranslateManager mTranslateManager;//翻译相关处理

	public MessageListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MessageListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		isDestroyed = false;
		mLayoutInflater = LayoutInflater.from(context);
		mLiveChatManager = LiveChatManager.newInstance(null);
		imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(
				context, 28), UnitConversion.dip2px(context, 28));
		mVoicePlayerManager = VoicePlayerManager.getInstance(context);
		mTranslateManager = TranslateManager.getInstance();
	}

	public void replaceAllRow(List<LCMessageItem> beanList) {
		LCMessageItem[] msgBeanArr = beanList
				.toArray(new LCMessageItem[beanList.size()]);
		lock.writeLock().lock();
		try {
			getContainer().removeAllViews();
			this.beanList.clear();
			this.mPositionMap.clear();
			if (beanList != null) {
				for (LCMessageItem bean : msgBeanArr) {
					addRowInternal(bean);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public View addRow(LCMessageItem bean) {
		lock.readLock().lock();
		try {
			return addRowInternal(bean);
		} finally {
			lock.readLock().unlock();
		}
	}

	public View addRowInternal(LCMessageItem bean) {
		View row = null;
		int position = beanList.size();
		if (beanList != null) {
			beanList.add(bean);
			mPositionMap.put(bean.msgId, Integer.valueOf(position));
		}
		switch (bean.sendType) {
		case Recv:
			switch (bean.msgType) {
			case Text:
				row = getTextMessageViewIn(bean, position);
				break;
			case Emotion:
				row = getEmotionViewIn(bean, position);
				break;
			case Voice:
				row = getVoiceViewIn(bean, position);
				break;
			case Photo:
				row = getPhotoViewIn(bean, position);
				break;
			case Video:
				break;
			case Warning:
				row = getWarningView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		case Send:
			switch (bean.msgType) {
			case Text:
				row = getTextMessageViewOut(bean);
				break;
			case Warning:
				row = getWarningView(bean);
				break;
			case Emotion:
				row = getEmotionViewOut(bean, position);
				break;
			case Voice:
				row = getVoiceViewOut(bean, position);
				break;
			case Photo:
				row = getPhotoViewOut(bean, position);
				break;
			case Video:
				row = getVideoViewOut(bean, position);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		case System:
			switch (bean.msgType) {
			case Warning:
				row = getWarningView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		case Unknow:
			switch (bean.msgType) {
			case Warning:
				row = getWarningView(bean);
				break;
			case System:
				row = getSystemMessageView(bean);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		if (row != null) {
			row.setTag(position);
			getContainer().addView(row);
			if (bean.msgType == MessageType.Emotion) {
				/* add 完成再加载，防止本地有图主线程回调回去，view未add进container导致死机 */
				new EmotionPlayImageDownloader().downloadEmotionPlayImage(
						bean, this);
			}
		}
		return row;
	}

	/**
	 * 收到警告消息（包括无点提示等）
	 * 
	 * @param bean
	 * @return
	 */
	private View getWarningView(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_warning_tips, null);
		TextView msgView = (TextView) row.findViewById(R.id.tvNotifyMsg);
		msgView.setText(bean.getWarningItem().message);
		return row;
	}

	/**
	 * 
	 * 系统消息View
	 * 
	 * @param bean
	 * @return
	 */
	private View getSystemMessageView(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_normal_notify, null);
		TextView notifyView = (TextView) row.findViewById(R.id.tvNotifyMsg);
		notifyView.setText(bean.getSystemItem().message);
		return row;
	}

	/**
	 * 初始化收到文本 来信息view
	 * 
	 * @param bean
	 */
	private View getTextMessageViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_message, null);
		TextView msgView = (TextView) row.findViewById(R.id.chat_message);
		TextView transTextView = (TextView) row.findViewById(R.id.chat_message_trans);
//		/*修改文本输入最大宽度设置*/
//		int maxWidth = SystemUtil.getDisplayMetrics(mContext).widthPixels - UnitConversion.dip2px(mContext, 16*2 + 24 + 36 + 16);
//		msgView.setMaxWidth(maxWidth);
//		transTextView.setMaxWidth(maxWidth);
		
		MaterialProgressBar pbDownload = (MaterialProgressBar) row.findViewById(R.id.pbDownload);
		ImageButton btnError = (ImageButton) row.findViewById(R.id.btnError);
		btnError.setTag(position);
		btnError.setOnClickListener(onTranslateErrorClick);
		
		String defaultLang = mTranslateManager.getDefaultTranslateLang(bean.getUserItem().userId);
		if(TextUtils.isEmpty(defaultLang)){
			//无需翻译
			msgView.setText(imageGetter.getExpressMsgHTML(bean.getTextItem().message));
		}else{
			//需要翻译
			String originalText = mContext.getResources().getString(R.string.livechat_original_text_message, bean.getTextItem().message);
			msgView.setText(imageGetter.getExpressMsgHTML(originalText));
			String translateResult = mTranslateManager.getMsgTranslateResult(String.valueOf(bean.msgId), defaultLang);
			if(!TextUtils.isEmpty(translateResult)){
				//已经翻译过
				transTextView.setVisibility(View.VISIBLE);
				transTextView.setText(mContext.getResources().getString(R.string.livechat_translate_text_message, translateResult));
			}else{
				//请求翻译接口
				pbDownload.setVisibility(View.VISIBLE);
				mTranslateManager.translateText(String.valueOf(bean.msgId), defaultLang, bean.getTextItem().message);
			}
		}
		return row;
	}

	/**
	 * 初始化收到Emotion view
	 * 
	 * @param bean
	 * @return
	 */
	private View getEmotionViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_emotion, null);
		return row;
	}

	/**
	 * 获取收到照片View
	 * 
	 * @param bean
	 * @return
	 */
	private View getPhotoViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_photo, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		ImageView ivPrivatePhoto = (ImageView) row
				.findViewById(R.id.ivPrivatePhoto);
		ImageButton btnError = (ImageButton) row.findViewById(R.id.btnError);
		TextView tvPhotoDesc = (TextView)row.findViewById(R.id.tvPhotoDesc);
		tvPhotoDesc.setText(bean.getPhotoItem().photoDesc);
		tvPhotoDesc.setVisibility(View.GONE);
		
		ivPrivatePhoto.setTag(position);
		btnError.setTag(position);
		ivPrivatePhoto.setOnClickListener(this);
		new PrivatePhotoDownloader(mContext).displayPrivatePhoto(
				ivPrivatePhoto, pbDownload, bean, btnError);
		return row;
	}

	/**
	 * 获取收到语音View
	 * 
	 * @param bean
	 * @return
	 */
	private View getVoiceViewIn(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_in_voice, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		ImageButton btnError = (ImageButton) row.findViewById(R.id.btnError);
		TextView timeView = (TextView) row.findViewById(R.id.chat_sound_time);
		timeView.setText(bean.getVoiceItem().timeLength + "''");
		timeView.setTag(position);
		timeView.setOnClickListener(this);
		new LivechatVoiceDownloader(mContext).downloadAndPlayVoice(pbDownload,
				btnError, bean);
		return row;
	}

	/**
	 * 初始化发送文本 来信息view
	 * 
	 * @param bean
	 */
	private View getTextMessageViewOut(LCMessageItem bean) {
		View row = mLayoutInflater.inflate(R.layout.item_out_message, null);
		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		TextView msgView = (TextView) row.findViewById(R.id.chat_message);
		LCTextItem textItem = bean.getTextItem();
		msgView.setText(imageGetter.getExpressMsgHTML(textItem.message));
		if (bean.getTextItem().illegal) {
			/* 非法的，显示警告 */
			row.findViewById(R.id.includeWaring).setVisibility(View.VISIBLE);
			((TextView) row.findViewById(R.id.tvNotifyMsg)).setText(mContext
					.getResources().getString(
							R.string.livechat_lady_illeage_message));
		}
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			LiveChatCallBackItem callBackItem = new LiveChatCallBackItem(bean.errType.ordinal(), bean.errno, bean.errmsg, bean);
			row.findViewById(R.id.btnError).setTag(callBackItem);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					onSendMessageError);
		}
		return row;
	}

	/**
	 * 初始化发送Emotion view
	 * 
	 * @param bean
	 * @return
	 */
	private View getEmotionViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_emotion, null);
		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			LiveChatCallBackItem callBackItem = new LiveChatCallBackItem(bean.errType.ordinal(), bean.errno, bean.errmsg, bean);
			row.findViewById(R.id.btnError).setTag(callBackItem);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					onSendMessageError);
		}
		return row;
	}

	/**
	 * 获取发送照片View
	 * 
	 * @param bean
	 * @return
	 */
	private View getPhotoViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_photo, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		ImageView ivPrivatePhoto = (ImageView) row
				.findViewById(R.id.ivPrivatePhoto);
		ivPrivatePhoto.setTag(position);
		ivPrivatePhoto.setOnClickListener(this);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			LiveChatCallBackItem callBackItem = new LiveChatCallBackItem(bean.errType.ordinal(), bean.errno, bean.errmsg, bean);
			row.findViewById(R.id.btnError).setTag(callBackItem);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					onSendMessageError);
		}

		new PrivatePhotoDownloader(mContext).displayPrivatePhoto(
				ivPrivatePhoto, null, bean, null);
		return row;
	}
	
	/**
	 * 获取发送视频View
	 * 
	 * @param bean
	 * @return
	 */
	private View getVideoViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_video, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		RelativeLayout rlVideo = (RelativeLayout) row.findViewById(R.id.rlVideo);
		rlVideo.setTag(position);
		rlVideo.setOnClickListener(this);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			LiveChatCallBackItem callBackItem = new LiveChatCallBackItem(bean.errType.ordinal(), bean.errno, bean.errmsg, bean);
			row.findViewById(R.id.btnError).setTag(callBackItem);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					onSendMessageError);
		}
		
		ImageView ivVideoPhoto = (ImageView)row.findViewById(R.id.ivVideoPhoto);
		ivVideoPhoto.setImageDrawable(new ColorDrawable(Color.parseColor("#16000000")));
		new LivechatVideoThumbPhotoDownloader(mContext).DisplayImage(ivVideoPhoto, bean.getVideoItem().videoItem.videoId, 
				VideoPhotoType.Big, UnitConversion.dip2px(mContext, 112), UnitConversion.dip2px(mContext, 112));
		return row;
	}

	/**
	 * 获取发送语音View
	 * 
	 * @param bean
	 * @return
	 */
	private View getVoiceViewOut(LCMessageItem bean, int position) {
		View row = mLayoutInflater.inflate(R.layout.item_out_voice, null);

		MaterialProgressBar pbDownload = (MaterialProgressBar) row
				.findViewById(R.id.pbDownload);
		if (bean.statusType == StatusType.Processing) {
			pbDownload.setVisibility(View.VISIBLE);
		} else if (bean.statusType == StatusType.Fail) {
			pbDownload.setVisibility(View.GONE);
			LiveChatCallBackItem callBackItem = new LiveChatCallBackItem(bean.errType.ordinal(), bean.errno, bean.errmsg, bean);
			row.findViewById(R.id.btnError).setTag(callBackItem);
			row.findViewById(R.id.btnError).setVisibility(View.VISIBLE);
			row.findViewById(R.id.btnError).setOnClickListener(
					onSendMessageError);
		}
		TextView timeView = (TextView) row.findViewById(R.id.chat_sound_time);
		timeView.setText(bean.getVoiceItem().timeLength + "''");
		timeView.setTag(position);
		timeView.setOnClickListener(this);
		new LivechatVoiceDownloader(mContext).downloadAndPlayVoice(null, null,
				bean);
		return row;
	}

	@Override
	public void onClick(View v) {
		int vid = v.getId();
		if (vid == R.id.ivPrivatePhoto) {
			/* 点击图片看大图处理 */
			onPrivatePhotoClick(v);
		} else if (vid == R.id.chat_sound_time) {
			/* 语音Item，点击播放语音 */
			onVoiceItemClick(v);
		}else if(vid == R.id.rlVideo){
			/*点击视频查看视频详情*/
			onVideoItemClick(v);
		}
	}

	/**
	 * 私密照点击看大图
	 */
	private void onPrivatePhotoClick(View v) {
		int postion = getPosition(v);
		if (beanList.size() > postion) {
			LCMessageItem currItem = beanList.get(postion);
			List<LCMessageItem> mPrivatePhotoList = new ArrayList<LCMessageItem>();
			for (LCMessageItem item : beanList) {
				if (item.msgType == MessageType.Photo) {
					mPrivatePhotoList.add(item);
				}
			}
			if (mPrivatePhotoList.contains(currItem)) {
				/* private photo item存在，打开预览 */
				mContext.startActivity(LivechatPrivatePhotoPreviewActivity.getIntent(
						mContext,
						new PrivatePhotoPriviewBean(mPrivatePhotoList
								.indexOf(currItem), mPrivatePhotoList)));
			}
		}
	}

	/**
	 * 语音Item点击处理
	 * 
	 * @param v
	 */
	private void onVideoItemClick(View v) {
		int postion = getPosition(v);
		if (beanList.size() > postion) {
			LCMessageItem item = beanList.get(postion);
			VideoPlayActivity.launchVideoPlayActivity(mContext, item.msgId, item.getUserItem().userId);
		}
	}
	
	/**
	 * 视频Item点击处理
	 * 
	 * @param v
	 */
	private void onVoiceItemClick(View v) {
		int postion = getPosition(v);
		if (beanList.size() > postion) {
			LCMessageItem item = beanList.get(postion);
			mVoicePlayerManager.startPlayVoice(v, item.msgId,
					item.getVoiceItem().filePath);
		}
	}

	/**
	 * 发送消息回调处理
	 * 
	 * @param errType
	 * @param item
	 */
	public void updateSendMessageCallback(final LiveChatCallBackItem callback) {
		LCMessageItem item = (LCMessageItem) callback.body;
		LiveChatErrType errType = LiveChatErrType.values()[callback.errType];
		/*
		 * 解决Livechat聊天消息由于底层处理试聊及是否有钱等情况成功后才返回错误提示，在这个过程中受到女士邀请，
		 * 导致失败返回列表消息中包含了收到的文本消息，导致无进度条异常失败
		 */
		if (item != null && (item.sendType == SendType.Send)) {
			if (mPositionMap.containsKey(item.msgId)) {
				/* 音频暂时单独处理 */
				int position = mPositionMap.get(item.msgId);
				/* 更新数据 */
				beanList.remove(position);
				beanList.add(position, item);
				/* 更新界面 */
				View row = getContainer().getChildAt(position);
				MaterialProgressBar pbDownload = (MaterialProgressBar) row
						.findViewById(R.id.pbDownload);
				ImageButton btnError = (ImageButton) row
						.findViewById(R.id.btnError);
				if (pbDownload != null) {
					pbDownload.setVisibility(View.GONE);
				}
				if (errType != LiveChatErrType.Success) {
					//发送消息错误统一处理
					btnError.setVisibility(View.VISIBLE);
					btnError.setTag(callback);
					btnError.setOnClickListener(onSendMessageError);
				}
			}
		}
	}

	/**
	 * 私密照购图界面下大图成功更新
	 * 
	 * @param bean
	 */
	public void onPrivatePhotoDownload(LCMessageItem bean) {
		if (bean != null) {
			if (mPositionMap.containsKey(bean.msgId)) {
				int position = mPositionMap.get(bean.msgId);
				if (!StringUtil.isEmpty(bean.getPhotoItem().showSrcFilePath)) {
					beanList.get(position).getPhotoItem().showSrcFilePath = bean
							.getPhotoItem().showSrcFilePath;
					/* 更新界面 */
					View row = getContainer().getChildAt(position);
					row.findViewById(R.id.btnError).setVisibility(View.GONE);
					row.findViewById(R.id.pbDownload).setVisibility(View.GONE);
					ImageView privatePhoto = (ImageView) row
							.findViewById(R.id.ivPrivatePhoto);
					Bitmap bitmap = ImageUtil
							.decodeHeightDependedBitmapFromFile(
									bean.getPhotoItem().showSrcFilePath,
									UnitConversion.dip2px(mContext, 112));
					if (bitmap != null) {
						privatePhoto.setImageBitmap(ImageUtil
								.get2DpRoundedImage(mContext, bitmap));
					}
				}
			}
		}
	}
	
	/**
	 * 私密照购图界面下大图成功更新
	 * 
	 * @param bean
	 */
	public void onVideoPhotoDownload(LCVideoItem item) {
		if (item != null && beanList != null) {
			for(LCMessageItem msgItem : beanList){
				if((msgItem.msgType == MessageType.Video) && (item.videoId.equals(msgItem.getVideoItem().videoItem))){
					msgItem.getVideoItem().videoItem.videoPath = item.bigPhotoPath;
					/*更新界面*/
					int position = mPositionMap.get(msgItem.msgId);
					View row = getContainer().getChildAt(position);
					ImageView ivVideoPhoto = (ImageView) row.findViewById(R.id.ivVideoPhoto);
					new LivechatVideoThumbPhotoDownloader(mContext).DisplayImage(ivVideoPhoto, item.videoId, 
							VideoPhotoType.Big, UnitConversion.dip2px(mContext, 112), UnitConversion.dip2px(mContext, 112));
				}
			}
		}
	}

	/**
	 * 发送消息失败统一处理
	 */
	private OnClickListener onSendMessageError = new OnClickListener() {

		@Override
		public void onClick(View v) {
			LiveChatCallBackItem callbackItem = (LiveChatCallBackItem)v.getTag();
			MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
			String errNo = callbackItem.errNo;
			if(TextUtils.isEmpty(errNo)){
				if(callbackItem.errType >= 0){
					errNo = LiveChatErrType.values()[callbackItem.errType].name();
				}
			}
			String errMsg = StringUtil.getErrorMsg(mContext, errNo, callbackItem.errMsg);
			if(TextUtils.isEmpty(errMsg)){
				errMsg = mContext.getResources().getString(R.string.send_error_text_normal);
			}
			dialog.setMessage(errMsg);
			dialog.addButton(dialog.createButton(mContext.getString(R.string.ok), null));

			dialog.show();
		}
	};

	/**
	 * 消息发送失败，重新发送，当成一条新的消息处理（隐藏原有消息，重新再底部生成一条）
	 * 
	 * @param item
	 */
	private void resendMsgItem(LCMessageItem item) {
		if (mPositionMap.containsKey(item.msgId)) {
			int position = mPositionMap.get(item.msgId);
			View row = getContainer().getChildAt(position);
			row.setVisibility(GONE);
			/* 消息重发，删除列表中旧消息 */
			mLiveChatManager.RemoveHistoryMessage(item);
		}
		LCMessageItem newItem = null;
		switch (item.msgType) {
		case Text:
			newItem = mLiveChatManager.SendMessage(item.toId,
					item.getTextItem().message);
			break;
		case Emotion:
			newItem = mLiveChatManager.SendEmotion(item.toId,
					item.getEmotionItem().emotionId);
			break;
		case Photo:
			newItem = mLiveChatManager.SendPhoto(item.toId,
					item.getPhotoItem().srcFilePath);
			break;
		case Voice:
			newItem = mLiveChatManager.SendVoice(item.toId,
					item.getVoiceItem().filePath, ".aac",
					item.getVoiceItem().timeLength);
			break;
		default:
			break;
		}

		addRow(newItem);
		scrollToBottom(true);
	}
	
	/************************* translate relative ****************************/
	/**
	 * 用户关闭自动翻译功能，更新界面，清除翻译消息
	 */
	public void clearTranslation(){
		if(beanList != null){
			for(LCMessageItem item : beanList){
				if((item.sendType == SendType.Recv) && (item.msgType == MessageType.Text)){
					//收到的文字消息才需要翻译
					if (mPositionMap.containsKey(item.msgId)){
						int position = mPositionMap.get(item.msgId);
						/* 更新界面 */
						View row = getContainer().getChildAt(position);
						((TextView) row.findViewById(R.id.chat_message)).setText(imageGetter.getExpressMsgHTML(item.getTextItem().message));
						row.findViewById(R.id.chat_message_trans).setVisibility(View.GONE);
						row.findViewById(R.id.pbDownload).setVisibility(View.GONE);
						row.findViewById(R.id.btnError).setVisibility(View.GONE);
					}
				}
			}
		}
	}
	
	/**
	 * 翻译现有已收到的文本消息
	 * @param transLang
	 */
	public void translateAllInTextMsg(String transLang){
		if(beanList != null){
			for(LCMessageItem item : beanList){
				if((item.sendType == SendType.Recv) && (item.msgType == MessageType.Text)){
					//收到的文字消息才需要翻译
					if (mPositionMap.containsKey(item.msgId)){
						int position = mPositionMap.get(item.msgId);
						/* 更新界面 */
						View row = getContainer().getChildAt(position);
						TextView msgView = (TextView) row.findViewById(R.id.chat_message);
						TextView transTextView = (TextView) row.findViewById(R.id.chat_message_trans);
						MaterialProgressBar pbDownload = (MaterialProgressBar) row.findViewById(R.id.pbDownload);
						row.findViewById(R.id.btnError).setVisibility(View.GONE);

						String originalText = mContext.getResources().getString(R.string.livechat_original_text_message, item.getTextItem().message);
						msgView.setText(imageGetter.getExpressMsgHTML(originalText));
						String translateResult = mTranslateManager.getMsgTranslateResult(String.valueOf(item.msgId), transLang);
						if(!TextUtils.isEmpty(translateResult)){
							//已经翻译过
							transTextView.setVisibility(View.VISIBLE);
							pbDownload.setVisibility(View.GONE);
							transTextView.setText(mContext.getResources().getString(R.string.livechat_translate_text_message, translateResult));
						}else{
							//请求翻译接口
							pbDownload.setVisibility(View.VISIBLE);
							transTextView.setVisibility(View.GONE);
							mTranslateManager.translateText(String.valueOf(item.msgId), transLang, item.getTextItem().message);
						}
						
					}
				}
			}
		}
	}
	
	/**
	 * 翻译返回更新已有消息列表
	 * @param transItem
	 */
	public void updateViewByTranslateCallbak(TranslateCallbackItem transItem){
		if((transItem != null)&&(!TextUtils.isEmpty(transItem.seq))){
			//消息请求翻译回调
			int msgId = Integer.valueOf(transItem.seq);
			if(mPositionMap.containsKey(msgId)){
				int position = mPositionMap.get(msgId);
				/* 更新界面 */
				View row = getContainer().getChildAt(position);
				TextView transTextView = (TextView) row.findViewById(R.id.chat_message_trans);
				MaterialProgressBar pbDownload = (MaterialProgressBar) row.findViewById(R.id.pbDownload);
				ImageButton btnError = (ImageButton) row.findViewById(R.id.btnError);
				
				if(transItem.isSuccess){
					//翻译成功
					pbDownload.setVisibility(View.GONE);
					btnError.setVisibility(View.GONE);
					transTextView.setVisibility(View.VISIBLE);
					transTextView.setText(mContext.getResources().getString(R.string.livechat_translate_text_message, transItem.tranlatedText));
				}else{
					//翻译失败
					pbDownload.setVisibility(View.GONE);
					btnError.setVisibility(View.VISIBLE);
					transTextView.setVisibility(View.GONE);
				}
			}
		}
	}
	
	/**
	 * 文本消息翻译失败按钮点击响应
	 */
	private OnClickListener onTranslateErrorClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int postion = getPosition(v);
			if (beanList.size() > postion) {
				final LCMessageItem item = beanList.get(postion);
				MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
				dialog.setMessage(mContext.getString(R.string.livechat_translate_text_error_tips));
				dialog.addButton(dialog.createButton(
						mContext.getString(R.string.retry),
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								String defaultLang = mTranslateManager.getDefaultTranslateLang(item.getUserItem().userId);
								mTranslateManager.translateText(String.valueOf(item.msgId), defaultLang, item.getTextItem().message);
							}
						}));
				dialog.show();
			}
		}
	};

	/*
	 * chatActivity退出回收资源
	 */
	public void onDestroy() {
		/* 关闭所有正在播放的动画 */
		isDestroyed = true;
		stopPlaying();
		if (beanList != null) {
			for (LCMessageItem item : beanList) {
				if (item.msgType == MessageType.Emotion) {
					if (mPositionMap.containsKey(item.msgId)) {
						int postion = mPositionMap.get(item.msgId);
						((EmotionPlayer) getContainer().getChildAt(postion)
								.findViewById(R.id.emotionPlayer)).stop();
					}
				}
			}
			beanList.clear();
		}
		mPositionMap.clear();
	}

	/**
	 * 停止语音播放
	 */
	public void stopPlaying() {
		mVoicePlayerManager.stopPlaying();
	}

	/**
	 * 是否女士发来了消息
	 * 
	 * @return
	 */
	public boolean hasLadyInvited() {
		int index = 0;
		while (beanList.size() > index) {
			LCMessageItem bean = beanList.get(index);
			if (bean.sendType == SendType.Recv) {
				return true;
			}
			index++;
		}
		return false;
	}

	public int getPosition(View view) {
		Object value = view.getTag();
		if (value != null && value instanceof Integer) {
			return (Integer) value;
		} else {
			ViewParent parent = view.getParent();
			if (parent != null && parent instanceof View) {
				View p = (View) parent;
				return getPosition(p);
			} else {
				return -1;
			}
		}
	}

	/* 下载高级表情播放图片回调 */
	@Override
	public void onEmotionPlayImageDownloadStart(LCMessageItem item) {
		if (!isDestroyed) {
			if (item != null) {
				if (mPositionMap.containsKey(item.msgId)) {
					int position = mPositionMap.get(item.msgId);
					View row = getContainer().getChildAt(position);
					if (item.sendType == SendType.Recv) {
						/* 当接受时才处理加载及错误按钮 */
						row.findViewById(R.id.pbDownload).setVisibility(
								View.VISIBLE);
						row.findViewById(R.id.btnError)
								.setVisibility(View.GONE);
					}
					row.findViewById(R.id.emotionPlayer).setVisibility(
							View.GONE);
				}
			}
		}
	}

	@Override
	public void onEmotionPlayImageDownloadSuccess(LCMessageItem item) {
		if ((!isDestroyed)) {
			if (item != null) {
				if (mPositionMap.containsKey(item.msgId)) {
					int position = mPositionMap.get(item.msgId);
					View row = getContainer().getChildAt(position);
					if (item.sendType == SendType.Recv) {
						/* 当接受时才处理加载及错误按钮 */
						row.findViewById(R.id.pbDownload).setVisibility(
								View.GONE);
						row.findViewById(R.id.btnError)
								.setVisibility(View.GONE);
					}
					row.findViewById(R.id.ivEmotionDef)
							.setVisibility(View.GONE);
					LCEmotionItem emotionItem = mLiveChatManager
							.GetEmotionInfo(item.getEmotionItem().emotionId);
					EmotionPlayer player = (EmotionPlayer) row
							.findViewById(R.id.emotionPlayer);
					player.setVisibility(View.VISIBLE);
					player.setImageList(emotionItem.playBigImages);
					if (isScrolled) {
						if ((currVisibleList != null)
								&& currVisibleList.contains(item)) {
							/* 可见的则播放，否则不播放 */
							player.play();
						}
					} else {
						/* 未满一页的情况下直接播放 */
						player.play();
					}
				}
			}
		}
	}

	@Override
	public void onEmotionPlayImageDownloadFail(final LCMessageItem item) {
		if (!isDestroyed) {
			if (item != null) {
				if (mPositionMap.containsKey(item.msgId)) {
					int position = mPositionMap.get(item.msgId);
					View row = getContainer().getChildAt(position);
					if (item.sendType == SendType.Recv) {
						/* 当接受时才处理加载及错误按钮 */
						row.findViewById(R.id.pbDownload).setVisibility(
								View.GONE);
						ImageButton btnError = (ImageButton) row
								.findViewById(R.id.btnError);
						btnError.setVisibility(View.VISIBLE);
						btnError.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								/* 下载失败，提示及重新下载 */
								MaterialDialogAlert dialog = new MaterialDialogAlert(
										mContext);
								dialog.setMessage(mContext
										.getString(R.string.livechat_download_emotion_fail));
								dialog.addButton(dialog.createButton(mContext
										.getString(R.string.retry),
										new OnClickListener() {
											@Override
											public void onClick(View v) {
												/* 文件下载失败重新下载 */
												new EmotionPlayImageDownloader()
														.downloadEmotionPlayImage(
																item,
																MessageListView.this);
											}
										}));
								dialog.addButton(dialog.createButton(mContext
										.getString(R.string.cancel),
										null));

								dialog.show();

							}
						});
					}
					row.findViewById(R.id.emotionPlayer).setVisibility(
							View.GONE);
					row.findViewById(R.id.ivEmotionDef).setVisibility(
							View.VISIBLE);
				}
			}
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
		Rect scrollBounds = new Rect();
		getHitRect(scrollBounds);
		isScrolled = true;
		currVisibleList.clear();
		for (int i = 0; i < getContainer().getChildCount(); i++) {
			if (getContainer().getChildAt(i).getLocalVisibleRect(scrollBounds)) {
				/**
				 * 可见的播放
				 */
				if (beanList.get(i).msgType == MessageType.Emotion) {
					EmotionPlayer player = (EmotionPlayer) getContainer()
							.getChildAt(i).findViewById(R.id.emotionPlayer);
					if ((!player.isPlaying()) && (player.canPlay())) {
						player.play();
					}
					currVisibleList.add(beanList.get(i));
				}
			} else {
				/**
				 * 不可见的，如果正在播放，关闭
				 */
				if(i < beanList.size()){
					if (beanList.get(i).msgType == MessageType.Emotion) {
						EmotionPlayer player = (EmotionPlayer) getContainer()
								.getChildAt(i).findViewById(R.id.emotionPlayer);
						if (player.isPlaying()) {
							player.stop();
						}
	
					}
				}
			}
		}
	}
}
