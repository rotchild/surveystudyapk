package com.whhcxw.MobileCheck;

import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.global.G;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.updateapp.UpdateApp;
import com.whhcxw.updateapp.UpdateAppDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SystemCode extends BaseActivity {

	private TextView mCode_View;
	private Context mContext;

	private Button mButton;
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.systemcode);
		mContext = this;
		initTitle();
	}

	public void initTitle() {
		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		String mUserClass = UserManager.getInstance().getUserName();
		
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
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		showProgressDialog();
		mCode_View = (TextView) this.findViewById(R.id.code);
		mCode_View.setText(getVersionName(mContext));
		
		mButton = (Button)this.findViewById(R.id.button);
		mButton.setText(getString(R.string.systemcode_current_code) + getVersionName(mContext));
		
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				UpdateAppDialog.cleanHint();
//				UpdateAppDialog.showUpdateAppDialog(SystemCode.this, G.UPDATE_APP_SAVE_PATH,R.drawable.logo);
				mProgressDialog.show();
				UpdateApp.clearPreferences(mContext);
				UpdateApp updateApp = new UpdateApp(mContext, HttpParams.GETVERSION, updateAPPHttpResponseHandler);
				updateApp.start();	
			}
		});
//		updateApp();
	}
	
	/** 检测APP 版本信息回调函数 */
	HttpResponseHandler updateAPPHttpResponseHandler = new HttpResponseHandler(){
		@Override
		public void response(boolean success, String response,Throwable error) {
			// TODO Auto-generated method stub
			super.response(success, response, error);
				//弹出升级APP 对话框
				mProgressDialog.dismiss();
				if(success){
					UpdateAppDialog.cleanHint();
					boolean b = UpdateAppDialog.showUpdateAppDialog(SystemCode.this,G.UPDATE_APP_SAVE_PATH,R.drawable.logo_code_orange);
					if(!b){
						mButton.setText(getString(R.string.systemcode_laster_code));
					}
				}else{
					G.showToast(mContext, getString(R.string.systemcode_weberror), true);
				}
					
		}			
	};

//	private void updateApp(){
//		SharedPreferences preferences = UpdateApp.getAppVersionPreferences(mContext);
//		String updateStage = preferences.getString("updateStage", UpdateApp.UPDATE_STAGE_NOTHINT);
//		if(updateStage.equals("3")){
//			mButton.setEnabled(false);
//		}else{
//			if(updateStage.equals("1")){
//				Editor editor = preferences.edit();
//				editor.putBoolean("isHint", true);
//				editor.commit();
//			}
//		} 
//	}
	
	public static String getVersionName(Context context) {
		String version = "";
		try {
			// 获取packagemanager的实例
			PackageManager packageManager = context.getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo;

			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}

	
	public void showProgressDialog(){
		mProgressDialog = new ProgressDialog(mContext);		
		String loading = mContext.getResources().getString(R.string.request_loading);
		mProgressDialog.setMessage(loading);
		mProgressDialog.setCanceledOnTouchOutside(false);
	}
}
