package com.whhcxw.broadcaset;


import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.service.SendGpsLogService;
import com.whhcxw.utils.AlarmColock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver{

	private static final String TAG = "com.whhcxw.MobileCheck";
	private SendGpsLogService mGpsLogService;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
	    { 
			Log.d(TAG, "the service of APP MobileCheck started");
			
			mGpsLogService = new SendGpsLogService(context);
			mGpsLogService.start();
			
			//开机启动重新设置发送日志闹钟
			AlarmColock colock = AlarmColock.getInstance(context);
			colock.destroyAlarm();
			colock.setAlarmColock(context.getString(R.string.senderror_date),true);	
	    }       
	}

}
