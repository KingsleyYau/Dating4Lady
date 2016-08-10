package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.framework.stickygridheaders.StickyGridHeadersGridView;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.AlbumDetailActivity.Category;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MultiSwipeRefreshLayout;
import com.qpidnetwork.request.OnQueryAlbumVideoListCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.item.AlbumVideoItem;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-22
 */
public class AlbumVideoListFragment extends BaseFragment implements OnClickListener, OnRefreshListener, OnQueryAlbumVideoListCallback{

	
	public static final int GET_ALBUM_ITEM_LIST = 1;
	public static final int UPDATE_ALBUM_ITEM = 2;
	
	private MultiSwipeRefreshLayout swipeRefresh;// 下拉刷新
	private StickyGridHeadersGridView sgvTheme;// 带标题排列的gridview
	private TextView tvEmpty;//数据为空显示
	private AlbumVideoAdapter mAdapter;
	private ArrayList<AlbumVideo> mAlbumItemList;
	private ArrayList<AlbumVideoItem> mAlbumVideoList;//排序的
	private ArrayList<AlbumVideoItem> unReviewItems;//待审核
	private ArrayList<AlbumVideoItem> pastItems;//审核通过
	private ArrayList<AlbumVideoItem> editItems;//打回修改
	private ArrayList<AlbumVideoItem> rejectItems;//不通过
	
	private String albumId;//相册ID 
	private String albumName;//相册名称 用于添加修改Video显示
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_album_item, null);
		sgvTheme = (StickyGridHeadersGridView) view.findViewById(R.id.sgvTheme);
		sgvTheme.setAreHeadersSticky(true);
		tvEmpty = (TextView) view.findViewById(R.id.emptyView);
		tvEmpty.setText(getResources().getString(R.string.album_no_video));
		sgvTheme.setEmptyView(tvEmpty);
		
		swipeRefresh = (MultiSwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
		swipeRefresh.setSwipeableChildren(R.id.sgvTheme, R.id.emptyView);
		swipeRefresh.setOnRefreshListener(this);
		swipeRefresh.setProgressViewOffset(false, 0, UnitConversion.dip2px(mContext, 24));
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		albumId = getActivity().getIntent().getExtras().getString(AlbumDetailActivity.INPUT_ALBUM_ID);
		albumName = getActivity().getIntent().getExtras().getString(AlbumDetailActivity.INPUT_ALBUM_NAME);
		
		mAlbumItemList = new ArrayList<AlbumVideo>();
		mAlbumVideoList = new ArrayList<AlbumVideoItem>();
		unReviewItems = new ArrayList<AlbumVideoItem>();
		pastItems = new ArrayList<AlbumVideoItem>();
		editItems = new ArrayList<AlbumVideoItem>();
		rejectItems= new ArrayList<AlbumVideoItem>();
		
		mAdapter = new AlbumVideoAdapter(mContext,albumId,albumName,mAlbumItemList,mAlbumVideoList);
		mAdapter.setIsIniting(true);
		sgvTheme.setAdapter(mAdapter);
		
		QueryAlbumItem();
	}
	
	/**
	 * 获取VideoList数据
	 */
	public void QueryAlbumItem() {
		// TODO Auto-generated method stub
		swipeRefresh.setRefreshing(true);
		if (albumId != null) {
			RequestJniAlbum.QueryAlbumVideoList(albumId, this);// 查询album列表
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		swipeRefresh.setRefreshing(false);
		mAdapter.setIsIniting(false);
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		switch (msg.what) {
		case GET_ALBUM_ITEM_LIST://获取视频相册item列表
			if (response.isSuccess) {// 获取成功
				AlbumVideoItem[] itemList = (AlbumVideoItem[]) response.body;
				notifyData(itemList);
			} else {// 请求失败
				if(getActivity() != null){
					Toast.makeText(getActivity(), getResources().getString(R.string.album_videolist_update_error), Toast.LENGTH_LONG).show();
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * @param itemList
	 * 视频列表分类
	 */
	private void notifyData(AlbumVideoItem[] itemList) {
		// TODO Auto-generated method stub
		clearData();//先清空数据
		
		//itemList分类型
		if(itemList != null){
			for (int i = 0; i < itemList.length; i++) {
				switch (itemList[i].reviewStatus) {
				case ReviewY:
					pastItems.add(itemList[i]);
					break;
				case ReviewP:
				case ReviewE:
					unReviewItems.add(itemList[i]);
					break;
				case ReviewD:
					editItems.add(itemList[i]);
					break;
				case ReviewN:
					rejectItems.add(itemList[i]);
					break;
				default:
					break;
				}
			}
		}
		
		//依次Add
		if(unReviewItems.size()>0){
			mAlbumItemList.add(new AlbumVideo(Category.UNDER_REVIEW, unReviewItems));
			mAlbumVideoList.addAll(unReviewItems);
		}
		if(pastItems.size()>0){
			mAlbumItemList.add(new AlbumVideo(Category.PAST, pastItems));
			mAlbumVideoList.addAll(pastItems);
		}
		if(editItems.size()>0){
			mAlbumItemList.add(new AlbumVideo(Category.REQUIRED_EDIT, editItems));
			mAlbumVideoList.addAll(editItems);
		}
		if(rejectItems.size()>0){
			mAlbumItemList.add(new AlbumVideo(Category.REJECTED, rejectItems));
			mAlbumVideoList.addAll(rejectItems);
		}
		
		//刷新界面
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 数据情空
	 */
	private void clearData() {
		// TODO Auto-generated method stub
		mAlbumItemList.clear();
		mAlbumVideoList.clear();
		pastItems.clear();
		unReviewItems.clear();
		editItems.clear();
		rejectItems.clear();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		QueryAlbumItem();
	}

	@Override
	public void OnQueryAlbumVideoList(boolean isSuccess, String errno,
			String errmsg, AlbumVideoItem[] itemList) {
		Message msg = Message.obtain();
		msg.what = GET_ALBUM_ITEM_LIST;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, itemList);
		msg.obj = response;
		sendUiMessage(msg);
	}
	
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return mAlbumVideoList.size() == 0 ? true : false;
	}
	
	public static class AlbumVideo{
		public Category category;
		public ArrayList<AlbumVideoItem> videoItems;
		
		public AlbumVideo(Category category, ArrayList<AlbumVideoItem> videoItems){
			this.category = category;
			this.videoItems = videoItems;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
