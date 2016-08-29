package com.qpidnetwork.ladydating.album;

import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.AlbumPreviewActivity.PreviewType;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.ladydating.customized.view.TouchImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.AlbumPhotoItem;
import com.qpidnetwork.request.item.AlbumVideoItem;
import com.qpidnetwork.tool.ImageViewLoader;
import com.qpidnetwork.tool.ImageViewLoader.ImageViewLoaderCallback;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-23
 */
public class AlbumPreviewFragment extends BaseFragment implements ImageViewLoaderCallback,OnClickListener{
	
	private static final String ALBUM_PHOTO = "album_photo";
	private static final String ALBUM_VIDEO = "album_video";
	public static final String PREVIEW_TYPE = "preview_type";
	
	private static final int GET_PHOTO_SUCCESS = 1;
	private static final int GET_PHOTO_FAILED = 2;
	private static final int CHECK_VIDEO_CALLBACK = 3; 
	
	private AlbumPhotoItem albumPhotoItem;
	private AlbumVideoItem albumVideoItem;
	private PreviewType previewType;
	
	private MaterialProgressBar progress;
	private TouchImageView imageView;
	private TextView tvDesc;
	private ImageButton videoButton;
	private TextView tvErrorTips;
	private ImageViewLoader mDownloader;
	
	public static AlbumPreviewFragment getInstance(AlbumPhotoItem photoItem){
		AlbumPreviewFragment fragment = new AlbumPreviewFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(ALBUM_PHOTO, photoItem);
		bundle.putInt(PREVIEW_TYPE, PreviewType.PhotoPreview.ordinal());
		fragment.setArguments(bundle);
		return fragment;
	}
	public static AlbumPreviewFragment getInstance(AlbumVideoItem videoItem){
		AlbumPreviewFragment fragment = new AlbumPreviewFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(ALBUM_VIDEO, videoItem);
		bundle.putInt(PREVIEW_TYPE, PreviewType.VideoPreview.ordinal());
		fragment.setArguments(bundle);
		return fragment;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_album_photo_preview, null);
		imageView = (TouchImageView)view.findViewById(R.id.imageView);
		tvDesc = (TextView) view.findViewById(R.id.tvDesc);
		progress = (MaterialProgressBar)view.findViewById(R.id.progress);
		videoButton = (ImageButton) view.findViewById(R.id.videoButton);
		tvErrorTips = (TextView) view.findViewById(R.id.tvErrorTips);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey(PREVIEW_TYPE)){
				previewType = PreviewType.values()[bundle.getInt(PREVIEW_TYPE)];
			}
			if (bundle.containsKey(ALBUM_PHOTO)) {
				albumPhotoItem = (AlbumPhotoItem) bundle.getSerializable(ALBUM_PHOTO);
			}
			if (bundle.containsKey(ALBUM_VIDEO)) {
				albumVideoItem = (AlbumVideoItem) bundle.getSerializable(ALBUM_VIDEO);
			}
		}
		
		if(previewType==null){
			return;
		}
		
		switch (previewType) {
		case PhotoPreview:
			if(albumPhotoItem!=null){
				showImageAndDesc(albumPhotoItem.url,albumPhotoItem.title);
			}
			break;
		case VideoPreview:
			if(albumVideoItem!=null){
//				showImageAndDesc(albumVideoItem.thumbUrl,albumVideoItem.title);
//				videoButton.setVisibility(View.VISIBLE);
//				videoButton.setOnClickListener(this);
				imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
				if(!TextUtils.isEmpty(albumVideoItem.previewUrl)){
					mDownloader= new ImageViewLoader(getActivity());
					String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(albumVideoItem.previewUrl);
					mDownloader.DisplayImage(imageView, albumVideoItem.previewUrl, localPath, null);
				}
				tvDesc.setText(albumVideoItem.title);
				progress.setVisibility(View.VISIBLE);
				checkVideoExist(albumVideoItem.url);
			}
			break;
		}
	}
	private void showImageAndDesc(String url, String title) {
		// TODO Auto-generated method stub
		imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
		if(!TextUtils.isEmpty(url)){
			mDownloader= new ImageViewLoader(getActivity());
			progress.setVisibility(View.VISIBLE);
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(url);
			mDownloader.DisplayImage(imageView, url, localPath, this);
		}else{
			progress.setVisibility(View.GONE);
		}
		tvDesc.setText(title);
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
		case CHECK_VIDEO_CALLBACK:{
			if(msg.arg1 == 1){
				//视频存在
				videoButton.setVisibility(View.VISIBLE);
				videoButton.setOnClickListener(this);
				tvErrorTips.setVisibility(View.GONE);
			}else{
				videoButton.setVisibility(View.GONE);
				tvErrorTips.setVisibility(View.VISIBLE);
			}
		}break;
		default:
			break;
		}
	}
	
	@Override
	public void OnDisplayNewImageFinish() {
		// TODO Auto-generated method stub
		sendEmptyUiMessage(GET_PHOTO_SUCCESS);
	}
	@Override
	public void OnLoadPhotoFailed() {
		// TODO Auto-generated method stub
		sendEmptyUiMessage(GET_PHOTO_FAILED);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.videoButton){//浏览器查看
			Uri uri = Uri.parse(albumVideoItem.url);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "video/mpeg4");
			if(getActivity() != null
					&&intent.resolveActivity(getActivity().getPackageManager()) != null){
				startActivity(intent);
			}else{
				Intent browerIntent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(browerIntent);
			}
		}
	}
	
	/**
	 * 通过仅获取头部检测文件是否存在
	 * @param url
	 */
	private void checkVideoExist(final String url){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isContain = false;
		        try {
		        	 
	                URL obj = new URL(url);
	                HttpURLConnection conn = (HttpURLConnection)obj.openConnection();
	                conn.setRequestProperty("Authorization", "Basic " + new String(Base64.encode("test:5179".getBytes(), Base64.DEFAULT)));
	                conn.connect();
	                if(conn.getResponseCode() == 200){
	                	isContain = true;
	                }
		        } catch (Exception e) {
	                e.printStackTrace();
		        }finally{
		        	Message msg = Message.obtain();
		        	msg.what = CHECK_VIDEO_CALLBACK;
		        	msg.arg1 = isContain?1:0;
		        	sendUiMessage(msg);
		        }
			}
		}).start();
	}

}
