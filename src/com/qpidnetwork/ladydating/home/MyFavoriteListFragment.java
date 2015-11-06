package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.bean.ManInfoBean;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.man.ManProfileActivity;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.livechat.jni.LiveChatUserStatus;
import com.qpidnetwork.request.OnQueryFavourListCallback;
import com.qpidnetwork.request.RequestJniMan;

public class MyFavoriteListFragment extends BaseListViewFragment implements
		ListView.OnItemClickListener, LiveChatManagerOtherListener {

	private static final int FAVORITE_MANLIST_CALLBACK = 0;
	private static final int GET_MAN_USERINFO_CALLBACK = 1;

	public String TAG = MyFavoriteListFragment.class.getName();

	private List<ManInfoBean> mManList;
	private NormalManListAdapter mAdapter;

	private HomeActivity homeActivity;
	private LiveChatManager mLiveChatManager;

	String[] mManIds = null;

	// 存放收藏男士默认返回排序序号
	private HashMap<String, Integer> mFavoriteManSortId = new HashMap<String, Integer>();

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		homeActivity = (HomeActivity) getActivity();
		pageBean.resetPageIndex();
		queryFavoriteManList();
	}

	@Override
	protected void setupListView(ExtendableListView listView) {
		mManList = new ArrayList<ManInfoBean>();
		mAdapter = new NormalManListAdapter(getActivity(), mManList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		setEmptyText("No Favorite mans");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterOtherListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mLiveChatManager.UnregisterOtherListener(this);
	}

	/**
	 * ListView onClick
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		startActivity(new Intent(this.getActivity(), ManProfileActivity.class));
	}

	@Override
	public void handleUiMessage(Message msg) {
		RequestBaseResponse response = (RequestBaseResponse) msg.obj;
		switch (msg.what) {
		case FAVORITE_MANLIST_CALLBACK: {
			mManIds = (String[]) response.body;
			if ((response.isSuccess) && (mManIds != null)
					&& (mManIds.length > 0)) {
				// 获取详情刷新页面
				pageBean.setDataCount(mManIds.length);
				getUserInfoByIds(pageBean.getNextPageIndex());
			} else {
				// 在线列表获取失败处理
				getProgressBar().setVisibility(View.GONE);
				onRefreshComplete();
				Toast.makeText(homeActivity,
						getString(R.string.online_manlist_error),
						Toast.LENGTH_SHORT).show();
			}
		}
			break;
		case GET_MAN_USERINFO_CALLBACK: {
			getProgressBar().setVisibility(View.GONE);
			if (response.isSuccess) {
				ArrayList<ManInfoBean> manList = (ArrayList<ManInfoBean>) response.body;
				if (pageBean.getPageIndex() > 1) {
					// load more
					mManList.addAll(manList);
					notifyListDataChanged();
				} else {
					// refresh
					mManList.clear();
					mManList.addAll(manList);
					notifyListDataChanged();
				}

			} else {
				pageBean.decreasePageIndex();
				Toast.makeText(homeActivity,
						getString(R.string.online_manlist_error),
						Toast.LENGTH_SHORT).show();
			}
			onRefreshComplete();
		}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 通知ListView刷新
	 */
	private void notifyListDataChanged(){
		Collections.sort(mManList, new Comparator<ManInfoBean>() {
			public int compare(ManInfoBean lhs, ManInfoBean rhs) {
				int result = 0;
				if(lhs.isOnline == rhs.isOnline){
					result = (lhs.sort_id > rhs.sort_id ? -1: 1);
				}else{
					result = (lhs.isOnline ? -1 : 1);
				}
				return result;
			};
		});
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.other_list, menu);
		MenuItem item = menu.findItem(R.id.contacts);
		if (homeActivity.liveChatUnreadCount > 0) {
			item.setIcon(R.drawable.ic_man_list_white_24dp_badged);
		} else {
			item.setIcon(R.drawable.ic_man_list_white_24dp);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * 获取收藏男士列表
	 */
	private void queryFavoriteManList() {
		RequestJniMan.QueryFavourList(new OnQueryFavourListCallback() {

			@Override
			public void OnQueryFavourList(boolean isSuccess, String errno,
					String errmsg, String[] itemList) {
				RequestBaseResponse reponse = new RequestBaseResponse(
						isSuccess, errno, errmsg, itemList);
				if (isSuccess && (itemList != null)) {
					mFavoriteManSortId.clear();
					for (int i = 0; i < itemList.length; i++) {
						mFavoriteManSortId.put(itemList[i], i);
					}
				}
				sendUiMessage(FAVORITE_MANLIST_CALLBACK, reponse);
			}
		});
	}

	@Override
	public void onPullDownToRefresh() {
		super.onPullDownToRefresh();
		// 初始化分页信息
		pageBean.resetPageIndex();
		pageBean.setDataCount(0);
		queryFavoriteManList();
	}

	@Override
	public void onPullUpToRefresh() {
		super.onPullUpToRefresh();
		getUserInfoByIds(pageBean.getNextPageIndex());
	}

	@Override
	public void onRefreshComplete() {
		super.onRefreshComplete();
		getRefreshLayout().setCanPullUp(pageBean.hasNextPage());
	}

	/**
	 * 获取指定页男士Id列表对应详情列表
	 * 
	 * @param useIds
	 */
	private void getUserInfoByIds(int pageIndex) {
		int startPos = (pageIndex - 1) * pageBean.getPageSize();
		int endPos = (pageIndex * pageBean.getPageSize() < pageBean
				.getDataCount()) ? (pageIndex * pageBean.getPageSize())
				: pageBean.getDataCount();
		String[] list = new String[endPos - startPos];
		int a = 0;
		for (int i = startPos; i < endPos; i++) {
			list[a] = mManIds[i];
			a++;
		}
		mLiveChatManager.GetUsersInfo(list);
	}
	

	/** =================== livechat interface ====================== */

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {

	}

	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			int seq, LiveChatTalkUserListItem[] list) {
		boolean isSuccess = true;
		if (errType != LiveChatErrType.Success) {
			isSuccess = false;
		}

		ArrayList<ManInfoBean> manList = new ArrayList<ManInfoBean>();
		if (isSuccess) {
			if ((list != null) && (list.length > 0)) {
				for (int i = 0; i < list.length; i++) {
					ManInfoBean item = ManInfoBean.parse(list[i]);
					if (item != null) {
						// 填充排序Id
						if (mFavoriteManSortId != null) {
							if (mFavoriteManSortId.containsKey(item.man_id)) {
								item.sort_id = mFavoriteManSortId
										.get(item.man_id);
							}
						}
						manList.add(item);
					}
				}
			}
		}
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, "",
				errmsg, manList);
		sendUiMessage(GET_MAN_USERINFO_CALLBACK, response);
	}

//	@Override
//	public void OnGetFeeRecentContactList(LiveChatErrType errType,
//			String errmsg, String[] userIds) {
//
//	}
//
//	@Override
//	public void OnGetLadyChatInfo(LiveChatErrType errType, String errmsg,
//			String[] chattingUserIds, String[] chattingInviteIds,
//			String[] missingUserIds, String[] missingInviteIds) {
//
//	}

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LiveChatUserStatus[] userList) {
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
	public void OnRecvTalkEvent(LCUserItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvIdentifyCode(String filePath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnContactStatusChange() {
		// TODO Auto-generated method stub
		
	}
}
