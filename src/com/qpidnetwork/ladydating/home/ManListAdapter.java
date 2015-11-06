package com.qpidnetwork.ladydating.home;

import java.util.List;
import java.util.Random;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ManListAdapter extends BaseAdapter{

	private List<ManItem> manList;
	private Context context;
	private int[] brandingColors = new int[]{
			R.color.brand_color_light11,
			R.color.brand_color_light12,
			R.color.brand_color_light13,
			R.color.brand_color_light14,
			R.color.brand_color_light15,
	};
	
	public ManListAdapter(Context context, List<ManItem> manList){
		this.context = context;
		this.manList = manList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return manList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		ManItem manItem = manList.get(position);
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_home_man_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		Random random = new Random();
		holder.image.setBackgroundColor(context.getResources().getColor(brandingColors[random.nextInt(brandingColors.length)]));
		holder.firstName.setText(manItem.firstname);
		holder.age.setText(context.getString(R.string.x_years_old, manItem.age));
		holder.country.setText(manItem.country);
		calculateImageViewSize(holder.image);
		
		return convertView;
	}
	
	
	private void calculateImageViewSize(ImageView imageView){
		int size = (DeviceUtil.getScreenSize().x - Converter.dp2px(12)) / 2;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)imageView.getLayoutParams();
		params.height = size;
		params.width = size;
	}
	
	private class ViewHolder{
		public ImageView image;
		public TextView firstName;
		public TextView age;
		public TextView country;
		
		public ViewHolder(View v){
			this.image = (ImageView)v.findViewById(R.id.image);
			this.firstName = (TextView)v.findViewById(R.id.firstName);
			this.age = (TextView)v.findViewById(R.id.age);
			this.country = (TextView)v.findViewById(R.id.country);
			v.setTag(this);
		}
	}
	
	
	public static class ManItem{
		public String imageUrl;
		public String firstname;
		public String age;
		public String country;
		
		public ManItem(String imageUrl, String firstName, String age, String country){
			this.imageUrl = imageUrl;
			this.firstname = firstName;
			this.age = age;
			this.country = country;
			
		}
		
	}

}
