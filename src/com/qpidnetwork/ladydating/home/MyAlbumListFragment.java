package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.AlbumDetailActivity;
import com.qpidnetwork.ladydating.album.AlbumEditActivity;
import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.request.OnQueryAlbumListCallback;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.request.RequestJniAlbum.AlbumType;
import com.qpidnetwork.request.item.AlbumListItem;

public class MyAlbumListFragment extends BaseGridViewFragment implements
		OnQueryAlbumListCallback, GridView.OnItemClickListener {

	public static final int GET_ALBUM_LIST = 1;
	public MyAlbumListAdapter mAdapter;
	public List<AlbumListItem> mAlbumList;//相册列表数据
	private HomeActivity homeActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		homeActivity = (HomeActivity) getActivity();
		getProgressBar().setVisibility(View.GONE);
		getRefreshLayout().setRefreshing(true);

	}

	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		mAlbumList = new ArrayList<AlbumListItem>();
		mAdapter = new MyAlbumListAdapter(getActivity(), mAlbumList);

		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(this);
		getRefreshLayout().setSwipeableChildren(R.id.gridView, R.id.emptyView);
		
		QueryAlbumList();//获取相册列表
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (position == 0) {//创建相册
			AlbumEditActivity.launch(homeActivity);
			return;
		}
		AlbumListItem item = mAlbumList.get(position-1);
		
		if(item == null){
			return ;
		}
		
		switch (item.type) {
		case Photo:
		case Video:
			AlbumDetailActivity.launch(homeActivity, item);
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		QueryAlbumList();
	}
	
	/**
	 * 相册列表查询
	 */
	public void QueryAlbumList(){
		RequestJniAlbum.QueryAlbumList(this);
	}

	@Override
	public void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		getRefreshLayout().setRefreshing(false);
		getProgressBar().setVisibility(View.GONE);
		switch (msg.what) {
		case GET_ALBUM_LIST:{
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			if (response.isSuccess) {//请求成功
				AlbumListItem[] itemList = (AlbumListItem[]) response.body;
				if(itemList != null){
					notifyData(itemList);//更新数据
				}
			}else{//请求失败
				if(getActivity() != null){
					Toast.makeText(getActivity(), getResources().getString(R.string.album_list_error), Toast.LENGTH_LONG).show();
				}
			}
		}break;
		
		}

	}

	/**
	 * @param itemList
	 * 更新数据(显示图片和视频列表)
	 */
	private void notifyData(AlbumListItem[] itemList) {
		// TODO Auto-generated method stub
		mAlbumList.clear();
		for (int i = 0; i < itemList.length; i++) {
			if(itemList[i].type==AlbumType.Photo||itemList[i].type==AlbumType.Video){
				mAlbumList.add(itemList[i]);
			}
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.other_list, menu);
		MenuItem item = menu.findItem(R.id.contacts);
		// MenuItemCompat.setActionView(item,
		// R.layout.badge_view_circle_solid_red_strok_white);
		if (homeActivity.liveChatUnreadCount > 0) {
			item.setIcon(R.drawable.ic_man_list_white_24dp_badged);
		} else {
			item.setIcon(R.drawable.ic_man_list_white_24dp);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void OnQueryAlbumList(boolean isSuccess, String errno,
			String errmsg, AlbumListItem[] itemList) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = GET_ALBUM_LIST;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, itemList);
		msg.obj = response;
		sendUiMessage(msg);
	}

	/**
	 * 相册创建/编辑成功刷新列表
	 */
	public void refresh() {
		// TODO Auto-generated method stub
		QpidApplication.updateAlbumsNeed = false;
		showDoneToast("Done");
		getProgressBar().setVisibility(View.VISIBLE);
		RequestJniAlbum.QueryAlbumList(this);
	}

}
