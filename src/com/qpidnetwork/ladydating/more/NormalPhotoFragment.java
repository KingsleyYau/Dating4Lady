package com.qpidnetwork.ladydating.more;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.ladydating.customized.view.TouchImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;

public class NormalPhotoFragment extends BaseFragment implements ImageViewLoaderCallback{
	
	private static final String PHOTO_URL = "photoUrl";
	
	private static final int GET_PHOTO_SUCCESS = 1;
	private static final int GET_PHOTO_FAILED = 2;
	
	private MaterialProgressBar progress;
	private TouchImageView imageView;
	private String photoUrl = "";
	private ImageViewLoader mDownloader;
	
	
	public static NormalPhotoFragment getInstance(String photoUrl){
		NormalPhotoFragment fragment = new NormalPhotoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(PHOTO_URL, photoUrl);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_normal_photo_preview, null);
		imageView = (TouchImageView)view.findViewById(R.id.imageView);
		progress = (MaterialProgressBar)view.findViewById(R.id.progress);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if((bundle != null)&&(bundle.containsKey(PHOTO_URL))){
			photoUrl = bundle.getString(PHOTO_URL);
		}
		imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
		if(!TextUtils.isEmpty(photoUrl)){
			mDownloader= new ImageViewLoader(getActivity());
			progress.setVisibility(View.VISIBLE);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(photoUrl);
			mDownloader.DisplayImage(imageView, photoUrl, localPath, this);
		}else{
			progress.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 图片放大查看，切换到下一张时需还原到为放大状态
	 */
	public void reset(){
		if(imageView != null){
			imageView.Reset();
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		progress.setVisibility(View.GONE);
		switch (msg.what) {
		case GET_PHOTO_SUCCESS:
			
			break;
		case GET_PHOTO_FAILED:
			
			break;
		default:
			break;
		}
	}

	@Override
	public void OnDisplayNewImageFinish() {
		sendEmptyUiMessage(GET_PHOTO_SUCCESS);
	}

	@Override
	public void OnLoadPhotoFailed() {
		sendEmptyUiMessage(GET_PHOTO_FAILED);
	}
}
