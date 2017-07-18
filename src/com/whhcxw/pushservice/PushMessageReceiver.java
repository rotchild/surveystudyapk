package com.whhcxw.pushservice;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.sax.StartElementListener;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.whhcxw.MobileCheck.ExitLoginDialog;
import com.whhcxw.MobileCheck.Dialog;
import com.whhcxw.MobileCheck.Login;
import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.MobileCheckApplocation;
import com.whhcxw.MobileCheck.PushMessageList;
import com.whhcxw.MobileCheck.RefuseCaseDialog;
import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.global.G;
import com.whhcxw.utils.AppManager;
import com.whhcxw.utils.CatchException;

/**
 * Push消息处理receiver
 */
public class PushMessageReceiver extends BroadcastReceiver {
	/** TAG to Log */
	public static final String TAG = PushMessageReceiver.class.getSimpleName();
	
	public static String API_KEY = "";

	private static Context mContext;
	AlertDialog.Builder builder;
	
	public static int newmessage=3;
	public static final String INTENT_NEWTASK = "FROM_PUSHSERVICE_TASK";
	
	public static void init(Context ctx){
		API_KEY = ctx.getString(R.string.push_apikey);
		mContext = ctx;
	}
	
	/**
	 * @param context Context
	 * @param intent  接收的intent
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {

		Log.d(TAG, ">>> Receive intent: \r\n" + intent);
		UserManager userManager = UserManager.getInstance();
		if(userManager.isLogin() == false){
			Log.d(TAG, "user not login");
			return;
		}

		if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
			//获取消息内容
			String message = intent.getExtras().getString(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
			//用户自定义内容读取方式，CUSTOM_KEY值和管理界面发送时填写的key对应
			//String customContentString = intent.getExtras().getString(CUSTOM_KEY);
			Log.i(TAG, "onMessage: " + message);
//			G.showToast(context, message, true);
			
			//用户在此自定义处理消息,以下代码为demo界面展示用
			
			/**
			 * message_type定义
			 *0 无特殊意义，发出通知提示。 1 调度任务。2 调度催办。3 定损受理任务。4 异地登陆。5,推送，发送错误日志
			 */
			String alert = " ",sound = " ",sender = " ",receiver = " ",message_type = " ",platform = " ";
			JSONObject data;
			try {
				JSONObject jsonObject = new JSONObject(message);
				JSONObject aps = jsonObject.getJSONObject("aps");
				alert = aps.getString("alert");
				sound = aps.getString("sound");
				sender = aps.getString("sender");
				receiver = aps.getString("receiver");
				message_type = aps.getString("message_type");
				data = aps.getJSONObject("data");
				platform = aps.getString("platform");
				
				//判斷自己是否是接受者，不是的話，说明不是自己的任务，就把它return掉
				if(!receiver.equals(UserManager.getInstance().getUserName())){
					return;
				}
				
				if(DBOperator.addPushMessage(alert, sound, sender, receiver, message_type, data.toString(), platform ,"","","0",userManager.getUserName())){
					
					if(message_type.equals("0")){
						
					}else if(message_type.equals("1")){
						pushCreateTask(context,message,alert);
					} else if(message_type.equals("2")) {
						
					}else if(message_type.equals("3")) {//定损受理任务
						
						String CaseNo = data.getString("CaseNo");
						String TaskType =  data.getString("TaskType");
						ContentValues values = new ContentValues();
						values.put(DBModle.Task.BackState, DBModle.TaskLog.FrontState_START);
						boolean result = DBOperator.updateTask(CaseNo, TaskType, values);
						if(result){
							Intent mIntent = new Intent(Main.NEWTASKTRENDS);
							mIntent.putExtra("yaner", context.getString(R.string.yuandong));
							context.sendBroadcast(mIntent);
							
							NotificationManager manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
				    		Notification notify = new Notification(R.drawable.logo_code_orange,null,System.currentTimeMillis());
				    		notify.sound=Settings.System.DEFAULT_NOTIFICATION_URI;	    		
				    		Intent notifyIntent = new Intent();
				    		notifyIntent.setAction(INTENT_NEWTASK);
				    		notifyIntent.setClass(context, PushMessageList.class);
				    		notifyIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
				    		PendingIntent intent1 = PendingIntent.getActivity(context, 0, notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT );
				    		String title = context.getString(R.string.main_newtrends);
				    		notify.setLatestEventInfo(context, title, alert, intent1);
				    		notify.flags = Notification.FLAG_AUTO_CANCEL; 
				    		manager.notify(newmessage,notify);	
						}
					}else if(message_type.equals("4")) {
						
						Intent dialogIntent = new Intent(context,ExitLoginDialog.class); 
						dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(dialogIntent);
						
					}else if(message_type.equals("5")) {//调错误日志
						boolean b = CatchException.sendExceptionEmail(context);
						if(b){
							CatchException.clearException(context);
						}
					}else if(message_type.equals("6")) {//拒绝任务
						Bundle bundle = new Bundle();
						bundle.putString("alert", alert);
						String caseno = data.getString("CaseNo");
						String tasktype = mContext.getString(R.string.tasktype_survey);
						ContentValues values = DBOperator.getTask(caseno, tasktype);
						bundle.putSerializable("TID", values.getAsString(DBModle.Task.TID));
						Intent dialogIntent = new Intent(context,RefuseCaseDialog.class); 
						dialogIntent.putExtras(bundle);
						dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(dialogIntent);
					}
				}
			
			} catch (Exception e) {
				// TODO: handle exception
				Log.d(TAG,e.toString());
				
			}
			
