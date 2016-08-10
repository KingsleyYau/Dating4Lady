package com.qpidnetwork.ladydating.chat.picture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.chat.downloader.LivechatPrivatePhotoDownloader;
import com.qpidnetwork.ladydating.chat.downloader.LivechatPrivatePhotoDownloader.OnDownloadCallback;
import com.qpidnetwork.ladydating.customized.view.FlatToast;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.ladydating.customized.view.MaterialRaisedButton;
import com.qpidnetwork.ladydating.customized.view.TouchImageView;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;

@SuppressLint("InflateParams")
public class PrivatePhotoPreviewFragment extends BaseFragment implements OnClickListener{

	private static final String LIVE_CHAT_MESSAGE_ITEM = "msgitem";
	
	private FlatToast flatToast;
	/**
	 * 已经购买
	 */
	private RelativeLayout rlChargedBody;
	private TouchImageView ivCharge;
	private MaterialProgressBar progressBar;

	/**
	 * 下载失败
	 */
	private LinearLayout llErrorPage;
	private MaterialRaisedButton tvRetry;

	/**
	 * 底部描述及下载原图
	 */
	private TextView textViewDescription;
	private ImageButton downloadButton;

	/* data */
	private LCMessageItem mMsgItem;

	private LiveChatManager mLiveChatManager;
	
	private List<LivechatPrivatePhotoDownloader> mDownLoaderList;//存储下载器列表，退出界面如果还未下载完成，清除回调，防止异步回调调用界面元素导致异常死机

	public static PrivatePhotoPreviewFragment getFragment(LCMessageItem item) {
		PrivatePhotoPreviewFragment fragment = new PrivatePhotoPreviewFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(LIVE_CHAT_MESSAGE_ITEM, item);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_livechat_private_photo,
				null);
		initViews(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mLiveChatManager = LiveChatManager.newInstance(getActivity());
		Bundle bundle = getArguments();
		if ((bundle != null) && (bundle.containsKey(LIVE_CHAT_MESSAGE_ITEM))) {
			LCMessageItem tempItem = (LCMessageItem) bundle
					.getSerializable(LIVE_CHAT_MESSAGE_ITEM);
			if(tempItem != null && tempItem.getUserItem() != null){
				mMsgItem = mLiveChatManager.GetMessageWithMsgId(tempItem.getUserItem().userId, tempItem.msgId);
			}
		}
		if(mMsgItem != null){
			mDownLoaderList = new ArrayList<LivechatPrivatePhotoDownloader>();
			
			UpdateView();
		}
	}

	private void initViews(View view) {
		
		/**
		 * A view contain a clean image which size is 370 *370
		 */
		rlChargedBody = (RelativeLayout) view.findViewById(R.id.rlChargedBody);
		ivCharge = (TouchImageView) view.findViewById(R.id.ivCharge);
		progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);

		
		/**
		 * A view contains a broken image with a retry button
		 */
		llErrorPage = (LinearLayout) view.findViewById(R.id.llErrorPage);
		tvRetry = (MaterialRaisedButton) view.findViewById(R.id.tvRetry);

		
		/**
		 * photo description if there is.
		 */
		textViewDescription = (TextView) view
				.findViewById(R.id.textViewDescription);
		downloadButton = (ImageButton) view
				.findViewById(R.id.imageViewDownload);

