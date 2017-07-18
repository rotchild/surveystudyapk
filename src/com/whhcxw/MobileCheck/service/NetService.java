package com.whhcxw.MobileCheck.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.whhcxw.MobileCheck.Dialog;
import com.whhcxw.MobileCheck.Login;
import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.MobileCheckApplocation;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.global.G;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.pushservice.PushMessageReceiver;
import com.whhcxw.utils.AppManager;
import com.whhcxw.utils.CatchException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class NetService extends Service {

	private final String TAG = this.getClass().getSimpleName();
	private Context mContext;
	private UploadWork uploadWork;
	private SnycService mSnycService;
	private WakeLock wakeLock;
	
	
	
	private SendGpsLogService mGpsLogService;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("NetService  onCreate() ", G.getPhoneCurrentTime());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CatchException.saveException(this, jsonObject);
		
		Log.d(TAG, "NetService oncreate");
		PowerManager pm = (PowerManager)getSystemService(this.getApplicationContext().POWER_SERVICE);  
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NetServiceWakeLock");
		wakeLock.acquire();
		
		if (uploadWork==null){
			uploadWork = new UploadWork(this);		
			uploadWork.start();	
		}
	if (mSnycService==null){
			String accessToken = UserManager.getInstance().getAccessToken();
			mSnycService = new SnycService(this,accessToken);
			mSnycService.start();
		}
		if (mGpsLogService==null){
			mGpsLogService = new SendGpsLogService(this);
			mGpsLogService.start();
		}
		
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("NetService  onStartCommand() ", G.getPhoneCurrentTime());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CatchException.saveException(this, jsonObject);

		
		
		
		
		//后台服务检测用户登录
		if(UserManager.getInstance().isLogin()){
			DataHandler dataHandler = new DataHandler(this);
			dataHandler.setIsShowProgressDialog(false);
			dataHandler.UserCheck(UserManager.getInstance().getUserName(), UserManager.getInstance().getPassWord(), "", mUserCheckResponse);
		}
			
		//后台服务，添加应用程序状态图标
		showNotification();
		
		return START_REDELIVER_INTENT;
	}
	
	
	/** 程序在后台运行时，在通知栏上显示图标 */
	private void showNotification() {
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
		if (UserManager.getInstance().isLogin()){
			 notificationIntent = new Intent(mContext, Main.class); // 点击该通知后要跳转的Activity			 
		}else {
			notificationIntent = new Intent(mContext, Login.class);
		}
		PendingIntent contentItent = PendingIntent.getActivity(mContext, 0,notificationIntent, 0);
		notification.setLatestEventInfo(mContext, contentTitle, contentText,contentItent);
		// 把Notification传递给NotificationManager
		startForeground(123, notification);
	}

	/** 取消图标 */
	private void cancelNotification() {
		// 启动后删除之前我们定义的通知
		stopForeground(true);
	}
	

	
	/**
	 * 用户验证回调函数
	 * */
	HttpResponseHandler mUserCheckResponse = new HttpResponseHandler() {
		@Override
		public void response(boolean success, String response, Throwable error) {
				if (success) {
					try {
						JSONObject jsonObject = new JSONObject(response);
						String code = jsonObject.getString("code");
						if (code.equals("0")) {//登录失败    登录成功 无操作
							String mUserClass = jsonObject.getJSONObject("value").getString("UserClass");
							 if(mUserClass.equals("-1")){
								 loginFail();
							 }
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}else{
				//登录失败
				
				
			}
		}
	};
	
	
	
	//登录失败后，的操作
	public void loginFail(){
		UserManager userManager = UserManager.getInstance();
		String UserName = userManager.getUserName();
		String AccessToken = userManager.getAccessToken();
		String PushID = PushManager.getBindID(this);
				
		DataHandler dataHandler = new DataHandler(this);
		dataHandler.setIsShowProgressDialog(false);		
		dataHandler.UnBindPushUser(UserName, PushID, AccessToken, new HttpResponseHandler(){
			public void response(boolean success,String response,Throwable error){
				String result = "unBind User failure";
				if(success){
					try {
						String code = new JSONObject(response).getString("code");
						if(code.equals("0")){
							result = "unBind User success";
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = "unBind User success server error parse json error json:" + response;
					}						
				}
				Log.d(TAG, result);
				
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancel(PushMessageReceiver.newmessage);
				
				UserManager.getInstance().userIsLogin(false);					
				//解除绑定成功，清空推送绑定信息
				boolean b = PushManager.clearPushPreferences(NetService.this);
				if(b){
					Log.d(TAG, "PushManager.clearPushPreferences  success!!!!");
				}else{
					Log.d(TAG, "PushManager.clearPushPreferences  fail!!!!");
				}
				
				// 启动后删除之前我们定义的通知
				NotificationManager notificationManager2 = (NotificationManager) NetService.this.getSystemService(NOTIFICATION_SERVICE);
				notificationManager2.cancel(0);
			
				UserManager.getInstance().userIsLogin(false);
				} 	
		});
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("NetService  onDestroy() ", G.getPhoneCurrentTime());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CatchException.saveException(this, jsonObject);
		
		if(uploadWork != null){
			uploadWork.stop();
			uploadWork=null;
		}
		if(mSnycService!= null){
//			mSnycService.stop();
			mSnycService.mHandler.getLooper().quit();
			mSnycService=null;
//			mSnycService.interrupt();
		}
		
		if(mGpsLogService!=null){
			mGpsLogService.stop();
			mGpsLogService=null;
		}
		wakeLock.release();
		super.onDestroy();
		
		//程序服务退出时，取消图标显示
		cancelNotification();
		
	}
	
}
