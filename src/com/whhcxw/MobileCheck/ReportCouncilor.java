package com.whhcxw.MobileCheck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.CouncilorAapter;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ReportCouncilor extends BaseActivity {

	private Context mContext;
	private Titlebar2 titlebar;
	private ListView mListView;

	private List<Map<String, Object>> dataList;

	private View mCouncilorno_layout;
	
	private String mLargeOrDanger;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.councilor);
		mContext = this;

		initTitle();

		initView();
	}

	public void initTitle() {
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);
		/*titlebar.showRight(titleRightOnClickListener);
		titlebar.setRightBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setRightText(R.string.title_finish);*/

	}

	public void initView() {
		// TODO Auto-generated method stub
		mListView = (ListView) this.findViewById(R.id.councilor_list);
		mListView.setOnItemClickListener(mListViewOnItemClickListener);
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("bundle");
		mLargeOrDanger = bundle.getString("LargeOrDanger");
		mCouncilorno_layout = (View)this.findViewById(R.id.councilorno_layout);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) bundle.getSerializable("datalist");
		if(list!=null){
			dataList = list;
		}else {
			
			String AreaID = UserManager.getInstance().getAreaID();
			ArrayList<ContentValues> arrayList;
			if(mLargeOrDanger.equals(CaseDetails.LARGE)){
				arrayList =  DBOperator.getUser(AreaID,"6");
			}else {
				arrayList =  DBOperator.getUser(AreaID,"8");
			}
			
			
			ContentValues contentValues;
			Map<String, Object> map;
			dataList = new ArrayList<Map<String, Object>>();
			for(int i = 0;i<arrayList.size();i++){
				map = new HashMap<String, Object>();
				contentValues = arrayList.get(i);
				map.put("UserName", contentValues.get(DBModle.Users.UserName).toString());
				map.put("RealName", contentValues.get(DBModle.Users.RealName).toString());
				map.put("Email", contentValues.getAsString(DBModle.Users.Email));
				map.put("check", false);
				dataList.add(map);
			}
			
		}
		CouncilorAapter adapter = new CouncilorAapter(mContext, dataList,R.layout.councilor_list);
		mListView.setAdapter(adapter);

		if(dataList.size() == 0){
			mCouncilorno_layout.setVisibility(View.VISIBLE);
		}else {
			mCouncilorno_layout.setVisibility(View.GONE);
		}
		
	}
	
	
	
	OnItemClickListener mListViewOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			CouncilorAapter councilorAapter = (CouncilorAapter) arg0.getAdapter();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) councilorAapter.getItem(arg2);
			
			if((Boolean) map.get("check")){
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.report_needcouncilor));
				return;
			}else {
				for(int i = 0 ;i<dataList.size();i++){
					if((Boolean) dataList.get(i).get("check")){
						dataList.get(i).put("check", false);
					}
					
					if(map.toString().equals(dataList.get(i).toString())){
						dataList.remove(i);
					}
				}
				
				map.put("check", true);
				dataList.add(0, map);
			}
			
			Bundle bundle = new Bundle();
			bundle.putSerializable("datalist", (Serializable) dataList);
			Intent intent = new Intent();
			intent.putExtra("bundle",bundle);
			setResult(Report.COUNCILOR,intent);
			finish();
		}
	};


	
}
