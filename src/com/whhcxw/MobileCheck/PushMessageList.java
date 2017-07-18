package com.whhcxw.MobileCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.CaseDetails2.AsynInitView;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.adapter.OrganizationAdapter;
import com.whhcxw.adapter.PushMessageAdapter;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.content.ContentValues;
import android.content.Context;

public class PushMessageList extends BaseActivity {

	private final String TAG = this.getClass().getName();

	private Context mContext;
	private ListView mPushmessage_listView;
	private LinearLayout mPushmessage_layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pushmessage);
		mContext = this;
		initTitle();
		initView();
	}


	public void initTitle() {
		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		String mUserClass = UserManager.getInstance().getUserClass();
		if(mUserClass.equals(Main.SURVEY)){
			titlebar.setCenterText(R.string.app_name);
		}else if(mUserClass.equals(Main.SUPERVISOR)){
			titlebar.setCenterText(R.string.app_name_large);
		}else if(mUserClass.equals(Main.DANGER)){
			titlebar.setCenterText(R.string.app_name_danger);
		}else if(mUserClass.equals(Main.RECEPTIONIST)){
			titlebar.setCenterText(R.string.app_name_recommend);
		}else if(mUserClass.equals(Main.OVERHAUL)){
			titlebar.setCenterText(R.string.app_name_overhaul);
		}
	}

	private void initView(){
		mPushmessage_listView = (ListView) findViewById(R.id.pushmessage_list);
		mPushmessage_listView.setOnItemClickListener(PushmessageListViewOnItemClickListener);
		mPushmessage_layout = (LinearLayout)findViewById(R.id.pushmessage_layout);
		
		AsynInitView asynInitView = new AsynInitView();
		asynInitView.execute();
	}
	
	OnItemClickListener PushmessageListViewOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			ContentValues values = (ContentValues) arg0.getAdapter().getItem(arg2);
			Dialog.negativeDialog(mContext, values.getAsString(DBModle.PushMessage.Alert));
		}
	};
	
	/**
	 * 异步加载数据
	 * @author Administrator
	 *
	 */
	class AsynInitView extends AsyncTask<String, Void, ArrayList<ContentValues>>{
		@Override
		protected ArrayList<ContentValues> doInBackground(String... params) {
			// TODO Auto-generated method stub
			ArrayList<ContentValues> arrayList = DBOperator.getPushMessage(UserManager.getInstance().getUserName());
			return arrayList;
		}
		
		@Override
		protected void onPostExecute(ArrayList<ContentValues> result) {
			// TODO Auto-generated method stub
			if(result.isEmpty()){
				mPushmessage_layout.setVisibility(View.VISIBLE);
			}else{
				PushMessageAdapter carTypeAdapter = new PushMessageAdapter(mContext, result, R.layout.pushmessage_list);
				mPushmessage_listView.setAdapter(carTypeAdapter);
			}
			super.onPostExecute(result);
		}
	}
}
