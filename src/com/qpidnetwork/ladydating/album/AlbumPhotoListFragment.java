package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
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
import com.qpidnetwork.request.OnQueryAlbumPhotoListCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.item.AlbumPhotoItem;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-21
 */
public class AlbumPhotoListFragment extends BaseFragment implements OnRefreshListener,OnQueryAlbumPhotoListCallback{
	
	public static final int GET_ALBUM_ITEM_LIST = 1;
	
	private MultiSwipeRefreshLayout swipeRefresh;// 下拉刷新
	private StickyGridHeadersGridView sgvTheme;// 带标题排列的gridview
	private TextView tvEmpty;//数据为空显示
	private AlbumPhotoAdapter mAdapter;
	private ArrayList<AlbumPhoto> mAlbumItemList;
	private ArrayList<AlbumPhotoItem> mAlbumPhotoList;//排序的
	private ArrayList<AlbumPhotoItem> unReviewItems;//待审核
	private ArrayList<AlbumPhotoItem> pastItems;//审核通过
	private ArrayList<AlbumPhotoItem> editItems;//打回修改
	private ArrayList<AlbumPhotoItem> rejectItems;//不通过
	
	private String albumId;//相册ID 
	private String albumName;//相册名称 用于添加修改photo显示
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_album_item, null);
		sgvTheme = (StickyGridHeadersGridView) view.findViewById(R.id.sgvTheme);
		sgvTheme.setAreHeadersSticky(true);
		tvEmpty = (TextView) view.findViewById(R.id.emptyView);
		tvEmpty.setText(getResources().getString(R.string.album_no_photo));
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
		
		mAlbumItemList = new ArrayList<AlbumPhoto>();
		mAlbumPhotoList = new ArrayList<AlbumPhotoItem>();
		unReviewItems = new ArrayList<AlbumPhotoItem>();
		pastItems = new ArrayList<AlbumPhotoItem>();
		editItems = new ArrayList<AlbumPhotoItem>();
		rejectItems= new ArrayList<AlbumPhotoItem>();
		
		mAdapter = new AlbumPhotoAdapter(mContext,albumId,albumName,mAlbumItemList,mAlbumPhotoList);
		mAdapter.setIsIniting(true);
		sgvTheme.setAdapter(mAdapter);
		
		QueryAlbumItem();//查询相册item
	}
	
	public void QueryAlbumItem() {
		// TODO Auto-generated method stub
		swipeRefresh.setRefreshing(true);
		if (albumId != null) {
			RequestJniAlbum.QueryAlbumPhotoList(albumId, this);// 查询album列表
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
		case GET_ALBUM_ITEM_LIST:
			if (response.isSuccess) {// 获取成功
				AlbumPhotoItem[] itemList = (AlbumPhotoItem[]) response.body;
				notifyData(itemList);
			} else {// 请求失败
				if(getActivity() != null){
					Toast.makeText(getActivity(), getResources().getString(R.string.album_photolist_update_error), Toast.LENGTH_LONG).show();
				}
			}
			break;

		default:
			break;
		}
	}

	
	private void notifyData(AlbumPhotoItem[] itemList) {
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
			mAlbumItemList.add(new AlbumPhoto(Category.UNDER_REVIEW, unReviewItems));
			mAlbumPhotoList.addAll(unReviewItems);
		}
		if(pastItems.size()>0){
			mAlbumItemList.add(new AlbumPhoto(Category.PAST, pastItems));
			mAlbumPhotoList.addAll(pastItems);
		}
		if(editItems.size()>0){
			mAlbumItemList.add(new AlbumPhoto(Category.REQUIRED_EDIT, editItems));
			mAlbumPhotoList.addAll(editItems);
		}
		if(rejectItems.size()>0){
			mAlbumItemList.add(new AlbumPhoto(Category.REJECTED, rejectItems));
			mAlbumPhotoList.addAll(rejectItems);
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
		mAlbumPhotoList.clear();
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
	public void OnQueryAlbumPhotoList(boolean isSuccess, String errno,
			String errmsg, AlbumPhotoItem[] itemList) {
		Message msg = Message.obtain();
		msg.what = GET_ALBUM_ITEM_LIST;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, itemList);
		msg.obj = response;
		sendUiMessage(msg);
		
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return mAlbumPhotoList.size() == 0 ? true : false;
	}
	
	public static class AlbumPhoto{
		public Category category;
		public ArrayList<AlbumPhotoItem> photoItems;
		
		public AlbumPhoto(Category category, ArrayList<AlbumPhotoItem> photoItems){
			this.category = category;
			this.photoItems = photoItems;
		}
	}

}
