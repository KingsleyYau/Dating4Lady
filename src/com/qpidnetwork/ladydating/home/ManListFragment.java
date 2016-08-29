package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.bean.ManInfoBean;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.man.ManProfileActivity;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.request.OnQueryManListCallback;
import com.qpidnetwork.request.OnQueryManRecentViewListCallback;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestEnum.QueryType;
import com.qpidnetwork.request.RequestJniMan;
import com.qpidnetwork.request.item.ManListItem;
import com.qpidnetwork.request.item.ManRecentViewListItem;

public class ManListFragment extends BaseListViewFragment implements
		MaterialDropDownMenu.OnClickCallback, View.OnClickListener,
		ListView.OnItemClickListener, LiveChatManagerOtherListener,
		ManSearcherWindow.Callback {

	private static final int ONLINE_MANLIST_CALLBACK = 0;
	private static final int SEARCH_MANLIST_CALLBACK = 1;// All,条件查询及Id
	private static final int RECENT_VISITORS_MANLIST_CALLBACK = 2;
	private static final int GET_MAN_USERINFO_CALLBACK = 3;

	public String TAG = ManListFragment.class.getName();
	
	private HomeActivity homeActivity;
	private View mListviewHeader;

	private List<ManInfoBean> mManList;
	private NormalManListAdapter mAdapter;

	private LiveChatManager mLiveChatManager;
	private CategoryType mCategoryType = CategoryType.ONLINE;

	// 搜索设置数据保存(默认搜索ALL)
	QueryType query_type = QueryType.DEFAULT;
	String man_id = "";
	int from_age = 18;
	int to_age = 99;
	Country country = Country.Unknow;
	boolean isWithPhoto = false;

	// 在线男士及最近访问存放Ids列表，用于更多处理
	String[] mManIds = null;
	
	//记录GetUsersInfo()返回数据是否是当前请求
	private int currSeq = -1;

	private enum CategoryType {
		ONLINE, SEARCH, RECENT_VISITORS
	};

	/**
	 * 头部分类标签条
	 */
	private String[] categoryText = null;

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		homeActivity = (HomeActivity) getActivity();
		categoryText = new String[] { getString(R.string.online),
				getString(R.string.all), getString(R.string.recent_visitors) };
		mCategoryType = CategoryType.ONLINE;
		pageBean.resetPageIndex();
		initRefreshData();
		
		// 统计sub的子页
//		homeActivity.onAnalyticsPageSelected(0, 0, mCategoryType.ordinal());
	}

	@Override
	protected void setupListView(ExtendableListView listView) {

		listView.addHeaderView(creaHeaderView(), null, false);

		mManList = new ArrayList<ManInfoBean>();
		mAdapter = new NormalManListAdapter(getActivity(), mManList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.search:
			showManSearchWindow();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.man_list, menu);
		MenuItem item = menu.findItem(R.id.contacts);
		if (homeActivity.liveChatUnreadCount > 0) {
			item.setIcon(R.drawable.ic_man_list_white_24dp_badged);
		} else {
			item.setIcon(R.drawable.ic_man_list_white_24dp);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * 显示搜索窗口
	 */
	private void showManSearchWindow() {
		ManSearcherWindow manSearch = new ManSearcherWindow(this.getActivity());
		manSearch.setCallback(this);
		manSearch.showAsDropDown(homeActivity.getToolBar());
	}

	/**
	 * 弹出男士列表（online/all/Recent vistors 窗口）
	 */
	private void showCategoryMenu() {

		Point point = new Point();
		point.x = Converter.dp2px(240);
		point.y = LayoutParams.WRAP_CONTENT;
		new MaterialDropDownMenu(getActivity(), categoryText, this, point)
				.showAsDropDownAtVerticalCenter(mListviewHeader);
	}

	@Override
	/**
	 * 男士列表分类标签（online/all/Recent vistors 窗口）点击响应回调
	 */
	public void onClick(AdapterView<?> adptView, View v, int which) {
		((TextView) mListviewHeader.findViewById(R.id.category))
				.setText(categoryText[which]);
		onCategoryClick(which);
		
		// 统计sub的子页
		homeActivity.onAnalyticsPageSelected(0, 0, which);
	}

	/**
	 * listview header (分类标签)
	 * 
	 * @return
	 */
	private View creaHeaderView() {
		mListviewHeader = LayoutInflater.from(getActivity()).inflate(
				R.layout.view_home_manlist_header, null);
		TextView category = (TextView) mListviewHeader
				.findViewById(R.id.category);
		category.setOnClickListener(this);
		return mListviewHeader;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.category:
			showCategoryMenu();
			break;
		default:
			break;
		}
	}

	@Override
	/* listview item click */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ManInfoBean bean = mManList.get(position-1); 
		ManProfileActivity.launchManProfileActivity(getActivity(), bean.man_id, bean.userName, bean.photoUrl);
	}

	// search button点击响应
	@Override
	public void OnClickSearch(View v, int selectedCountryIndex, int minAge,
			int maxAge, boolean isWithPhoto) {
		((TextView) mListviewHeader.findViewById(R.id.category)).setText(getString(R.string.search));
		resetCategoryData(CategoryType.SEARCH);
		query_type = QueryType.DEFAULT;
		from_age = minAge;
		to_age = maxAge;
		country = Country.values()[selectedCountryIndex];
		this.isWithPhoto = isWithPhoto;
		getProgressBar().setVisibility(View.VISIBLE);
		initRefreshData();
		
		// 统计search screen
		homeActivity.onAnalyticsPageSelected(0, 0, -1);
	}

	@Override
	public void OnClickGo(View v, String ladyId) {
		((TextView) mListviewHeader.findViewById(R.id.category)).setText(getString(R.string.search));
		resetCategoryData(CategoryType.SEARCH);
		query_type = QueryType.BYID;
		man_id = ladyId;
		getProgressBar().setVisibility(View.VISIBLE);
		initRefreshData();
		
		// 统计search screen
		homeActivity.onAnalyticsPageSelected(0, 0, -1);
	}

	/**
	 * 分类标签点击选择
	 * 
	 * @param which
	 *            选中标签
	 */
	private void onCategoryClick(int which) {
		switch (which) {
		case 0:
			// online
			resetCategoryData(CategoryType.ONLINE);
			break;
		case 1:
			// search all
			resetCategoryData(CategoryType.SEARCH);
			break;
		case 2:
			// recent visitor
			resetCategoryData(CategoryType.RECENT_VISITORS);
			break;

		default:
			break;
		}
		getProgressBar().setVisibility(View.VISIBLE);
		initRefreshData();
	}

	/**
	 * 切换Category，重置数据
	 * 
	 * @param categoryType
	 */
	private void resetCategoryData(CategoryType categoryType) {
		mCategoryType = categoryType;
		// 初始化分页信息
		pageBean.resetPageIndex();
		pageBean.setDataCount(0);

		// 列表清空
		mManIds = null;
		mManList.clear();
		mAdapter.notifyDataSetChanged();

		// 搜索相关参数，默认修改为搜索所有
		query_type = QueryType.DEFAULT;
		man_id = "";
		from_age = 18;
		to_age = 99;
		country = Country.Unknow;
		isWithPhoto = false;
	}

	@Override
	protected void handleUiMessage(Message msg) {
		RequestBaseResponse response = (RequestBaseResponse) msg.obj;
		switch (msg.what) {
		case ONLINE_MANLIST_CALLBACK: {
			if ((mCategoryType == CategoryType.ONLINE)) {
				mManIds = (String[]) response.body;
				hideEmptyOrErrorTips();//暂时不处理无数据
				if ((response.isSuccess)) {
					// 获取详情刷新页面
					if (mManIds != null && mManIds.length > 0){
						pageBean.resetPageIndex();
						pageBean.setDataCount(mManIds.length);
						getUserInfoByIds(pageBean.getNextPageIndex());
					}else{
						mManList.clear();
						mAdapter.notifyDataSetChanged();
						if(isAdded()){
							setEmptyOrErrorTips(getString(R.string.no_online_members_at_the_moment));
						}
						onRefreshComplete();
					}
					
				} else {
					// 在线列表获取失败处理
					onRefreshComplete();
					if (mManList == null || mManList.size() == 0){
						if(isAdded()){	
							setEmptyOrErrorTips(getString(R.string.online_manlist_error));
						}
					}
					
				}
			}
		}
			break;
		case SEARCH_MANLIST_CALLBACK: {
			if (mCategoryType == CategoryType.SEARCH) {
				getProgressBar().setVisibility(View.GONE);
				if (response.isSuccess) {
					ArrayList<ManInfoBean> manList = (ArrayList<ManInfoBean>) response.body;
					Log.i(TAG, "pageIndex: " + pageBean.getPageIndex());
					if (pageBean.getPageIndex() > 1) {
						// load more
						mManList.addAll(manList);
						mAdapter.notifyDataSetChanged();
					} else {
						// refresh
						mManList.clear();
						mManList.addAll(manList);
						mAdapter.notifyDataSetChanged();
						if(mManList == null || mManList.size() == 0){
							if(isAdded()){
								if(!TextUtils.isEmpty(man_id)){
									setEmptyOrErrorTips(String.format(getString(R.string.profile_id_x_not_found), man_id));
								}else{
									setEmptyOrErrorTips(getString(R.string.search_man_not_match));
								}
							}
						}else{
							hideEmptyOrErrorTips();
						}
					}
					
				} else {
					if (mManList == null || mManList.size() == 0){
						if(isAdded()){
							setEmptyOrErrorTips(getString(R.string.online_manlist_error));
						}
					}
					
					pageBean.decreasePageIndex();
				}
				onRefreshComplete();
			}
		}
			break;
		case RECENT_VISITORS_MANLIST_CALLBACK: {
			if ((mCategoryType == CategoryType.RECENT_VISITORS)) {
				ManRecentViewListItem[] recentVisit = (ManRecentViewListItem[]) response.body;
				if(recentVisit != null && recentVisit.length > 0){
					String[] manIds = new String[recentVisit.length];
					for(int i=0; i<recentVisit.length; i++){
						manIds[i] = recentVisit[i].man_id;
					}
					mManIds = manIds;
				}
				hideEmptyOrErrorTips();//暂时不处理无数据
				if (response.isSuccess) {
					// 获取详情刷新页面
					if (mManIds != null && mManIds.length > 0){
						pageBean.setDataCount(mManIds.length);
						getUserInfoByIds(pageBean.getNextPageIndex());
					}else{
						mManList.clear();
						mAdapter.notifyDataSetChanged();
						if(isAdded()){
							setEmptyOrErrorTips(getString(R.string.no_recent_visitors));
						}
						onRefreshComplete();
					}
					
					
				} else {
					// 在线列表获取失败处理
					onRefreshComplete();
					if (mManList == null || mManList.size() == 0){
						if(isAdded()){
							setEmptyOrErrorTips(getString(R.string.online_manlist_error));
						}
					}
				}
			}
		}
			break;
		case GET_MAN_USERINFO_CALLBACK: {
			if (mCategoryType == CategoryType.ONLINE
					|| mCategoryType == CategoryType.RECENT_VISITORS) {
				getProgressBar().setVisibility(View.GONE);
				if (response.isSuccess) {
					ArrayList<ManInfoBean> manList = (ArrayList<ManInfoBean>) response.body;
					if (pageBean.getPageIndex() > 1) {
						// load more
						mManList.addAll(manList);
						mAdapter.notifyDataSetChanged();
					} else {
						// refresh
						mManList.clear();
						mManList.addAll(manList);
						mAdapter.notifyDataSetChanged();
					}
					
					
				} else {
					pageBean.decreasePageIndex();
					
					if (mManList == null || mManList.size() == 0){
						if(isAdded()){
							setEmptyOrErrorTips(getString(R.string.online_manlist_error));
						}
					}
					
				}
				
				onRefreshComplete();
			}
		}
			break;
		default:
			break;
		}
	}
	
	private void setEmptyOrErrorTips(String emptyText){
		getProgressBar().setVisibility(View.GONE);
		getListView().setEmptyView(null);
		setEmptyText(emptyText);
		getEmptyView().setVisibility(View.VISIBLE);
	}
	
	/**
	 * 刷新有数据后要控制隐藏无数据view
	 */
	private void hideEmptyOrErrorTips(){
		getListView().setEmptyView(getEmptyView());
		getEmptyView().setVisibility(View.GONE);
	}

	@Override
	public void onRefreshComplete() {
		// TODO Auto-generated method stub
		super.onRefreshComplete();
		Log.i(TAG, "hasNextPage: " + pageBean.hasNextPage() + " datacount: " + pageBean.getDataCount());
		getRefreshLayout().setCanPullUp(pageBean.hasNextPage());
		getListView().setVisibility(View.VISIBLE);
		getProgressBar().setVisibility(View.GONE);
	}

	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		// 初始化分页信息
		pageBean.resetPageIndex();
		pageBean.setDataCount(0);
		
		initRefreshData();
	}

	@Override
	public void onPullUpToRefresh() {
		// TODO Auto-generated method stub
		super.onPullUpToRefresh();
		loadMoreData();
	}

	/**
	 * 上拉刷新及切换Category初始化请求数据
	 */
	private void initRefreshData() {
		
		hideEmptyOrErrorTips();
		switch (mCategoryType) {
		case ONLINE: {
			queryOnlineManlist();
		}
			break;
		case RECENT_VISITORS: {
			queryRecentVisitors();
		}
			break;
		case SEARCH:
			querySearchManlist(pageBean.getNextPageIndex());
			break;
		default:
			break;
		}
	}

	/**
	 * 加载更多时刷新数据
	 */
	private void loadMoreData() {
		switch (mCategoryType) {
		case ONLINE:
		case RECENT_VISITORS: {
			getUserInfoByIds(pageBean.getNextPageIndex());
		}
			break;
		case SEARCH: {
			Log.i(TAG, "loadMoreData");
			querySearchManlist(pageBean.getNextPageIndex());
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 获取最近访问男士列表 对应（ManListType.RECENT_VISITORS）
	 */
	private void queryRecentVisitors() {
		RequestJniMan
				.QueryManRecentViewList(new OnQueryManRecentViewListCallback() {

					@Override
					public void OnQueryManRecentViewList(boolean isSuccess,
							String errno, String errmsg,
							ManRecentViewListItem[] itemList) {
						RequestBaseResponse reponse = new RequestBaseResponse(
								isSuccess, errno, errmsg, itemList);
						sendUiMessage(RECENT_VISITORS_MANLIST_CALLBACK, reponse);
					}
				});
	}

	/**
	 * 搜索男士列表（包括ALL，条件搜索，Id搜索）
	 */
	private void querySearchManlist(final int pageIndex) {
		int fromAge = Calendar.getInstance().get(Calendar.YEAR) - to_age;
		int toAge = Calendar.getInstance().get(Calendar.YEAR) - from_age;
		Log.i(TAG, "querySearchManlist pageIndex: " + pageIndex);
		RequestJniMan.QueryManList(pageIndex, pageBean.getPageSize(),
				query_type, man_id, fromAge, toAge, country, isWithPhoto,
				new OnQueryManListCallback() {

					@Override
					public void OnQueryManList(boolean isSuccess, String errno,
							String errmsg, ManListItem[] itemList,
							int totalCount) {
						ArrayList<ManInfoBean> manList = new ArrayList<ManInfoBean>();
						if (isSuccess) {
							pageBean.setDataCount(totalCount);
							if ((itemList != null) && (itemList.length > 0)) {
								for (int i = 0; i < itemList.length; i++) {
									ManInfoBean item = ManInfoBean
											.parse(itemList[i]);
									if (item != null) {
										manList.add(item);
									}
								}
							}
						}
						RequestBaseResponse response = new RequestBaseResponse(
								isSuccess, errno, errmsg, manList);
						sendUiMessage(SEARCH_MANLIST_CALLBACK, response);
					}
				});
	}

	/**
	 * 获取在线男士Ids列表
	 */
	private void queryOnlineManlist() {
		if(!mLiveChatManager.SearchOnlineMan(18, 99)){
			RequestBaseResponse response = new RequestBaseResponse(false, "",
					"", new String[0]);
			sendUiMessage(ONLINE_MANLIST_CALLBACK, response);
		};
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
		currSeq = mLiveChatManager.GetUsersInfo(list);
		if(currSeq == -1){
			//未登录无回调
			RequestBaseResponse response = new RequestBaseResponse(false, "",
					"", null);
			sendUiMessage(GET_MAN_USERINFO_CALLBACK, response);
		}
	}

	/** =================== livechat interface ====================== */

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		boolean isSuccess = true;
		if (errType != LiveChatErrType.Success) {
			isSuccess = false;
		}
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, "",
				errmsg, userIds);
		sendUiMessage(ONLINE_MANLIST_CALLBACK, response);
	}

	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			int seq, LiveChatTalkUserListItem[] list) {
		if(seq == currSeq){
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
							manList.add(item);
						}
					}
				}
			}
			RequestBaseResponse response = new RequestBaseResponse(isSuccess, "",
					errmsg, manList);
			sendUiMessage(GET_MAN_USERINFO_CALLBACK, response);
		}
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
	public void OnRecvIdentifyCode(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnContactListChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnTransStatusChange() {
		// TODO Auto-generated method stub
		
	}

}
