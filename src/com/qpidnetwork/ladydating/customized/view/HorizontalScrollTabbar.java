package com.qpidnetwork.ladydating.customized.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;

public class HorizontalScrollTabbar extends HorizontalScrollView{
	
	private Context mContext;
	private RadioGroup rg_nav_content;
	private View indicator;
	
	private int itemWidth = 0;
	private int currPosition = 0;
	private int widowWidth = 0;
	
	private OnHorizontalScrollTitleBarSelected mOnHorizontalScrollTitleBarSelected;
	
	public HorizontalScrollTabbar(Context context) {
        super(context);
        initView(context);
    }

    public HorizontalScrollTabbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    private void initView(Context context){
    	this.mContext = context;
    	
    	DisplayMetrics dm = SystemUtil.getDisplayMetrics(context);
    	widowWidth = dm.widthPixels;
        
    	View view = LayoutInflater.from(context).inflate(R.layout.view_horizontal_title_bar, null);
    	rg_nav_content = (RadioGroup)view.findViewById(R.id.rg_nav_content);
    	indicator = (View)view.findViewById(R.id.indicator);
    	addView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
    
    /**
     * 初始化titlebar
     * @param title 标题数组（以此判断title数目）
     * @param currPosition 当前选中按钮
     * @param itemWidth 单Item宽度
     */
    public void setParams(String[] title, int currPosition, int itemWidth){
    	this.itemWidth = itemWidth;
    	//先清除所有子，防止重复添加
    	rg_nav_content.removeAllViews();
    	if(title != null){
    		for(int i=0; i<title.length; i++){
    			rg_nav_content.addView(createTitleItem(i, title[i]));
    		}
    	}
    	indicator.setLayoutParams(new FrameLayout.LayoutParams(itemWidth,
                UnitConversion.dip2px(mContext, 2)));
    	setSelected(currPosition);
    	initListener();
    }
    
    private void initListener(){
    	rg_nav_content.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId != currPosition){
					setSelected(checkedId);
				}
			}
		});
    }
    
    /**
     * 创建单个菜单
     * @param position
     * @param title
     * @return
     */
    private View createTitleItem(int position, String title){
    	RadioButton radioButton = (RadioButton) LayoutInflater.from(mContext).inflate(
                R.layout.item_horizontal_scroll_title, null);;
    	radioButton.setId(position);
    	radioButton.setText(title);
    	radioButton.setLayoutParams(new LayoutParams(itemWidth,
                LayoutParams.MATCH_PARENT));
    	return radioButton;
    }
    
    /**
     * 控制当前选中按钮
     * @param position
     */
    public void setSelected(int position){
    	this.currPosition = position;
    	View child = rg_nav_content.getChildAt(position);
    	if(child != null){
    		((RadioButton)child).setChecked(true);
    	}
    	
    	/*移动指示图标*/
    	FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)indicator.getLayoutParams();
		params.leftMargin = itemWidth*position;
		indicator.setLayoutParams(params);
		
		int needScroll = (position + 1)*itemWidth - widowWidth;
		if(needScroll >0){
			scrollTo(needScroll, 0);
		}else{
			scrollTo(0, 0);
		}
		
		if(mOnHorizontalScrollTitleBarSelected != null){
			mOnHorizontalScrollTitleBarSelected.onTitleBarSelected(position);
		}
    }
    
    public void setOnHorizontalScrollTitleBarSelected(OnHorizontalScrollTitleBarSelected listener){
    	mOnHorizontalScrollTitleBarSelected = listener;
    }
    
    
    public interface OnHorizontalScrollTitleBarSelected{
    	public void onTitleBarSelected(int position);
    }
}