			//处理绑定等方法的返回数据
			//注:PushManager.startWork()的返回值通过PushConstants.METHOD_BIND得到
		} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
			//获取方法
			final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);
			//方法返回错误码,您需要恰当处理。比如，方法为bind时，若失败，需要重新bind,即重新调用startWork
			final int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE,PushConstants.ERROR_SUCCESS);
			//返回内容
			final String content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
			
			//用户在此自定义处理消息,以下代码为demo界面展示用			
			Log.d(TAG, "onMessage: method : " + method);
			Log.d(TAG, "onMessage: result : " + errorCode);
			Log.d(TAG, "onMessage: content : " + content);
			
			String UserName = userManager.getUserName();
			if(PushConstants.METHOD_BIND.equals(method)){
				if (errorCode == 0){
					//gt-n7100 {"response_params":{"appid":"751739","channel_id":"3980400113410407003","user_id":"1010016440638226015"},"request_id":897150327}
					//me525+  
					//moto 711 
//					Toast.makeText(context,"method : " + method + "\n result: " + errorCode	+ "\n content = " + content, Toast.LENGTH_SHORT).show();					
					String AccessToken = userManager.getAccessToken();
					PushManager.BindUsers(context, UserName, AccessToken, content);
				}else{
					String errorStr = "Bind Fail, Error Code: " + errorCode;
					if (errorCode == 30607) {
						Log.d(TAG, "Bind Fail error:update channel token-----!");
					}else{
						Log.d(TAG, "Bind Fail error:" + errorStr);
					}					
					PushManager.reBind(context, PushConstants.LOGIN_TYPE_API_KEY, PushMessageReceiver.API_KEY, UserName);
				}
			}
			
			
		//可选。通知用户点击事件处理
		} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)){			
			//自定义点击事件
		}
	}
	
	/**推送，创建任务*/
	public void pushCreateTask(Context context,String message,String alert){
		if(PushManager.addPushTask(context,message) && UserManager.getInstance().isLogin()){//推送，创建任务
			Log.e(TAG, "PushMessageReceiver addPushTask fail");
			
			Intent mIntent = new Intent(Main.NEWTASKTRENDS); 
            mIntent.putExtra("yaner", context.getString(R.string.yuandong)); 
            context.sendBroadcast(mIntent);
			
			//当有新推送消息时，发送一个通知
			@SuppressWarnings("static-access")
			NotificationManager manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
    		Notification notify = new Notification(R.drawable.logo_code_orange,null,System.currentTimeMillis());
    		notify.sound=Settings.System.DEFAULT_NOTIFICATION_URI;	    		
    		Intent notifyIntent = new Intent();
    		notifyIntent.setAction(INTENT_NEWTASK);
    		notifyIntent.setClass(context, Main.class);

    		notifyIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
    		PendingIntent intent1 = PendingIntent.getActivity(context, 0, notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT );
    		String title = context.getString(R.string.main_newtrends);
	
    		notify.setLatestEventInfo(context, title, alert, intent1);
    		notify.flags = Notification.FLAG_AUTO_CANCEL; 
    		manager.notify(newmessage,notify);	
		}
	}
	
	
	
	

}
