package com.qpidnetwork.ladydating.chat.video;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.customized.view.HorizontalScrollTabbar;
import com.qpidnetwork.ladydating.customized.view.HorizontalScrollTabbar.OnHorizontalScrollTitleBarSelected;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCVideoItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback.ResultType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

public class LivechatVideoListFragment extends BaseFragment implements LiveChatManagerVideoListener{
	

	private static final int GET_VIDEOLIST_CALLBACK = 1;
	
	private LinearLayout llContent;
	private HorizontalScrollTabbar mTitlebar;
	private ViewPager mViewPager;
	private MaterialProgressBar progress;
	private ImageView imgRefresh;
	
	private LiveChatManager mLiveChatManager;
	private LivechatVideoGroupAdapter mViewPagerAdapter;
	
	private String targetId = "";
	private LCVideoListGroupItem[] mAlbumList;
	private LCVideoListVideoItem[] mAlbumVideoList;
	
	public static LivechatVideoListFragment newInstance(String targetId){
		LivechatVideoListFragment fragment = new LivechatVideoListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ChatActivity.CHAT_TARGET_ID, targetId);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_livechat_albumlist, null);
		llContent = (LinearLayout)view.findViewById(R.id.llContent);
		mTitlebar = (HorizontalScrollTabbar)view.findViewById(R.id.titleBar);
		mViewPager = (ViewPager)view.findViewById(R.id.viewPager);
		progress = (MaterialProgressBar)view.findViewById(R.id.progress);
		imgRefresh = (ImageView)view.findViewById(R.id.imgRefresh);
		imgRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mLiveChatManager != null){
					progress.setVisibility(View.VISIBLE);
					imgRefresh.setVisibility(View.GONE);
					mLiveChatManager.ClearAndGetVideoList();
				}
			}
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if((bundle!=null)&&(bundle.containsKey(ChatActivity.CHAT_TARGET_ID))){
			targetId = bundle.getString(ChatActivity.CHAT_TARGET_ID);
		}
		
		mLiveChatManager = LiveChatManager.getInstance();
		mTitlebar.setOnHorizontalScrollTitleBarSelected(new OnHorizontalScrollTitleBarSelected() {
			
			@Override
			public void onTitleBarSelected(int position) {
				mViewPager.setCurrentItem(position, true);
				resetAllOther(position);
			}
		});
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				mTitlebar.setSelected(arg0);
				resetAllOther(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/*获取图片列表*/
		llContent.setVisibility(View.GONE);
		progress.setVisibility(View.VISIBLE);
		mLiveChatManager.RegisterVideoListener(this);
		mLiveChatManager.GetVideoList();	
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		progress.setVisibility(View.GONE);
		imgRefresh.setVisibility(View.VISIBLE);
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		String errMsg = response.errmsg;
		if(getActivity() != null){
			errMsg = StringUtil.getErrorMsg(getActivity(), response.errno, response.errmsg);
		}
		switch (msg.what) {
		case GET_VIDEOLIST_CALLBACK:{
			if(response.isSuccess){
				LCVideoAblumPack albumPack = (LCVideoAblumPack)response.body;
				if(albumPack != null){
					mAlbumList = albumPack.albums;
					mAlbumVideoList = albumPack.videos;
					updateViews();
				}
			}else{
				if(getActivity() != null){
					Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
				}
			}
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 获取TagList 列表
	 * @return
	 */
	private String[] getAlbumTagList(){
		List<String> tagTitles = new ArrayList<String>();
		tagTitles.add(getResources().getString(R.string.all));
		if((mAlbumList != null) && (mAlbumList.length >0)){
			for(int i=0; i<mAlbumList.length; i++){
				tagTitles.add(mAlbumList[i].groupTitle);
			}
		}
		String[] tempArray = new String[tagTitles.size()];
		tagTitles.toArray(tempArray);
		return tempArray;
	}
	
	/**
	 * 根据tagId 过滤获取指定Tag video列表
	 * @param albumTagId
	 * @return
	 */
	private LCVideoListVideoItem[] getVideoListByTagId(String albumTagId){
		List<LCVideoListVideoItem> videoList = new ArrayList<LCVideoListVideoItem>();
		if( mAlbumVideoList!= null){
			for(LCVideoListVideoItem item : mAlbumVideoList){
				if(item.groupId.equals(albumTagId)){
					videoList.add(item);
				}
			}
		}
		LCVideoListVideoItem[] tempArray = new LCVideoListVideoItem[videoList.size()];
		videoList.toArray(tempArray);
		return tempArray;
	}
	
	/**
	 * 刷新界面
	 */
	private void updateViews(){
		if((mAlbumList != null)&&(mAlbumList.length > 0)){
			llContent.setVisibility(View.VISIBLE);
			int titleItemWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels*2/9;
			mTitlebar.setParams(getAlbumTagList(), 0, titleItemWidth);
			
			mViewPagerAdapter = new LivechatVideoGroupAdapter(getChildFragmentManager(), mAlbumList);
			mViewPager.setAdapter(mViewPagerAdapter);
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLiveChatManager.UnregisterVideoListener(this);
	}
	
	/**
	 * 滚动后刷新其他页面显示及状态
	 * @param position
	 */
	private void resetAllOther(int position){
		if(mAlbumList != null){
			for(int i=0; i<mAlbumList.length; i++){
				if(i != position){
					if((mViewPagerAdapter != null) && (mViewPagerAdapter.getFragment(i)!=null)){
						((VideoThumbPhotoFragment)mViewPagerAdapter.getFragment(i)).resetStatusAndData();
					}
				}
			}
		}
	}
	
	private class LivechatVideoGroupAdapter extends FragmentPagerAdapter{
		
		private LCVideoListGroupItem[] albumTagList;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;
		
		public LivechatVideoGroupAdapter(FragmentManager fg, LCVideoListGroupItem[] tagList) {
			super(fg);
			this.albumTagList = tagList;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}
		
		public Fragment getFragment(int position){
			Fragment fragment = null;
			if(mPageReference.containsKey(position)){
				fragment = mPageReference.get(position).get();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			int count = 1;
			if(albumTagList != null){
				count += albumTagList.length;
			}
			return count;
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment fragment= null;
			if(mPageReference.containsKey(position)){
				fragment = mPageReference.get(position).get();
			}
			if(fragment == null){
				if(position == 0){
					//All
					fragment = VideoThumbPhotoFragment.newInstance(mAlbumVideoList, targetId);
				}else{
					fragment = VideoThumbPhotoFragment.newInstance(getVideoListByTagId(albumTagList[position - 1].groupId), targetId);
				}
				mPageReference.put(position, new WeakReference<Fragment>(fragment));
			}
			return fragment;
		}
	}
	
	/********************* Livechat Video 相关回掉 *********************************/
	@Override
	public void OnCheckSendVideo(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCVideoItem videoItem) {
		// TODO Auto-generated method stub
		
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
	public void OnRecvShowPhoto(LCUserItem userItem, String videoId,
			String videoDesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) {
		Message msg = Message.obtain();
		msg.what = GET_VIDEOLIST_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, new LCVideoAblumPack(groups, videos));
		msg.obj = response;
		sendUiMessage(msg);		
	}

	@Override
	public void OnRecvShowVideo(LCUserItem userItem, String videoId,
			String videoDesc) {
		// TODO Auto-generated method stub
		
	}
	
	private class LCVideoAblumPack{
		
		LCVideoListGroupItem[] albums;
		LCVideoListVideoItem[] videos;
		
		public LCVideoAblumPack(LCVideoListGroupItem[] albums, LCVideoListVideoItem[] videos){
			this.albums = albums;
			this.videos = videos;
		}
	}
	
}
