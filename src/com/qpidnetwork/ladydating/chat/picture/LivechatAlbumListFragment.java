package com.qpidnetwork.ladydating.chat.picture;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.qpidnetwork.livechat.LCPhotoItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback.ResultType;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public class LivechatAlbumListFragment extends BaseFragment implements LiveChatManagerPhotoListener{
	
	private static final int GET_PHOTOLIST_CALLBACK = 1;
	
	private LinearLayout llContent;
	private HorizontalScrollTabbar mTitlebar;
	private ViewPager mViewPager;
	private MaterialProgressBar progress;
	private ImageView imgRefresh;
	
	private LivechatAlbumAdapter mViewPagerAdapter;
	private LiveChatManager mLiveChatManager;
	private LCPhotoListAlbumItem[] mAlbumList;
	private LCPhotoListPhotoItem[] mAlbumPhotoList;
	
	private String targetId = "";
	
	
	public static LivechatAlbumListFragment newInstance(String targetId){
		LivechatAlbumListFragment fragment = new LivechatAlbumListFragment();
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
					mLiveChatManager.ClearAndGetPhotoList();
				}
			}
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		mLiveChatManager.RegisterPhotoListener(this);
		mLiveChatManager.GetPhotoList();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLiveChatManager.UnregisterPhotoListener(this);
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
						((AlbumPhotoFragment)mViewPagerAdapter.getFragment(i)).resetStatusAndData();
					}
				}
			}
		}
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
		case GET_PHOTOLIST_CALLBACK:{
			if(response.isSuccess){
				LCPhotoAblumPack albumPack = (LCPhotoAblumPack)response.body;
				if(albumPack != null){
					mAlbumList = albumPack.albums;
					mAlbumPhotoList = albumPack.photos;
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
				tagTitles.add(mAlbumList[i].title);
			}
		}
		String[] tempArray = new String[tagTitles.size()];
		tagTitles.toArray(tempArray);
		return tempArray;
	}
	
	/**
	 * 根据tagId 过滤获取指定Tag photo列表
	 * @param albumTagId
	 * @return
	 */
	private LCPhotoListPhotoItem[] getPhotoListByTagId(String albumTagId){
		List<LCPhotoListPhotoItem> photoList = new ArrayList<LCPhotoListPhotoItem>();
		if( mAlbumPhotoList!= null){
			for(LCPhotoListPhotoItem item : mAlbumPhotoList){
				if(item.albumId.equals(albumTagId)){
					photoList.add(item);
				}
			}
		}
		LCPhotoListPhotoItem[] tempArray = new LCPhotoListPhotoItem[photoList.size()];
		photoList.toArray(tempArray);
		return tempArray;
	}
	
	private void updateViews(){
		if((mAlbumList != null)&&(mAlbumList.length > 0)){
			llContent.setVisibility(View.VISIBLE);
			int titleItemWidth = SystemUtil.getDisplayMetrics(getActivity()).widthPixels*2/9;
			mTitlebar.setParams(getAlbumTagList(), 0, titleItemWidth);
			
			mViewPagerAdapter = new LivechatAlbumAdapter(getChildFragmentManager(), mAlbumList);
			mViewPager.setAdapter(mViewPagerAdapter);
		}
	}
	
	private class LivechatAlbumAdapter extends FragmentPagerAdapter{
		
		private LCPhotoListAlbumItem[] albumTagList;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;
		
		public LivechatAlbumAdapter(FragmentManager fg, LCPhotoListAlbumItem[] tagList) {
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
					fragment = AlbumPhotoFragment.newInstance(mAlbumPhotoList, targetId);
				}else{
					fragment = AlbumPhotoFragment.newInstance(getPhotoListByTagId(albumTagList[position - 1].albumId), targetId);
				}
				mPageReference.put(position, new WeakReference<Fragment>(fragment));
			}
			return fragment;
		}
	}

	
	/****************************** Livechat 图片相关  *******************************/
	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
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
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg,
			LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos) {
		Message msg = Message.obtain();
		msg.what = GET_PHOTOLIST_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, new LCPhotoAblumPack(albums, photos));
		msg.obj = response;
		sendUiMessage(msg);
	}
	
	private class LCPhotoAblumPack{
		LCPhotoListAlbumItem[] albums;
		LCPhotoListPhotoItem[] photos;
		
		public LCPhotoAblumPack(LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos){
			this.albums = albums;
			this.photos = photos;
		}
	}
	
}
