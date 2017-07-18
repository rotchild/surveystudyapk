package com.whhcxw.MobileCheck;


import com.baidu.android.pushservice.PushManager;
import com.whhcxw.MobileCheck.data.AppUpdateInitialize;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.updateapp.UpdateApp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Welcome extends BaseActivity {

	private Context mContext;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		mContext = this;
		
		//启动程序初始化数据升级动作
		new AppUpdateInitialize(mContext).init();
		
		//检测玩APP升级后执行跳转页面
		UpdateApp updateApp = new UpdateApp(this, HttpParams.GETVERSION, updateAPPHttpResponseHandler);
		updateApp.start();		
	}
	
	/** 检测APP 版本信息回调函数 */
	HttpResponseHandler updateAPPHttpResponseHandler = new HttpResponseHandler(){
		@Override
		public void response(boolean success, String response,Throwable error) {
			// TODO Auto-generated method stub
			super.response(success, response, error);
			jump();
		}			
	};
	//判断跳转到哪个页面
	private void jump(){

		if(UserManager.getInstance().isLogin()){
			Intent intent = new Intent(mContext, Main.class);
			startActivity(intent);
		}else{
			Intent intent = new Intent(mContext,Login.class);
			startActivity(intent);
		}
		finish();
	}
	
	
	@Override
	protected void onStart() {
		PushManager.activityStarted(this);
		super.onStart();
	}
	
	@Override
	public void onStop() {
//		PushManager.activityStoped(this);
		super.onStop();
	}
	
}
