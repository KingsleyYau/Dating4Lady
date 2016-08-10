package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.stickygridheaders.StickyGridHeadersBaseAdapter;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.AlbumDetailActivity.Category;
import com.qpidnetwork.ladydating.album.AlbumVideoListFragment.AlbumVideo;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.RequestJniAlbum.ReviewStatus;
import com.qpidnetwork.request.item.AlbumVideoItem;
import com.qpidnetwork.tool.ImageViewLoader;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-22
 */
public class AlbumVideoAdapter implements StickyGridHeadersBaseAdapter{
	private final DataSetObservable mDataSetObservable = new DataSetObservable();

	private Context mContext;// 上下文对象
	private String albumId;
	private String albumName;
	private ArrayList<AlbumVideo> mAlbumItemList;
	private ArrayList<AlbumVideoItem> mAlbumVideoList;
	private boolean isIniting = false;//用于处理第一次初始化时不显示空提示
	
	public AlbumVideoAdapter(Context mContext,String albumId,String albumName,ArrayList<AlbumVideo> mAlbumItemList,ArrayList<AlbumVideoItem> mAlbumVideoList) {
		super();
		this.mContext = mContext;
		this.albumId = albumId;
		this.albumName = albumName;
		this.mAlbumItemList = mAlbumItemList;
		this.mAlbumVideoList = mAlbumVideoList;
	}
	
	public interface UpdateCallBack{
		void onUpdateCallBack(String requestId,int position);
	}

	@Override
	public boolean isEmpty() {
		boolean isEmpty = false;
		if(!isIniting){
			if(mAlbumItemList == null 
					|| mAlbumItemList.size() == 0){
				isEmpty = true;
			}
		}
		return isEmpty;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getNumHeaders() {
		return mAlbumItemList.size();
	}

	@Override
	public int getCountForHeader(int header) {
		return mAlbumItemList.get(header).videoItems.size();
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeadViewHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_for_album_detail_list_head, null);
			holder = new HeadViewHolder(convertView);
		}else{
			holder = (HeadViewHolder) convertView.getTag();
		}
		holder.category.setText(categoryToString(mAlbumItemList.get(position).category));
		return convertView;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ItemViewHolder holder;
		if (convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_for_album_detail_list_gridview, null);
			holder = new ItemViewHolder(convertView);
		}else{
			holder = (ItemViewHolder)convertView.getTag();
			convertView.setTag(holder);
		}
		
		final AlbumVideoItem item = mAlbumVideoList.get(position);
		
		holder.uploadProgress.setVisibility(View.GONE);
		holder.retryButton.setVisibility(View.GONE);
		holder.editButton.setVisibility(View.GONE);
		holder.title.setText(item.title);
		
		setupImageSize(holder.photo);
		
		if (null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if(!TextUtils.isEmpty(item.thumbUrl)){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.thumbUrl);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.default_photo_100dp));
			holder.imageDownLoader.DisplayImage(holder.photo, item.thumbUrl, localPath, null);
		}
		
		
		convertView.setClickable(true);
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(item.reviewStatus==ReviewStatus.ReviewD){
					EditVideoActivity.launchWithEditVideoMode(mContext,albumId,albumName,item);
				}else{
					AlbumPreviewActivity.launchNoramlVideoActivity(mContext, mAlbumVideoList,position);
				}
			}
		});
		
		switch (item.reviewStatus){
		case ReviewD:
			holder.editButton.setVisibility(View.VISIBLE);
			holder.videoButton.setVisibility(View.GONE);
			break;
		default:
			holder.videoButton.setVisibility(View.VISIBLE);
			break;
		
		}
		return convertView;
	}


	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		mDataSetObservable.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		mDataSetObservable.unregisterObserver(observer);

	}
	
	public void notifyDataSetChanged() {
	   mDataSetObservable.notifyChanged();
	}
	
	/**
	 * 设置当前是否初始化状态
	 * @param isInit
	 */
	public void setIsIniting(boolean isInit){
		isIniting = isInit;
	}
	
	private int setupImageSize(View v){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
		int size = (DeviceUtil.getScreenSize().x - (Converter.dp2px(4 * 2))) / 3;
		params.width = size;
		params.height = size;
		v.setLayoutParams(params);
		return size;
	}

	private class HeadViewHolder {

		public TextView category;

		public HeadViewHolder(View v) {
			category = (TextView) v.findViewById(R.id.category);
			v.setTag(this);
		}
	}
	
	public class ItemViewHolder{
		public ImageView photo;
		public TextView title;
		public ImageButton editButton;
		public ImageButton retryButton;
		public ImageButton videoButton;
		public MaterialProgressBar uploadProgress;
		public ImageViewLoader imageDownLoader;
		
		public ItemViewHolder(View convertView){
			this.photo = (ImageView)convertView.findViewById(R.id.photo);
			this.title = (TextView)convertView.findViewById(R.id.title);
			this.editButton = (ImageButton)convertView.findViewById(R.id.editButton);
			this.retryButton = (ImageButton)convertView.findViewById(R.id.retryButton);
			this.videoButton = (ImageButton)convertView.findViewById(R.id.videoButton);
			this.uploadProgress = (MaterialProgressBar)convertView.findViewById(R.id.uploadProgres);
			this.imageDownLoader = null;
			this.videoButton.setVisibility(View.VISIBLE);
			convertView.setTag(this);
		}
	}
	
	/**
	 * 获取头显示
	 * @return
	 */
	private String categoryToString(Category catefory){
		String title = "";
		switch (catefory) {
		case PAST:
			title = mContext.getResources().getString(R.string.album_detail_title_pass);
			break;
		case UNDER_REVIEW:
			title = mContext.getResources().getString(R.string.album_detail_title_under_review);
			break;
		case REQUIRED_EDIT:
			title = mContext.getResources().getString(R.string.album_detail_title_required_edit);
			break;
		case REJECTED:
			title = mContext.getResources().getString(R.string.album_detail_title_rejected);
			break;
		default:
			break;
		}
		return title;
	}

}
