package com.whhcxw.MobileCheck;

import org.json.JSONException;
import org.json.JSONObject;

import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.crashreport.CrashHandler;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.pushservice.PushMessageReceiver;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.theme.PreferenceHelper;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.utils.AppManager;
import com.whhcxw.utils.CatchException;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class SystemSetting extends BaseActivity {

	private final String TAG = this.getClass().getName();
	private Context mContext;
	
	private View mSetID_View;
	private View mSystemcode_View;
	private Button mLogout_Button;
	private Button mExit_Button;
//	private Button mSetvideoID_Button;
	private TextView mUser_TextView;
	private Button mUser_Button;
	private ProgressDialog mProgressDialog;
	private Thread mThread;
	public static final int SETIP=112;
	
	private TextView mIP_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.systemsetting);
		mContext = this;
		initTitle();
	}
	
	public void initTitle() {
		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		String mUserClass = UserManager.getInstance().getUserClass();
		if(mUserClass.equals(Main.SURVEY)){
			titlebar.setCenterText(R.string.app_name);
		}else if(mUserClass.equals(Main.SUPERVISOR)){
			titlebar.setCenterText(R.string.app_name_large);
		}else if(mUserClass.equals(Main.DANGER)){
			titlebar.setCenterText(R.string.app_name_danger);
		}else if(mUserClass.equals(Main.RECEPTIONIST)){
			titlebar.setCenterText(R.string.app_name_recommend);
		}else if(mUserClass.equals(Main.OVERHAUL)){
			titlebar.setCenterText(R.string.app_name_overhaul);
		}
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mProgressDialog = ToolsProgressDialog.getInitProgressDialog(mContext, mContext.getResources().getString(R.string.request_loading)).showProgressDialog();
		mSetID_View = (View)this.findViewById(R.id.setID);
		mSetID_View.setOnClickListener(mSetIDViewOnClickListener);
		mSystemcode_View = (View)this.findViewById(R.id.systemcode);
		mSystemcode_View.setOnClickListener(mSystemcodeViewOnClickListener);
		mLogout_Button =(Button)this.findViewById(R.id.logout_button);
		mLogout_Button.setOnClickListener(mLogoutButtonOnClickListener);
		mExit_Button = (Button)this.findViewById(R.id.exit_button);
		mExit_Button.setOnClickListener(mExitButtonOnClickListener);
		mUser_TextView = (TextView)this.findViewById(R.id.user);
		mUser_Button = (Button)this.findViewById(R.id.usermes);
		mUser_Button.setOnClickListener(mUserButtonOnClickListener);
		
		UserManager manager = UserManager.getInstance();
		
		mUser_TextView.setText(manager.getRealName());
		
		mIP_text = (TextView)this.findViewById(R.id.ip_text);
		String ip = manager.getIP();
		if(ip.equals("")){
			ip = HttpParams.BSASURL;
		}
		mIP_text.setText(ip.substring(7, ip.length()-1));
		
		TextView bound_text = (TextView)this.findViewById(R.id.bound_text);
		String pushID = PushManager.getBindID(mContext);
		if(pushID.equals("")){
			bound_text.setText(getString(R.string.systemseting_bound));
		}else {
			bound_text.setText(pushID);
		}
		
		TextView systemcode_text = (TextView)this.findViewById(R.id.systemcode_text);
		systemcode_text.setText(SystemCode.getVersionName(mContext));
		
		Button exception_Button = (Button)this.findViewById(R.id.exception);
		
		exception_Button.setOnClickListener(new exceptionButtonOnClickListener());
		
		RadioGroup group = (RadioGroup)this.findViewById(R.id.radiogroup1);
		
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == R.id.block){
					PreferenceHelper.setTheme(mContext, R.style.app_skin_black);
				} else if(checkedId == R.id.orange){
					PreferenceHelper.setTheme(mContext, R.style.app_skin_orange);
				}
				reload();
			}
		});
	}
	
	
	/**
	 * 发送错误日志Email
	 */
	class exceptionButtonOnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(CrashHandler.getInstance().getActiveNetwork(mContext)==null){
				Dialog.negativeDialog(mContext, getString(R.string.systemsetting_web));
			}else{
				mProgressDialog.show();
				mThread = new Thread(mProgressDialogRunnable);
				mThread.start();
			}
		}
	}
	
	private final int sendSuccess = 0;
	private  final int sendFalie = 1;
	private Runnable mProgressDialogRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean b = CatchException.sendExceptionEmail(mContext);
			if(b){
				CatchException.clearException(mContext);
				mProgressDialogHandler.obtainMessage(sendSuccess).sendToTarget();
			}else{
				mProgressDialogHandler.obtainMessage(sendFalie).sendToTarget();
			}
		}
	};
	
	private Handler mProgressDialogHandler =new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case sendSuccess:
				mProgressDialog.dismiss();
				Dialog.negativeDialog(mContext, getString(R.string.systemsetting_exception_success));
				break;
			case sendFalie:
				mProgressDialog.dismiss();
				Dialog.negativeDialog(mContext, getString(R.string.systemsetting_exception_error));
				break;
			default:
				break;
			}
		}
	};
	
	/**IP端口设置*/
	OnClickListener mSetIDViewOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext , SettingIP.class);
			intent.putExtra("IPEdit", false);
			startActivity(intent);
		}
	};
	
/*	*//**视频IP端口设置*//*
	OnClickListener mSetvideoIDButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext , SettingIP.class);
			startActivityForResult(intent, SETIP);
		}
	};*/
	
	/**修改用户密码*/
	OnClickListener mUserButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext , ChangePassword.class);
			startActivity(intent);
		}
	};
	
	
	/**系统版本*/
	OnClickListener mSystemcodeViewOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext , SystemCode.class);
			startActivity(intent);
		}
	};
	
	
	/**注销*/
	OnClickListener mLogoutButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			UserManager userManager = UserManager.getInstance();
			String UserName = userManager.getUserName();
			String AccessToken = userManager.getAccessToken();
			String PushID = PushManager.getBindID(mContext);
					
			DataHandler dataHandler = new DataHandler(mContext);
			dataHandler.setIsShowProgressDialog(true);		
			dataHandler.setIsCancelableProgressDialog(false);
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
					
					UserManager.getInstance().userIsLogin(false);					
					//解除绑定成功，清空推送绑定信息
					boolean b = PushManager.clearPushPreferences(mContext);
					if(b){
						Log.d(TAG, "PushManager.clearPushPreferences  success!!!!");
					}else{
						Log.d(TAG, "PushManager.clearPushPreferences  fail!!!!");
					}
						
					cancelNotification();
					
					JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put("LogoutSystem", "注销程序");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CatchException.saveException(mContext, jsonObject);
					
					((MobileCheckApplocation)getApplication()).logout();
					AppManager.getAppManager().finishAllActivity();
					startActivity(new Intent(mContext,Login.class));
					
				}
			});
		}
	};
	
	/**退出程序*/
	OnClickListener mExitButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			cancelNotification();
			
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("ExitSystem", "退出程序");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CatchException.saveException(mContext, jsonObject);
			
			((MobileCheckApplocation)getApplication()).exit();
			AppManager.getAppManager().AppExit(mContext);
			
			
		}
	};
	
	/** 取消图标 */
	private void cancelNotification() {
		// 启动后删除之前我们定义的通知
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case SETIP:
			break;

		default:
			break;
		}
		
	}

	
	
	
}
