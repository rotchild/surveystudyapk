package com.whhcxw.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.global.G;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallAlarm extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = AlarmColock.getInstance(context).ACTION;
	  if(action.equals(intent.getAction())){
			boolean b = CatchException.sendExceptionEmail(context);
			if(b){
				CatchException.clearException(context);
			}else{
				String time = getOneTime(3);
				String alram = time.substring(11, time.length()); 
				AlarmColock.getInstance(context).setAlarmColock(alram, false);
				return;
			}
		}
		
		//定时删除一个月外的案件
		String date = getOneDate(1).substring(0, 10);
		if(date == null) return;
		boolean result =  DBOperator.deleteOverdueCase(date);
		if(result){
			Intent mIntent = new Intent(Main.NEWTASKTRENDS); 
            mIntent.putExtra("yaner", context.getString(R.string.yuandong)); 
            context.sendBroadcast(mIntent);
		}
		String [] isRequest = new String[] {DBModle.TaskStack.IsRequest_S,DBModle.TaskStack.IsRequest_F};
		boolean result2 =  DBOperator.deleteTaskStackByIsRequest(isRequest,date);
		Log.d("CallAlarm deleteTaskStackByIsRequest", result2+"");
		
		boolean result3 =  DBOperator.deleteOverduePushmessage(date);
		Log.d("CallAlarm deleteOverduePushmessage", result3+"");
	}

	
	@SuppressLint("SimpleDateFormat")
	private String getOneDate(int month){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.MONTH, -month);    //得到前一个月 
//		cal.add(Calendar.DATE, month);
		long date = cal.getTimeInMillis();
		return format.format(new Date(date));
	}
	
	/**
	 * 多少分钟后的时间
	 * @param minutes
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	private String getOneTime(int minutes){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.MINUTE, minutes);    
		long date = cal.getTimeInMillis();
		return format.format(new Date(date));
	}
	
}
