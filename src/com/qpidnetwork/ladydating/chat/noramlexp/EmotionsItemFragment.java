package com.qpidnetwork.ladydating.chat.noramlexp;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.request.item.EmotionConfigEmotionItem;


/**
 * @author Yanni
 * 
 * @version 2016-6-3
 */
public class EmotionsItemFragment extends BaseFragment {

	private TextView tvEmotionPrice;// 价格
	private GridView gvEmotion;
	public OnItemClickCallback itemClickCallback;

	private List<EmotionConfigEmotionItem> mEmotionItemList;
	private EmotionGridviewAdapter mAdapter;

	private int mVpHeight = 0;
	
	public EmotionsItemFragment(){
		
	}

	public EmotionsItemFragment(int vpHeight,
			List<EmotionConfigEmotionItem> emotionItemList) {
		super();
		this.mEmotionItemList = emotionItemList;
		this.mVpHeight = vpHeight;
	}

	public interface OnItemClickCallback {
		public void onItemClick();

		public void onItemLongClick();

		public void onItemLongClickUp();
	}

	public void setOnItemClickCallback(OnItemClickCallback callback) {
		this.itemClickCallback = callback;
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_emotion_vp_gridview, null);
		tvEmotionPrice = (TextView) view.findViewById(R.id.tvEmtionPrice);
		gvEmotion = (GridView) view.findViewById(R.id.gvEmotion);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

//		int gvHeight = (mVpHeight - UnitConversion.dip2px(mContext, 35 + 10));
		int gvHeight = mVpHeight;

		if(gvHeight>0){
			gvEmotion.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, gvHeight));
		}

		if (mEmotionItemList.size() > 0) {
			tvEmotionPrice.setText(getMagicPrice(mEmotionItemList));// 获取价格最大最小值
			mAdapter = new EmotionGridviewAdapter(mContext, gvHeight,mEmotionItemList, gvEmotion, itemClickCallback);
			gvEmotion.setAdapter(mAdapter);
		}
	}

	/**
	 * @param 获取价格
	 */
	private String getMagicPrice(List<EmotionConfigEmotionItem> item) {
		// TODO Auto-generated method stub
		String priceDesc = "";
		Double minPrice = item.get(0).price;
		Double maxPrice = item.get(0).price;
		for (int i = 0; i < item.size(); i++) {
			if (minPrice > item.get(i).price) {
				minPrice = item.get(i).price;
			}
			if (maxPrice < item.get(i).price) {
				maxPrice = item.get(i).price;
			}
		}
		String tips = getActivity().getResources().getString(R.string.livechat_magicIcon_price_desc);
		if (minPrice.equals(maxPrice)){
			priceDesc = String.format(tips, maxPrice.toString());
		}else{
			priceDesc = String.format(tips, minPrice.toString()+ " - " +maxPrice.toString());
		}
		//priceDesc = String.format(tips, minPrice.toString(),maxPrice.toString());
		return priceDesc;
	}

}
