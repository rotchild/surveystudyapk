package com.whhcxw.MobileCheck;

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
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

public class ReportCopy extends BaseActivity {

	private final String TAG = this.getClass().getName();
	private Context mContext;
	private Titlebar2 titlebar;
	private ListView mListView;
	private List<Map<String, Object>> dataList;
	private AutoCompleteTextView mSearch_Edit;
	private boolean isTextChangeFinish;
	private Handler mHandler = new Handler();
	
	private ArrayList<Map<String, Object>> mChoiced_List ;
	private CouncilorAapter mAdapter;
	
	private View mCopyno_layout;
	private Button mDeleteText_Button;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.copy);
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
		titlebar.showRight(titleRightOnClickListener);
//		titlebar.setRightBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setRightText(R.string.title_finish);

	}

	public void initView() {
		// TODO Auto-generated method stub
		mChoiced_List = new ArrayList<Map<String,Object>>();
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("bundle");
		
		mListView = (ListView) this.findViewById(R.id.councilor_list);
		mListView.setOnItemClickListener(mListViewOnItemClickListener);
		
		mSearch_Edit = (AutoCompleteTextView)this.findViewById(R.id.autoCompleteTextView);
		mSearch_Edit.addTextChangedListener(mSearchEditTextWatcher);
		
		mCopyno_layout = (View)this.findViewById(R.id.copyno_layout);
		
		
		mDeleteText_Button = (Button)this.findViewById(R.id.deletetext);
		mDeleteText_Button.setOnClickListener(mDeletetextButtonOnClickListener);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) bundle.getSerializable("datalist");
		if(list!=null){
			dataList = list;
		}else {
			
			String name = UserManager.getInstance().getUserName();
			
			ArrayList<ContentValues> arrayList =  DBOperator.getUsers();
			
			ContentValues contentValues;
			Map<String, Object> map;
			dataList = new ArrayList<Map<String, Object>>();
			for(int i = 0;i<arrayList.size();i++){
				map = new HashMap<String, Object>();
				contentValues = arrayList.get(i);
				String username = contentValues.getAsString(DBModle.Users.UserName);
				if(username.equals(name)){
					continue;
				}
				map.put(DBModle.Users.UserName,username);
				map.put(DBModle.Users.RealName, contentValues.get(DBModle.Users.RealName).toString());
				map.put(DBModle.Users.Email, contentValues.get(DBModle.Users.Email).toString());
				map.put("check", false);
				
				
				dataList.add(map);
			}
		}
		mAdapter = new CouncilorAapter(mContext, dataList,R.layout.copy_list);
		mListView.setAdapter(mAdapter);
		
		if(dataList.size()==0){
			mCopyno_layout.setVisibility(View.VISIBLE);
		}else {
			mCopyno_layout.setVisibility(View.GONE);
		}
	}

	/**清空搜索框中的内容*/
	OnClickListener mDeletetextButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mSearch_Edit.setText("");
		}
	};
	
	OnItemClickListener mListViewOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			CouncilorAapter councilorAapter = (CouncilorAapter) arg0.getAdapter();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) councilorAapter.getItem(arg2);
			if((Boolean) map.get("check")){
				map.put("check", false);
				for(int i = 0;i<mChoiced_List.size();i++){
					if(map.toString().equals(mChoiced_List.get(i).toString())){
						mChoiced_List.remove(i);
					}
				}
				
			}else {
				map.put("check", true);
				mChoiced_List.add(map);
			}
			councilorAapter.notifyDataSetChanged();
		}
	};

	/** titlebar完成 */
	OnClickListener titleRightOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Boolean b;
			ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < dataList.size(); i++) {
				b = (Boolean) dataList.get(i).get("check");
				if (b) {
					Map<String, Object> map = dataList.get(i);
					list.add(map);
					
				}
			}
			
			ArrayList<Map<String, Object>> list2 = dataSort(list);

			if (list.size() != 0) {
				Bundle bundle = new Bundle();
				bundle.putSerializable("datalist", list2);
				Intent intent = new Intent();
				intent.putExtra("bundle",bundle);
				setResult(Report.COPY,intent);
				finish();
			}else {
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.current_layout_list_mess));
			}
		}
	};
	
	
	/**数据重新排序*/
	public ArrayList<Map<String, Object>> dataSort(ArrayList<Map<String, Object>> list){
		
		ArrayList<Map<String, Object>> list2 = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < list.size(); i++) {
			String selUserName = list.get(i).get(DBModle.Users.UserName).toString();
			for(int j = 0; j < dataList.size(); j++){
				String userName = list.get(i).get(DBModle.Users.UserName).toString();
				if(selUserName.equals(userName)){
					list2.add(list.get(i));
					break;
				}
			}
		}
		for (int i = 0; i < list2.size(); i++) {
			dataList.remove(list2.get(i));			
			dataList.add(0, list2.get(i));
		}
		return (ArrayList<Map<String, Object>>) dataList;
	}
	
	TextWatcher mSearchEditTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			// TODO Auto-generated method stub
			isTextChangeFinish = false;
			mHandler.removeCallbacks(updateListRunnable);
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			isTextChangeFinish = true;
			// 延时500毫秒开始索引
			mHandler.postDelayed(updateListRunnable, 500);
		}
	};
	
	
	Runnable updateListRunnable = new Runnable() {
		public void run() {
			if (isTextChangeFinish) {
				String key = mSearch_Edit.getText().toString().trim();
				if (key.equals("")) {
					mAdapter.updata(dataList);
				} else {
					ArrayList<Map<String, Object>> searchList = new ArrayList<Map<String,Object>>();
					for (Map<String, Object> map: dataList) {
						String username = map.get(DBModle.Users.RealName).toString();
						if (username.contains(key)) {
							searchList.add(map);
							continue;
						}
					}
					ArrayList<Map<String, Object>> choiceSearchList = new ArrayList<Map<String,Object>>();
					for(Map<String, Object> map2 : mChoiced_List){
						choiceSearchList.add(map2);
					}
					for(Map<String, Object> map3 : searchList){
						choiceSearchList.add(map3);
					}
					
					mAdapter.updata(choiceSearchList);
				}
				Log.d(TAG, "isTextChangeFinish:" + key);
			}
		}
	};

}
