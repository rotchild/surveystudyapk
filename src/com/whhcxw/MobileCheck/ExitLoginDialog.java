package com.whhcxw.MobileCheck;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.utils.AppManager;

public class ExitLoginDialog extends BaseActivity {
	private final String TAG = this.getClass().getName();
	private Context mContext;
	private Button mSure_Button;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exitlogindialog);
		mContext = this;
		initView();
	}
	
	public void initView(){
		mSure_Button = (Button)this. findViewById(R.id.button);
		mSure_Button.setOnClickListener(mSureButtonOnClickListener);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				UserManager userManager = UserManager.getInstance();
				String UserName = userManager.getUserName();
				String AccessToken = userManager.getAccessToken();
				String PushID = PushManager.getBindID(mContext);
				
				AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
				
				String url = HttpParams.UNBINDPUSHUSER;		
				RequestParams params = new RequestParams();
				params.put("UserName", UserName);
				params.put("PushID", PushID);
				params.put("AccessToken", AccessToken);	
				mAsyncHttpClient.post(url, params,new HttpResponseHandler(){
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
						
						UserManager.getInstance().userIsLogin(false);					
						//解除绑定成功，清空推送绑定信息
						boolean b = PushManager.clearPushPreferences(mContext);
						if(b){
							Log.d(TAG, "PushManager.clearPushPreferences  success!!!!");
						}else{
							Log.d(TAG, "PushManager.clearPushPreferences  fail!!!!");
						}
						cancelNotification();
						((MobileCheckApplocation)getApplication()).logout();
					}
				});
			}
		}).start();
		
	}
	
	
	OnClickListener mSureButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
					AppManager.getAppManager().finishAllActivity();
					startActivity(new Intent(mContext,Login.class));
					finish();
				}
	};
	
	/** 取消图标 */
	private void cancelNotification() {
		// 启动后删除之前我们定义的通知
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME){
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
}
