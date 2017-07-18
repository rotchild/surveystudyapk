package com.whhcxw.theme;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.Login;
import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.MobileCheckApplocation;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.utils.AppManager;

public class BaseActivity extends StatActivity {

	private final String TAG = this.getClass().getName();

	private Context mContext;
	public int mTheme = R.style.app_skin_default;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		if (savedInstanceState == null) {
			mTheme = PreferenceHelper.getTheme(this);
		} else {
			mTheme = savedInstanceState.getInt("theme");
		}
		setTheme(mTheme);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		//添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTheme != PreferenceHelper.getTheme(this)) {
			reload();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		/*if(!isBackground(mContext)){
			cancelNotification();
		}*/
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		/*if(isBackground(mContext)){
			showNotification();
		}*/
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}

	/**判断程序是在在前台运行*/
	public boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
					Log.i(TAG, String.format("Background App:",appProcess.processName));
					return true;
				} else { 
					Log.i(TAG, String.format("Foreground App:",appProcess.processName));
					return false;
				}
			}
		}
		return false;
	}

	/** 程序在后台运行时，在通知栏上显示图标 *//*
	private void showNotification() {
		// 创建一个NotificationManager的引用
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		// 定义Notification的各种属性
		Notification notification = new Notification(R.drawable.logo_code_orange,getString(R.string.app_name), System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
		notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.ledARGB = Color.BLUE;
		notification.ledOnMS = 5000;
		// 设置通知的事件消息
		CharSequence contentTitle = getString(R.string.app_name); // 通知栏标题
		CharSequence contentText = getString(R.string.app_name)+getString(R.string.app_run); // 通知栏内容
		Intent notificationIntent;
		if (MobileCheckApplocation.activityInitFlag==0){
			 notificationIntent = new Intent(mContext, Login.class);
		}else {
			 notificationIntent = new Intent(mContext, Main.class); // 点击该通知后要跳转的Activity			 
		}
		PendingIntent contentItent = PendingIntent.getActivity(mContext, 0,notificationIntent, 0);
		notification.setLatestEventInfo(mContext, contentTitle, contentText,contentItent);
		// 把Notification传递给NotificationManager
		notificationManager.notify(0, notification);
	}

	*//** 取消图标 *//*
	private void cancelNotification() {
		// 启动后删除之前我们定义的通知
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}*/

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("theme", mTheme);
	}

	protected void reload() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}
}
