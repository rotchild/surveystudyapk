package com.whhcxw.MobileCheck;

import org.json.JSONException;
import org.json.JSONObject;

import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.global.G;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.pushservice.PushMessageReceiver;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.utils.AppManager;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ChangePassword extends BaseActivity {

	private final String TAG = this.getClass().getName();

	private Context mContext;

	private TextView mOldPwd_TextView;
	private TextView mNewPwd_TextView;
	private TextView mSurePwd_TextView;

	private Button mSubmit_Button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag == 0)
			finish();

		setContentView(R.layout.changepassword);
		mContext = this;
		initTitle();
	}

	public void initTitle() {
		initView();

		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		titlebar.setCenterText(R.string.app_settingsystem);
	}

	private void initView() {
		mOldPwd_TextView = (TextView) this.findViewById(R.id.oldpwd);
		mNewPwd_TextView = (TextView) this.findViewById(R.id.newpwd);
		mSurePwd_TextView = (TextView) this.findViewById(R.id.surepwd);

		mSubmit_Button = (Button) this.findViewById(R.id.button);
		mSubmit_Button.setOnClickListener(mSubmitButton);
	}

	/** 修改密码提交 */
	OnClickListener mSubmitButton = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			MD5 md5 = new MD5();

			UserManager userManager = UserManager.getInstance();

			String oldPwd = mOldPwd_TextView.getText().toString();
			if (!md5.toMd5(oldPwd).equals(userManager.getPassWord())) {
				Dialog.negativeDialog(mContext,
						getString(R.string.changepassword_oldpwd_error));
				return;
			}

			String newPwd = mNewPwd_TextView.getText().toString();
			if (newPwd.equals("")) {
				Dialog.negativeDialog(mContext,
						getString(R.string.changepassword_newpwd_nonull));
				return;
			}

			String surePwd = mSurePwd_TextView.getText().toString();
			if (!newPwd.equals(surePwd)) {
				Dialog.negativeDialog(mContext,
						getString(R.string.changepassword_newpwd_noequal));
				return;
			}

			DataHandler dataHandler = new DataHandler(mContext);
			dataHandler.setIsShowProgressDialog(true);
			dataHandler.ChangePassWord(userManager.getUserName(), md5.toMd5(oldPwd), md5.toMd5(newPwd) ,userManager.getAccessToken() , ChangePassWordHttpResponseHandler);
		}
	};

	HttpResponseHandler ChangePassWordHttpResponseHandler = new HttpResponseHandler() {
		@Override
		public void response(boolean success, String response, Throwable error) {
			if (success) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					String code = jsonObject.getString("code");
					if (!code.equals("0")) {
						String cause = jsonObject.getString("cause");
						G.showToast(mContext, cause, true);
						return;
					}
					
					Dialog.positiveDialog(mContext, getString(R.string.changepassword_success), new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							
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
									
									NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
									notificationManager.cancel(PushMessageReceiver.newmessage);
									
									UserManager.getInstance().userIsLogin(false);					
									//解除绑定成功，清空推送绑定信息
									boolean b = PushManager.clearPushPreferences(mContext);
									if(b){
										Log.d(TAG, "PushManager.clearPushPreferences  success!!!!");
									}else{
										Log.d(TAG, "PushManager.clearPushPreferences  fail!!!!");
									}
										
									NotificationManager notificationManager1 = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
									notificationManager1.cancel(0);
									
									((MobileCheckApplocation)getApplication()).logout();
									AppManager.getAppManager().finishAllActivity();
									startActivity(new Intent(mContext,Login.class));
								}
							});
						}
					});

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					G.showToast(mContext,mContext.getResources().getString(R.string.changepassword_data_error), true);
					return;
				}

			} else {
				G.showToast(mContext,mContext.getResources().getString(R.string.changepassword_error),true);
			}
		}
	};

}
