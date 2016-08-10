package com.qpidnetwork.ladydating.chat.emotion;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.chat.downloader.EmotionImageDownloader;
import com.qpidnetwork.ladydating.chat.emotion.NormalEmotionFragment.OnItemClickCallback;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.request.item.EmotionConfigEmotionItem;

@SuppressLint("InflateParams")
public class EmotionGridviewAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<EmotionConfigEmotionItem> mEmotionList;
	private GridView gridView;
	private EmotionPreviewer preview;
	private boolean canScroll = true;
	private OnItemClickCallback onItemClickCallback;
	private int previewPosition = 0;
	
	public EmotionGridviewAdapter(Context context, List<EmotionConfigEmotionItem> emotionList, GridView gridView){
		this(context, emotionList, gridView, null);
	}
	
	public EmotionGridviewAdapter(Context context, List<EmotionConfigEmotionItem> emotionList, GridView gridView, OnItemClickCallback callback){
		mContext = context;
		mEmotionList = emotionList;
		LiveChatManager.newInstance(null);
		this.gridView = gridView;
		this.gridView.setClickable(true);
		this.gridView.setLongClickable(true);
		this.preview = new EmotionPreviewer(mContext);
		this.onItemClickCallback = callback;
		
		gridView.setOnTouchListener(touch);
		this.gridView.setOnItemClickListener(itemOnClick);
		this.gridView.setOnItemLongClickListener(itemOnLongClick);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mEmotionList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mEmotionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			
			
			float density = mContext.getResources().getDisplayMetrics().density;
			Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
			Point size = new Point();
			
			if(Build.VERSION.SDK_INT > 12){
				display.getSize(size);
			}else{
				size.x = display.getWidth();
				size.y = display.getHeight();
			}
			
			int item_size = (int)(((float)size.x - (int)(3.0f * density)) / 4);
			
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_preminum_emotion, null);

			ViewHolder holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
			holder.price = (TextView) convertView.findViewById(R.id.price);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(item_size, item_size);
			holder.imageView.setLayoutParams(params);
			
			convertView.setTag(holder);
	
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.price.setText( mEmotionList.get(position).price + "");
		new EmotionImageDownloader().displayEmotionImage(holder.imageView, null, mEmotionList.get(position).fileName);
		convertView.setTag(R.id.item_position, position);
		
		//convertView.setOnClickListener(itemOnClick);
		//convertView.setOnLongClickListener(itemLongClick);
		//convertView.setLongClickable(true);
		
		return convertView;
	}
	
	public static class ViewHolder {
		ImageView imageView;
		TextView price;
		//ProgressBar progressBar;
	}
	
	
	OnItemClickListener itemOnClick = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			EmotionConfigEmotionItem b = (EmotionConfigEmotionItem) mEmotionList.get(position);
			Intent intent = new Intent(ChatActivity.SEND_EMTOTION_ACTION);
			intent.putExtra(ChatActivity.EMOTION_ID, b.fileName);
			mContext.sendBroadcast(intent);
			
			if (onItemClickCallback != null ) onItemClickCallback.onItemLongClick();
		}
		
	};
	
	OnItemLongClickListener itemOnLongClick = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			// TODO Auto-generated method stub
//			OtherEmotionConfigEmotionItem b = (OtherEmotionConfigEmotionItem) mEmotionList.get(position);
			if (!preview.isShowing()){
				preview.showAtLocation(arg0.getRootView(), Gravity.CENTER, 0, 0);
				canScroll = false;
				previewPosition = position;
				playEmotion();
				if (onItemClickCallback != null ) onItemClickCallback.onItemLongClick();
			}
			
			return true;
		}
	};
	
	OnTouchListener touch = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			//Log.v(" m", event.getX() + "");
			if (event.getAction() == MotionEvent.ACTION_UP){
				if (preview.isShowing()) preview.dismiss(); 
				if (onItemClickCallback != null ) onItemClickCallback.onItemLongClickUp();
				canScroll = true;
			}
			
			if (canScroll){
				return false;
			}else{
				playEmotionByTouchPosition(0, event.getX(),event.getY());
				return true;
			}
			
			//return false;
		}
		
	};
	
	
	private void playEmotionByTouchPosition(int Xoffset, float x, float y){
		
		int which =  gridView.pointToPosition((int)x, (int)y);
		if (which != AdapterView.INVALID_POSITION){
			if (previewPosition != which){
				previewPosition = which;
				playEmotion();
			}
		}
		

	}
	
	private void playEmotion(){
		EmotionConfigEmotionItem item = (EmotionConfigEmotionItem) this.getItem(previewPosition);
		preview.setEmotionItem(item);
	}
}
