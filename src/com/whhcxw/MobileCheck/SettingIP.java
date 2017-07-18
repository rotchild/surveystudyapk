package com.whhcxw.MobileCheck;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.updateapp.UpdateApp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingIP extends BaseActivity {

	private final String TAG = this.getClass().getName(); 
	
	private Context mContext;
	
	private EditText mIP_EditText;
	private EditText mFPS_EditText;
	private EditText mBIT_EditText;
	private Spinner mPwPh_Spinner;
	private String mPwPh_Str = "352x288";
	private int mPwPhSelect_int = 0;
	private ArrayAdapter<?> spinnerAdapter;
	
	private boolean IPEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent == null){
			if (MobileCheckApplocation.activityInitFlag==0) finish();
		}else {
			IPEdit = intent.getBooleanExtra("IPEdit", false);
		}
		
		setContentView(R.layout.settingip);
		mContext = this;
		initTitle();
	}
	
	public void initTitle(){
		initView();
		
		Titlebar2 titlebar = (Titlebar2)findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		titlebar.setCenterText(R.string.app_settingsystem);
		titlebar.showRight(mIPOnClickListener);
		titlebar.setRightText(R.string.settingip_save);
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		String preferencesIP = UserManager.getInstance().getIP();
		
		 mIP_EditText = (EditText)this.findViewById(R.id.ip);
		 if(preferencesIP.equals("")){
			 preferencesIP = HttpParams.BSASURL;
		 }
		 mIP_EditText.setText(preferencesIP.substring(7, preferencesIP.length()-1));
		 if(!IPEdit){
			 mIP_EditText.setFocusable(false);
		 }
		 
		 
		 String FPS = UserManager.getInstance().getFPS();
		 if(FPS.equals("")){
			 FPS = getString(R.string.setting_defaultFPS);
		 }
		 mFPS_EditText = (EditText)this.findViewById(R.id.FPS);
		 mFPS_EditText.setText(FPS);
		 
		 String BIT = UserManager.getInstance().getBIT();
		 if(BIT.equals("")){
			 BIT = getString(R.string.setting_defaultBIT);
		 }
		 mBIT_EditText = (EditText)this.findViewById(R.id.BIT);
		 mBIT_EditText.setText(BIT);
		 
//		 String pw_ph = UserManager.getInstance().getPw_ph();
//		 if(pw_ph.equals("")){
//			 pw_ph = UserManager.getInstance().getPw_ph();
//		 }
		 
		 int pwphSelect= UserManager.getInstance().getPwPhSelect();
		 if(pwphSelect != 0){
			 mPwPhSelect_int = pwphSelect;
		 }
		 
		 mPwPh_Spinner = (Spinner)this.findViewById(R.id.pw_ph);
		 
		//将可选内容与ArrayAdapter连接起来  
		 spinnerAdapter =ArrayAdapter.createFromResource(mContext,R.array.pref_camera_previewsize_entryvalues , android.R.layout.simple_spinner_item);
		 //设置下拉列表的风格    
		 spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 mPwPh_Spinner.setAdapter(spinnerAdapter);
		 mPwPh_Spinner.setSelection(mPwPhSelect_int, true);
		 mPwPh_Spinner.setOnItemSelectedListener(mPwPh_SpinnerOnItemSelectedListener);
		 
	}
	
	OnItemSelectedListener mPwPh_SpinnerOnItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
			// TODO Auto-generated method stub
			mPwPh_Str = spinnerAdapter.getItem(arg2).toString();
			mPwPhSelect_int = arg2;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};

	/** ip保存 */
	OnClickListener mIPOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String ip_Str =mIP_EditText.getText().toString();
			String FPS_Str = mFPS_EditText.getText().toString();
			String BIT_Str = mBIT_EditText.getText().toString();
			String PwPh_Str = mPwPh_Spinner.getSelectedItem().toString();
			
			if(ip_Str.equals("") || FPS_Str.equals("") || BIT_Str.equals("")){
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.settingip_nomes));
				return;
			}
			boolean saveIP_boolean = HttpParams.saveIP("http://"+ip_Str+"/", mContext);
			if(saveIP_boolean){
				HttpParams.init(mContext);
				boolean b = UpdateApp.clearPreferences(mContext);
				Log.d(TAG, " clearPreferences is success:"+b);
			}
			
			boolean b =  UserManager.getInstance().saveSettingSystem(mContext, FPS_Str, BIT_Str, PwPh_Str,mPwPhSelect_int);
			if(b){
				Log.d(TAG, "saveSettingSystem success");
			}
			finish();
		}
	};
	
}
