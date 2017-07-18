package com.whhcxw.MobileCheck;

import com.whhcxw.MobileCheck.data.SQLiteManager;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.service.NetService;
import com.whhcxw.androidcamera.NetCameraService;
import com.whhcxw.crashreport.CrashHandler;
import com.whhcxw.global.G;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.pushservice.PushMessageReceiver;
import com.whhcxw.updateapp.UpdateAppDialog;
import com.whhcxw.utils.AlarmColock;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MobileCheckApplocation extends Application {
	public static int activityInitFlag=0;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initApp();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}


	private void initApp(){		
		//初始化网络参数
		HttpParams.init(getApplicationContext());
		//初始化数据库对象
		SQLiteManager.initContext(getApplicationContext());
		
		UserManager.getInstance().init(getApplicationContext());
		
		/** 初始化API key*/
		PushMessageReceiver.init(getApplicationContext());
		
		//启动定位服务
		BaiduLocation baiduLocation = BaiduLocation.instance();
		baiduLocation.init(getApplicationContext(), baiduLocation.getDefaultLocationClientOption());
		
		//启动抓异常包类
		CrashHandler.getInstance().init(getApplicationContext(),getPackageName());
		
		//设置闹钟，定时发送错误日志
		AlarmColock colock = AlarmColock.getInstance(this);
		colock.destroyAlarm();
		colock.setAlarmColock(getString(R.string.senderror_date),true);	
		
	}
	

	
	/** 注销程序 */
	public void logout(){
		//停止百度服务
		BaiduLocation.instance().stop();
		//停止程序服务
		Intent netServiceIntent = new Intent(getApplicationContext(),NetService.class);
		getApplicationContext().stopService(netServiceIntent);
		//停止音视频服务
		Intent CameraServiceIntent = new Intent(getApplicationContext(),NetCameraService.class);
		getApplicationContext().stopService(CameraServiceIntent);
	}
	
	public void exit(){
		//清除已提示过标记
		UpdateAppDialog.cleanHint();
		logout();
	}
	
    
}
