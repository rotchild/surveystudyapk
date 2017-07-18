package com.whhcxw.MobileCheck;

import java.util.ArrayList;

import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.GaragesAdapter;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RepairGarages extends BaseActivity {

	private Context mContext;
	
	private ArrayList<ContentValues> mGarages_data;
	
	private ListView mRepairgarages_list;
	
	private View mRepairgarages_layout;
	private String mCartypeID;
	
	public static final int GARAGES = 1001;
	
	private boolean isCooperation ;
	
	private int mIsEnterprisechoices;
	private int mIsCategorychoices;
	private int mAreaid;
	
	private boolean mButtonCheck = false;
	
//	private Button moreGarages;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_repairgarages);
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
		mGarages_data = (ArrayList<ContentValues>) intent.getSerializableExtra("mGarages_data");
		isCooperation = intent.getBooleanExtra("isCooperation", true);
		
//		moreGarages = (Button)this.findViewById(R.id.button);
//		moreGarages.setOnClickListener(moreGaragesOnClickListener);
		
		
		if(isCooperation){
			mIsEnterprisechoices = intent.getIntExtra("isEnterprisechoices",-1);
			mIsCategorychoices= intent.getIntExtra("isCategorychoices",-1);
			mAreaid = intent.getIntExtra("areaid",-1);
//			moreGarages.setVisibility(View.VISIBLE);
			
		}else {
			
			mAreaid = intent.getIntExtra("areaid",-1);
			mCartypeID = intent.getStringExtra("cartype");
			
		}
		
		mRepairgarages_list = (ListView)this.findViewById(R.id.repairgarages_list);
		mRepairgarages_list.setOnItemClickListener(mListViewOnItemClickListener);
		mRepairgarages_layout = (View)this.findViewById(R.id.repairgarages_layout);
		
		
		
		if(mGarages_data.size() == 0){
			initGaragesData();
		}
		
		
		GaragesAdapter carTypeAdapter = new GaragesAdapter(mContext, mGarages_data, R.layout.garages_list);
		mRepairgarages_list.setAdapter(carTypeAdapter);
		
	}
	
	OnClickListener moreGaragesOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			moreGarages.setEnabled(false);
			mButtonCheck = true;
			initGaragesData();
			GaragesAdapter carTypeAdapter = new GaragesAdapter(mContext, mGarages_data, R.layout.garages_list);
			mRepairgarages_list.setAdapter(carTypeAdapter);
		}
	};
	
	
	/**初始化修理厂数据*/
	public void initGaragesData(){
		if(isCooperation){
//				if(mButtonCheck){
					mGarages_data = DBOperator.getAllCooperationGarages(mAreaid,mIsCategorychoices,mIsEnterprisechoices);
//				}else {
//					mGarages_data = DBOperator.getCooperationGarages(mAreaid,mIsCategorychoices,mIsEnterprisechoices);
//				}
			
			}else {
				mGarages_data = DBOperator.getGarages(mAreaid,mCartypeID);
			}
		ContentValues values;
		for(int i=0;i<mGarages_data.size();i++){
			values = mGarages_data.get(i);
			values.put("check", false);
		}
		
		if(mGarages_data.size() == 0){
			mRepairgarages_layout.setVisibility(View.VISIBLE);
		}
	}
	
	
	OnItemClickListener mListViewOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			for(ContentValues values : mGarages_data){
				values.put("check", false);
			}
			ContentValues contentValues = mGarages_data.get((int)arg3);
			contentValues.put("check", true);
			Intent intent = new Intent();
			intent.putExtra("garagesdata", mGarages_data);
			intent.putExtra("garages", contentValues);
			setResult(GARAGES, intent);
			
			finish();
		}
	};

}
