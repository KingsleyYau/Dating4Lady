package com.qpidnetwork.ladydating.home;


import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogSingleChoice;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.customized.view.MaterialRaisedButton;
import com.qpidnetwork.ladydating.customized.view.RangeSeekBar;
import com.qpidnetwork.ladydating.customized.view.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.qpidnetwork.request.RequestEnum.Country;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class ManSearcherWindow extends PopupWindow implements View.OnClickListener, PopupWindow.OnDismissListener{
	
	private static int defaultHeight = LayoutParams.WRAP_CONTENT;
	private static int defaultWidth = LayoutParams.MATCH_PARENT;
	
	private HomeActivity context;
	public MaterialRaisedButton buttonSearch;
	public MaterialRaisedButton buttonGo;
	public MaterialEditText editTextId;

	public LinearLayout layoutAge;
	public TextView country;
	public RangeSeekBar<Integer> rangeSeekBar;
	public TextView textViewMin;
	public TextView textViewMax;
	public CheckBox withPhoto;
	
	public Callback callback;
	public MaterialDialogSingleChoice countryChooser;
	public int selectedCountry = Country.Unknow.ordinal();
	public Integer miMin = 18;
	public Integer miMax = 99;
	
	public interface Callback {
		public void OnClickSearch(View v, int selectedCountryIndex, int minAge, int maxAge, boolean isWithPhoto);
		public void OnClickGo(View v, String ladyId);
	}
	
	
	public ManSearcherWindow(Context context){
		super(context);
		this.context = (HomeActivity)context;
		this.setContentView(createContentView());
		this.setFocusable(true);
		this.setTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setHeight(defaultHeight);
		this.setWidth(defaultWidth);
		this.setAnimationStyle(R.style.DropDownListAnimation);
		this.setOnDismissListener(this);
	}
	
	public View createContentView(){
		View view = LayoutInflater.from(context).inflate(R.layout.view_home_member_search, null);
		
		buttonSearch = (MaterialRaisedButton) view.findViewById(R.id.buttonSearch);
		buttonGo = (MaterialRaisedButton) view.findViewById(R.id.buttonGo);
		country = (TextView) view.findViewById(R.id.selectCountry);
		withPhoto = (CheckBox) view.findViewById(R.id.withPhoto);
		textViewMin = (TextView) view.findViewById(R.id.textViewMin);
		textViewMax = (TextView) view.findViewById(R.id.textViewMax);
		layoutAge = (LinearLayout) view.findViewById(R.id.layoutAge);
		editTextId = (MaterialEditText) view.findViewById(R.id.idTextInputLayout);
		
		buttonSearch.setOnClickListener(this);
		buttonGo.setOnClickListener(this);
		country.setOnClickListener(this);
		
		rangeSeekBar = new RangeSeekBar<Integer>(
				18, 
				99, 
				context, 
				context.getResources().getColor(R.color.green)
				);
		rangeSeekBar.setLayoutParams(
				new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT
						)
				);
		
		
		rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                    // handle changed range values
            	miMin = minValue;
            	miMax = maxValue;
            	textViewMin.setText(String.valueOf(minValue));
            	textViewMax.setText(String.valueOf(maxValue));
            }
		});
		layoutAge.addView(rangeSeekBar);
		
		
		return view;
	}
	
	public void setCallback(Callback callback){
		this.callback = callback;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.buttonSearch:
			clickSearch(v);
			break;
		case R.id.buttonGo:
			clickGo(v);
			break;
		case R.id.selectCountry:
			clickSelectCountry();
			break;
		}
	}
	
	private void clickSelectCountry(){
		
		if (countryChooser != null){
			countryChooser.show();
			return;
		}
		
		final String[] array = new String[Country.values().length];
		for(int i=0; i< array.length; i++){
			array[i] = Country.values()[i].name();
		}
		countryChooser = new MaterialDialogSingleChoice(
				context, 
				array,
				new MaterialDialogSingleChoice.OnClickCallback() {
					
					@Override
					public void onClick(AdapterView<?> adptView, View v, int which) {
						// TODO Auto-generated method stub
						selectedCountry = which;
						country.setText(array[which]);
					}
				}, -1);
		
		countryChooser.setTitle(context.getString(R.string.country));
		countryChooser.setCanceledOnTouchOutside(true);
		countryChooser.show();
		
	}
	
	private void clickSearch(View v){
		dismiss();
		if( callback != null ) {
			callback.OnClickSearch(v, selectedCountry, miMin, miMax, withPhoto.isChecked());
		}
	}
	
	private void clickGo(View v){
		String ladyId = editTextId.getEditText().getText().toString();
		if (ladyId.length() == 0){
			context.shakeView(editTextId,true);
			editTextId.setError(context.getString(R.string.please_enter_member_id));
			editTextId.setErrorEnabled(true);
			return;
		}
		
		dismiss();
		if( callback != null ) {
			callback.OnClickGo(v, ladyId);
		}
	}
	
	public void dimiss(){
		super.dismiss();
	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub
		editTextId.setErrorEnabled(false);
	}
	
	
	
	
	

	

}
