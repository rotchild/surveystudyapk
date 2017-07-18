package com.whhcxw.broadcaset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.whhcxw.MobileCheck.IntoCase;
import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	private final String TAG = "SMSReceiver";
	/**是否已读  0 已读  1 未读*/
	public static int ISREAD = 1;
	
	private Context mContext;
	private static final String SMS_RECEIVED ="android.provider.Telephony.SMS_RECEIVED";
	
	public static final int notifyNewTask = 2;
	
	private int newmessage = 4;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "短信截取");
		mContext = context;
		if(intent.getAction().equals(SMS_RECEIVED)){
			loadMsg(intent);
		}
	}

	private void loadMsg(Intent intent) {
		// TODO Auto-generated method stub
		
		try {
			Bundle bundle = intent.getExtras();
			Object[] messages = (Object[]) bundle.get("pdus");
			SmsMessage[] smsMessage = new SmsMessage[messages.length];
			String Msg="";
			String tel = "";
			int smsL= smsMessage.length;
			if(smsL>0){
				  for (int i = 0; i < smsMessage.length; i++) 
			        {    	
			        	smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);	 
//			        	String ss = smsMessage[i].getMessageBody();
				        Msg += new String(smsMessage[i].getMessageBody());			        
			        }
			        Msg  = replaceJsonChar(Msg);
			        tel = smsMessage[0].getOriginatingAddress();
			        String[] splitStrs = new String[]{"1/2)","2/2)","1/3)","2/3)","3/3)"};
			        boolean isSplit = false;
			        for(int j = 0; j < splitStrs.length;j++){
						int splitIndex = Msg.indexOf(splitStrs[j]);
						if(splitIndex >=0){
							isSplit = true;
							break;
						}
					}
				
			        if(isSplit){
			        	@SuppressWarnings("static-access")
						NotificationManager manager = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
			    		Notification notify = new Notification(R.drawable.logo_code_orange,null,System.currentTimeMillis());
			    		notify.sound=Settings.System.DEFAULT_RINGTONE_URI;	    		
			    		Intent notifyIntent = new Intent();
			    		notifyIntent.setClass(mContext, IntoCase.class);
		
			    		notifyIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			    		PendingIntent intent1 = PendingIntent.getActivity(mContext, 0, notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT );
			    		String title = "任务";
			    		String text = "有新的可拼接短信，请查看！";
			    		notify.setLatestEventInfo(mContext, title, text, intent1);
			    		notify.flags = Notification.FLAG_AUTO_CANCEL; 
			    		manager.notify(notifyNewTask,notify);	
			        }else{
			        	
//			        		String sms = Msg.substring(7);
			        	
			        		JSONObject json = new JSONObject();
			        		json.put("phone", tel);
			        		json.put("msg", Msg);
//			        		json.put("msg", sms);
			        		String s = json.toString();
			        				        		
			        		boolean b = parserSMS(Msg);
//			        		boolean b = parserSMS(sms);
			        		if(b){
			        			ContentValues values = new ContentValues();
			        			values.put(DBModle.MessageSources.Contents, s);
			        			values.put(DBModle.MessageSources.IsRead, ISREAD);
			        			values.put(DBModle.MessageSources.TID, "");
			        			
			        			boolean b1 = DBOperator.addMessages(values);
			        			if(b1){
			        				Log.i(TAG, "add message success");
			        				
			        				Intent mIntent = new Intent(Main.NEWTASKTRENDS); 
			        	            mIntent.putExtra("yaner", mContext.getString(R.string.jiangxi)); 
			        	            mContext.sendBroadcast(mIntent);
			        				
			        				//当有新推送消息时，发送一个通知
			        				@SuppressWarnings("static-access")
			        				NotificationManager manager = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
			        	    		Notification notify = new Notification(R.drawable.logo_code_orange,null,System.currentTimeMillis());
			        	    		notify.sound=Settings.System.DEFAULT_NOTIFICATION_URI;	    		
			        	    		Intent notifyIntent = new Intent();
			        	    		notifyIntent.setClass(mContext, Main.class);

			        	    		notifyIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			        	    		PendingIntent intent1 = PendingIntent.getActivity(mContext, 0, notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT );
			        	    		String title = mContext.getString(R.string.main_newtrends);
			        	    		String message = mContext.getString(R.string.smsreceiver_newcase);
			        	    		notify.setLatestEventInfo(mContext, title, message , intent1);
			        	    		notify.flags = Notification.FLAG_AUTO_CANCEL; 
			        	    		manager.notify(newmessage,notify);	
			        							        						        				
			        			}
			        		}
			        }       
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "短信拦截失败:"+e);
		}
	}

	
	public String replaceJsonChar(String s){
		String ss = s.replace("[", "")
					.replace("]", "")
					.replace("{", "")
					.replace("}", "")
					.replace("\"", "")
					.replace("\\r", "")
					.replace("\\n", "");
		return ss;
	}

	/** 校验短信 */
	public boolean parserSMS(String sms) {
		//去除短信中的空格及换行
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(sms);
		String smsInfo = m.replaceAll("");
		try {
			Pattern pattern = Pattern.compile(IntoCase.PINGANSMSREG);
			Matcher matcher = pattern.matcher(smsInfo);
			if (matcher.find()) {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "短信校验失败"+e);
		}
		return false;
	}
	
	
	
}
