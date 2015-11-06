package com.qpidnetwork.ladydating.customized.view;

/**
 * Author: Martin Shum
 * 
 * This view is structured by google material design
 * 
 * Set icon
 * Set Title
 * Set Message
 * Add button
 * 
 * If you have any questions please read the code 
 * through to find out the answer (Bazinga!).
 * 
 */


import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) 
public class MaterialDialogSingleChoice extends Dialog{
	
	
	public static int DIALOG_MIN_WIDTH = 280;
	
	
	private LinearLayout contentView;
	private int view_padding = 24;
	private int view_margin = 24;
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private OnClickCallback mCallback;
	private int checked_position = -1;
	private String checked_item_string;
	private ListView listView;
	private ThisAdapter adapter;
	
	public MaterialDialogSingleChoice(Context context, String[] items, OnClickCallback callback, String checkedString){
		this(context, items, callback);
		checked_item_string = checkedString;
		createView(items);
	}
	
	public MaterialDialogSingleChoice(Context context, String[] items, OnClickCallback callback, int checkedPostion){
		this(context, items, callback);
		checked_position = checkedPostion;
		createView(items);
	}
	
	public MaterialDialogSingleChoice(Context context, String[] items, OnClickCallback callback){
		this(context, items);
		mCallback = callback;
		createView(items);
	}
	
	public MaterialDialogSingleChoice(Context context, String[] array) {
		super(context);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
	}



    protected void createView(String[] items) {
    	
    	Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);
    	this.getWindow().setLayout((int)dialog_width, LayoutParams.WRAP_CONTENT);
    	
    	//setup content view
		DIALOG_MIN_WIDTH = (int)(280.0f * density);
		view_padding = (int)(24.0f * density);
		view_margin = (int)(20.0f * density);
		
		contentView = new LinearLayout(this.getContext());
		contentView.setMinimumWidth(DIALOG_MIN_WIDTH);
		contentView.setBackgroundResource(R.drawable.rectangle_rounded_angle_white_bg);
		contentView.setGravity(Gravity.TOP | Gravity.CENTER);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setPadding(0, view_padding, 0, view_padding);
		this.setContentView(contentView);

        //title
        LayoutParams titleParams = new LayoutParams((int)dialog_width, LayoutParams.WRAP_CONTENT);
        
        titleParams.setMargins(0, 0, 0, view_margin);
        TextView title = new TextView(this.getContext());
        title.setLayoutParams(titleParams);
        title.setPadding(view_padding, 0, view_padding, 0);
        title.setTextColor(this.getContext().getResources().getColor(R.color.text_color_dark));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setId(android.R.id.title);
        title.setVisibility(View.GONE);
        
        
        
        //listView
        int offset = (int)((24.0f + 56.0f + 48.0f + 72.0f) * this.getContext().getResources().getDisplayMetrics().density);
        int LH = size.y - offset;
        
        if ((float)items.length * 48.0f * density < LH){
        	LH = LayoutParams.MATCH_PARENT;
        }
        
        LayoutParams listParams = new LayoutParams((int)dialog_width, LH);
        listView = new ListView(this.getContext());
        listView.setLayoutParams(listParams);
        listView.setVisibility(View.VISIBLE);
        listView.setFadingEdgeLength(0);
        listView.setDivider(null);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				dismiss();
				if (mCallback != null) mCallback.onClick(arg0, arg1, arg2);
			}
        	
        });
        
        if (checked_item_string != null){
        	adapter = new ThisAdapter(this.getContext(), items, checked_item_string);
        }else{
        	adapter = new ThisAdapter(this.getContext(), items, checked_position);
        	if (checked_position > 3){
        		
        		listView.post(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						listView.smoothScrollToPosition(checked_position);
					}
        			
        		});
        	}
        }
        
        listView.setAdapter(adapter);
        contentView.addView(title);
        contentView.addView(listView);
        
    }
    
    
    public interface OnClickCallback{
    	public void onClick (AdapterView<?> adptView, View v, int which);
    }
    
    
    public LinearLayout getContentView(){
    	return contentView;
    }
    
    public void setTitle(CharSequence title){
    	getTitle().setText(title);
    	getTitle().setVisibility(View.VISIBLE);
    }
    
    public TextView getTitle(){
    	return (TextView)contentView.findViewById(android.R.id.title);
    }
    
    public void setSelectItem(int position){
    	checked_position = position;
    	adapter.notifyDataSetChanged();
    }
    
    public void addButton(Button button){
    	LinearLayout v = (LinearLayout)contentView.findViewById(android.R.id.extractArea);
    	v.addView(button);
    }
    

}


class ThisAdapter extends ArrayAdapter<ArrayList<String>>{

	private String[] mListData;
	private Context mContext;
	private int mCheckedPosition = -1;
	private String mCheckedItemString = null;
	private ArrayList<View> views;
	
	public ThisAdapter(Context context, String[] objects, int checkedPosition) {
		this(context, objects);
		mCheckedPosition = checkedPosition;
		//Log.v("", "adpt: " + mCheckedPosition);
	}
	
	public ThisAdapter(Context context, String[] objects, String checkedItemString) {
		this(context, objects);
		mCheckedItemString = checkedItemString;
	}
	
	public ThisAdapter(Context context, String[] objects) {
		super(context, 0);
		mListData = objects;
		mContext = context;
		views = new ArrayList<View>();
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListData.length;
	}



	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		if (position + 1 <= views.size()){
			return views.get(position);
		}
		
		float density = mContext.getResources().getDisplayMetrics().density;
		int item_height = (int)(48.0f * density);
		int padding = (int)(24.0f * density);
		int check_indicator_size = (int)(36.0f * density);
		
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, item_height);
		LinearLayout item = new LinearLayout(mContext);
		item.setPadding(padding, 0, padding, 0);
		item.setOrientation(LinearLayout.HORIZONTAL);
		item.setLayoutParams(params);
		item.setBackgroundResource(R.drawable.touch_feedback_holo_light);
		
		TextView text = new TextView(mContext);
		LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tp.weight = 1;
		tp.gravity = Gravity.LEFT | Gravity.CENTER;
		text.setTextSize(18);
		text.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
		text.setLayoutParams(tp);
		text.setId(android.R.id.text1);
		text.setText(mListData[position]);
		
		
		ImageView check_indicator = new ImageView(mContext);
		LinearLayout.LayoutParams cip = new LinearLayout.LayoutParams(check_indicator_size, check_indicator_size);
		cip.weight = 0;
		cip.gravity = Gravity.LEFT | Gravity.CENTER;
		check_indicator.setScaleType(ScaleType.CENTER);
		check_indicator.setId(android.R.id.icon);
		check_indicator.setVisibility(View.GONE);
		check_indicator.setImageResource(R.drawable.ic_done_grey600_24dp);
		check_indicator.setLayoutParams(cip);
		if (position == mCheckedPosition || mListData[position].equals(mCheckedItemString)){
			check_indicator.setVisibility(View.VISIBLE);
			mCheckedPosition = position;
		}
		
		item.addView(text);
		item.addView(check_indicator);
		
		views.add(item);
		
		return item;
	
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
