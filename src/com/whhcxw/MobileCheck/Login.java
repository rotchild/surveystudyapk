package com.whhcxw.MobileCheck;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.data.DBModle.TaskStack;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.updateapp.UpdateAppDialog;
import com.whhcxw.utils.CatchException;

import android.os.Bundle;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends BaseActivity {

	private final String TAG = this.getClass().getName();

	private Titlebar2 titlebar;
	private EditText mEditText_name;
	private EditText mEditText_pass;
	private Context mContext;

	/** 用户名 */
	private String mUserName;
	private String md5Pass;

	private String mUserID = "";// 用户ID
	private String mAreaID = "";// 区域ID
	private String mIMEI = "";// 设备号
	private String mTelephone = "";// 用户电话
	private String mEmail = "";// 用户Email
	private String mRealName = "";// 用户真实姓名
	private String mRoleTypes = "";// 系统配置功能

	// private SharedPreferences preferences;

	/** 用户类型 */
	private String mUserClass = "";// 2,查勘员 6,区域督导 8,风险调查员
	private String mAccessToken = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		mContext = this;
		initTitle();
		initView();

		// 弹出升级APP 对话框
		UpdateAppDialog.showUpdateAppDialog(this, G.UPDATE_APP_SAVE_PATH,
				R.drawable.logo_code_orange);
	}

	public void initTitle() {
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.setCenterText(R.string.app_login);
	}

	public void initView() {
		Button button = (Button) this.findViewById(R.id.button);
		button.setOnClickListener(loginOnClickListener);
		mEditText_name = (EditText) this.findViewById(R.id.LTUserName);

		// preferences =getSharedPreferences("user",0);
		mEditText_name.setText(UserManager.getInstance().getUserName());
		mEditText_pass = (EditText) this.findViewById(R.id.LPassWordText);
	}

	// 登录事件
	OnClickListener loginOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 检查上传任务列表中是否解除用户推送绑定的任务还在不，如果是同一个用户就把其删除，不是就不管
			mUserName = mEditText_name.getText().toString();

			ArrayList<ContentValues> taskStackList = DBOperator.getTaskStack();
			if (taskStackList != null) {
				for (ContentValues contentValues : taskStackList) {
					String taskTack_Values = contentValues.getAsString(TaskStack.Vals);
					String[] values = taskTack_Values.split("\\|");
					String username = values[0];
					if (username.equals(mUserName)) {
						boolean b = DBOperator.deleteTaskStack(contentValues
								.getAsString(TaskStack.StackID));
						if (b) {
							Log.e(TAG,
									getString(R.string.systemseting_unbindpushuser_fail));
						}
					}
				}
			}

			String userPass = mEditText_pass.getText().toString();
			if (mUserName.equals("") || userPass.equals("")) {
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.login_mess));
			} else {
				MD5 md5 = new MD5();
				md5Pass = md5.toMd5(userPass);

				DataHandler dataHandler = new DataHandler(mContext);
				dataHandler.setIsShowProgressDialog(true);
				dataHandler.UserCheck(mUserName, md5Pass, "", mUserCheckResponse);
			}
		}
	};

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
					if (!code.equals("0")) {
						String cause = jsonObject.getString("cause");
						G.showToast(mContext, cause, true);
						return;
					}
					mUserClass = jsonObject.getJSONObject("value").getString(
							"UserClass");
					mUserID = jsonObject.getJSONObject("value").getString(
							"UserID");
					mIMEI = jsonObject.getJSONObject("value").getString("IMEI");
					mTelephone = jsonObject.getJSONObject("value").getString(
							"Telephone");
					mEmail = jsonObject.getJSONObject("value").getString(
							"Email");
					mRealName = jsonObject.getJSONObject("value").getString(
							"RealName");
					mAccessToken = jsonObject.getJSONObject("value").getString(
							"AccessToken");
					mAreaID = jsonObject.getJSONObject("value").getString(
							"AreaID");
					mRoleTypes = jsonObject.getJSONObject("value").getString(
							"RoleTypes");

					// !mUserClass.equals(Main.SUPERVISOR)&&
					// &&!mUserClass.equals(Main.DANGER)
					if (!mUserClass.equals(Main.SURVEY)
							&& !mUserClass.equals(Main.RECEPTIONIST)
							&& !mUserClass.equals(Main.OVERHAUL)) {
						Dialog.negativeDialog(mContext,
								getString(R.string.login_illegitmacy));
						return;
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					G.showToast(mContext,mContext.getResources().getString(R.string.login_error), true);
					return;
				}

				UserManager.getInstance().saveUserInfo(mContext, mUserID,
						mUserName, mUserClass, mIMEI, mTelephone, mEmail,
						mRealName, mAccessToken, mAreaID, mRoleTypes,md5Pass);

				Intent intent = new Intent(mContext, Main.class);
				startActivity(intent);
				finish();
				
				
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("LoginSuccess", "Login Success");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				CatchException.saveException(mContext, jsonObject);
			} else {
				G.showToast(
						mContext,
						mContext.getResources().getString(R.string.login_error),
						true);
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST, 0, getString(R.string.login_setIP));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case Menu.FIRST:
			Intent intent = new Intent(mContext, SettingIP.class);
			intent.putExtra("IPEdit", false);
			startActivity(intent);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 清除升级提示
		UpdateAppDialog.cleanHint();
	}

}
