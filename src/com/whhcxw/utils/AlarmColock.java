package com.whhcxw.utils;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmColock {
	private static Context mContext;
	public String ACTION = "whcx.alarm.alarmcolock.action";

	private static AlarmColock mAlarmClock;

//	private	long oneday = 10*60* 1000L;// 闹铃间隔， 10分钟
	private	long oneday = 24*60*60* 1000L;// 闹铃间隔， 24小时
	public static AlarmColock getInstance(Context context) {
		mContext = context;
		if (mAlarmClock == null) {
			mAlarmClock = new AlarmColock();
		}
		return mAlarmClock;
	}

	/**
	 * 设置定时，上传错误日志
	 * @param alarmDate 格式  “13:12:12”
	 * @param continuationAlarm      true连续闹    false 不连续闹
	 */
	@SuppressLint("SimpleDateFormat")
	public void setAlarmColock(String alarmDate , boolean continuationAlarm){
		String[] alarm = alarmDate.split(":");
		
		Calendar c  = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarm[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(alarm[1]));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long timeMillis=c.getTimeInMillis();
        if (timeMillis<System.currentTimeMillis()){
        	timeMillis+=oneday;
        }
		setAlarmTime(timeMillis , continuationAlarm);
	}
	
	/**
	 * 设置闹钟
	 * @param timeInMillis
	 * @param continuationAlarm      true连续闹  false 不连续闹
	 */
	public void setAlarmTime(long timeInMillis , boolean continuationAlarm) {
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent();
		intent.setAction(ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
		if(continuationAlarm){
			alarmManager.setRepeating(AlarmManager.RTC, timeInMillis,oneday, sender);
		}else{
			alarmManager.set(AlarmManager.RTC, timeInMillis, sender);
		}
	}
	
	
	
	
	/**
	 * 注销
	 */
	public void destroyAlarm(){
		Intent intent = new Intent();
		intent.setAction(ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

}