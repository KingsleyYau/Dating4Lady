package com.qpidnetwork.ladydating.album;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import com.qpidnetwork.ladydating.album.AlbumPreviewActivity.PreviewType;
import com.qpidnetwork.request.item.AlbumPhotoItem;
import com.qpidnetwork.request.item.AlbumVideoItem;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-23
 */
public class AlbumPreviewAdapter extends FragmentPagerAdapter{

	private ArrayList<AlbumPhotoItem> mAlbumPhotoList;
	private ArrayList<AlbumVideoItem> mAlbumVideoList;
	private PreviewType previewType;
	
	private HashMap<Integer, WeakReference<Fragment>> mPageReference;

	@SuppressLint("UseSparseArrays")
	public AlbumPreviewAdapter(FragmentActivity activity,PreviewType previewType,ArrayList<AlbumPhotoItem> mAlbumPhotoList,ArrayList<AlbumVideoItem> mAlbumVideoList) {
		super(activity.getSupportFragmentManager());
		this.previewType = previewType;
		this.mAlbumPhotoList = mAlbumPhotoList;
		this.mAlbumVideoList = mAlbumVideoList;
		mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
	}


	public Fragment getFragment(int position) {
		Fragment fragment = null;
		if (mPageReference.containsKey(position)) {
			fragment = mPageReference.get(position).get();
		}
		return fragment;
	}

	@Override
	public int getCount() {
		int count = 0;
		switch (previewType) {
		case PhotoPreview:
			if(mAlbumPhotoList!=null){
				count = mAlbumPhotoList.size();
			}
			break;
		case VideoPreview:
			if(mAlbumVideoList!=null){
				count = mAlbumVideoList.size();
			}
			break;
		}
		return count;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = null;
		if (mPageReference.containsKey(position)) {
			fragment = mPageReference.get(position).get();
		}
		if (fragment == null) {
			switch (previewType) {
			case PhotoPreview:
				fragment = AlbumPreviewFragment.getInstance(mAlbumPhotoList.get(position));
				break;
			case VideoPreview:
				fragment = AlbumPreviewFragment.getInstance(mAlbumVideoList.get(position));
				break;
			}
			mPageReference.put(position, new WeakReference<Fragment>(fragment));
		}
		return fragment;
	}

}