		tvRetry.setOnClickListener(this);
		downloadButton.setOnClickListener(this);
		
	}

	/**
	 * 刷新界面
	 */
	private void UpdateView() {
		if( ivCharge != null ) {
			ivCharge.SetCanScale(false);
		}
		
		/** Set photo description **/
		textViewDescription.setText(mMsgItem.getPhotoItem().photoDesc);
			
		/*解决发送图片查看时，本地已有，但是还是显示现在失败等异常*/
		if(mMsgItem.sendType == SendType.Send){
			String filePath = mMsgItem.getPhotoItem().srcFilePath; 
			if(!StringUtil.isEmpty(filePath) && new File(filePath).exists()){
				setDownloadClearImageViewOnSuccessful(filePath);
				return;
			}
		}

		/**
		 * Download clear image if the photo is paid.
		 */
		
		setDownloadClearImageView();
		LivechatPrivatePhotoDownloader downloader = new LivechatPrivatePhotoDownloader(getActivity());
		downloader.startDownload(mMsgItem,
				PhotoSizeType.Large, new OnDownloadCallback() {

					@Override
					public void onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader downloader, String filePath) {
						mDownLoaderList.remove(downloader);
						setDownloadClearImageViewOnSuccessful(filePath);
					}

					@Override
					public void onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader downloader) {
						mDownLoaderList.remove(downloader);
						setDownloadClearImageViewOnError();
					}
		});
		mDownLoaderList.add(downloader);
	}
	
	
	private void setDownloadClearImageView(){
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		rlChargedBody.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		ivCharge.SetCanScale(false);
	}
	
	private void setDownloadClearImageViewOnSuccessful(String filePath){
		llErrorPage.setVisibility(View.GONE);
		downloadButton.setVisibility(View.VISIBLE);
		rlChargedBody.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		int screenWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels;
		int scrrenHeight = SystemUtil.getDisplayMetrics(getActivity()).heightPixels;
		Bitmap bitmap = ImageUtil.decodeAndScaleBitmapFromFile(filePath, screenWidth, scrrenHeight);
		if (bitmap != null) {
			ivCharge.setImageBitmap(bitmap);
			ivCharge.SetCanScale(true);
		}else{
			setDownloadClearImageViewOnError();
		}
		
	}
	
	private void setDownloadClearImageViewOnError(){
		progressBar.setVisibility(View.GONE);
		downloadButton.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.VISIBLE);
		rlChargedBody.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageViewDownload:
			/* 下载原图按钮 */
			downloadSourcePicture();
			break;
		case R.id.tvRetry:
			/* 下载失败，重新下图 */
			UpdateView();
			break;
		case R.id.buttonCancel:
			getActivity().finish();
			break;
		default:
			break;
		}
	}
	
	private void downloadSourcePicture(){
		
		if (flatToast == null) 
			flatToast = new FlatToast(getActivity());
		flatToast.setProgressing(getResources().getString(R.string.common_downloading));
		flatToast.show();
		
		/*开始下载，disable下载按钮*/
		downloadButton.setClickable(false);
		
		LivechatPrivatePhotoDownloader downloader = new LivechatPrivatePhotoDownloader(getActivity());
		downloader.startDownload(mMsgItem, PhotoSizeType.Original, new OnDownloadCallback() {
			
			@Override
			public void onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader downloader , String filePath) {
				mDownLoaderList.remove(downloader);
				flatToast.setDone(getResources().getString(R.string.common_done));
				/*下载成功，enable下载按钮*/
				downloadButton.setClickable(true);
				try {
					// 直接保存到相册
					String fileName = mMsgItem.getPhotoItem().photoId + "-" + System.currentTimeMillis() + ".jpg";
					ImageUtil.SaveImageToGallery(getActivity(), mMsgItem.getPhotoItem().showSrcFilePath, filePath, fileName, null);
					
//					MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), filePath, mMsgItem.getPhotoItem().photoDesc , getActivity().getResources().getString(R.string.app_name));
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					flatToast.setFailed(getResources().getString(R.string.common_failed));
				}
				Toast.makeText(getActivity(), getString(R.string.livechat_saved_origional_image), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader downloader) {
				mDownLoaderList.remove(downloader);
				flatToast.setFailed(getResources().getString(R.string.common_failed));
				/*下载成功，enable下载按钮*/
				downloadButton.setClickable(true);
			}
		});
		mDownLoaderList.add(downloader);
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
	}
	
	/**
	 * 图片放大查看，切换到下一张时需还原到为放大状态
	 */
	public void reset(){
		if(ivCharge != null){
			ivCharge.Reset();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(flatToast != null){
			flatToast.cancelImmediately();
		}
		if(mDownLoaderList != null){
			/*清除下载器回调，防止界面结束仍回调导致界面异常*/
			for(LivechatPrivatePhotoDownloader downloader : mDownLoaderList){
				downloader.unregisterDownloaderCallback();
			}
			mDownLoaderList.clear();
		}
	}
}
