package com.qpidnetwork.ladydating;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.home.HomeActivity;


public class MainActivity extends BaseFragmentActivity implements OnClickListener{

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
