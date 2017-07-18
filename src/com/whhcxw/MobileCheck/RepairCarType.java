package com.whhcxw.MobileCheck;

import java.util.ArrayList;

import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.CarTypeAdapter;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RepairCarType extends BaseActivity {

	private Context mContext;
	
	private ArrayList<ContentValues> mCartype_data;
	
	private ListView mRepaircartype_list;
	
	private View mRepaircartype_layout;
	
	public static final int CARTYPE = 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_repaircartype);
		mContext = this;
		
		initTitle();

	}

	public void initTitle() {
		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		
		titlebar.setCenterText(R.string.app_name);
		
		initView();
	}

	@SuppressWarnings("unchecked")
	public void initView() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		mCartype_data = (ArrayList<ContentValues>) intent.getSerializableExtra("cartypedata");
		
		
		mRepaircartype_list = (ListView)this.findViewById(R.id.repaircartype_list);
		mRepaircartype_list.setOnItemClickListener(mListViewOnItemClickListener);
		mRepaircartype_layout = (View)this.findViewById(R.id.repaircartype_layout);
		
		if(mCartype_data.size() == 0){
			initCarTypeData();
		}
		
		
		CarTypeAdapter carTypeAdapter = new CarTypeAdapter(mContext, mCartype_data, R.layout.cartype_list);
		mRepaircartype_list.setAdapter(carTypeAdapter);
		
	}
	
	/**初始化品牌数据*/
	public void initCarTypeData(){
		
		mCartype_data = DBOperator.getCarTypes();
		ContentValues values;
		for(int i=0;i<mCartype_data.size();i++){
			values = mCartype_data.get(i);
			values.put("check", false);
		}
		
		
		if(mCartype_data.size() == 0){
			mRepaircartype_layout.setVisibility(View.VISIBLE);
		}
	}
	
	
	OnItemClickListener mListViewOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			for(ContentValues values : mCartype_data){
				values.put("check", false);
			}
			
			ContentValues contentValues = mCartype_data.get((int)arg3);
			contentValues.put("check", true);
			String cartype = contentValues.getAsString(DBModle.CarType.TypeName);
			Intent intent = new Intent();
			intent.putExtra("cartypedata", mCartype_data);
			intent.putExtra("cartype", contentValues);			
			setResult(CARTYPE, intent);
			
			finish();
		}
	};

}
