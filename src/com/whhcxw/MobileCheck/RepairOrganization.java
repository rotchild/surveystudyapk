package com.whhcxw.MobileCheck;

import java.util.ArrayList;

import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.OrganizationAdapter;
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

public class RepairOrganization extends BaseActivity {

	private Context mContext;
	
	private ArrayList<ContentValues> mOrganization_data;
	
	private ListView mOrganization_list;
	
	private View mOrganization_layout;
	
	public static final int ORGANIZATION = 1002;
	
	private boolean mIsCooperation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_repairorganization);
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
		mOrganization_data = (ArrayList<ContentValues>) intent.getSerializableExtra("organizationdata");
		
		mIsCooperation = intent.getBooleanExtra("isCooperation", true);
		
		mOrganization_list = (ListView)this.findViewById(R.id.organization_list);
		mOrganization_list.setOnItemClickListener(mListViewOnItemClickListener);
		mOrganization_layout = (View)this.findViewById(R.id.organization_layout);
		
		if(mOrganization_data.size() == 0){
			initOrganizationData();
		}
		
		
		OrganizationAdapter carTypeAdapter = new OrganizationAdapter(mContext, mOrganization_data, R.layout.organization_list);
		mOrganization_list.setAdapter(carTypeAdapter);
		
	}
	
	/**初始化机构数据*/
	public void initOrganizationData(){
		
		mOrganization_data = DBOperator.getAreaList();
		ContentValues values;
		for(int i=0;i<mOrganization_data.size();i++){
			values = mOrganization_data.get(i);
			values.put("check", false);
		}
		
		
		if(mOrganization_data.size() == 0){
			mOrganization_layout.setVisibility(View.VISIBLE);
		}
	}
	
	
	OnItemClickListener mListViewOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			for(ContentValues values : mOrganization_data){
				values.put("check", false);
			}
			
			ContentValues contentValues = mOrganization_data.get((int)arg3);
			contentValues.put("check", true);
			Intent intent = new Intent();
			intent.putExtra("organizationdata", mOrganization_data);
			intent.putExtra("organization", contentValues);
			intent.putExtra("isCooperation", mIsCooperation);
			setResult(ORGANIZATION, intent);
			
			finish();
		}
	};

}
