package com.whhcxw.MobileCheck;

import org.json.JSONObject;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
/**
 * 维修厂接待员
 */
public class Receptionist extends BaseActivity {

//	private final String TAG = "LargerDangerCaseDetails";
	
//	private String mPath_str;
	private String mCaseNo;
	private String mTasktype;
	private String mTaskID;
	private String mAccessToken;


	private Titlebar2 titlebar;

	private Context mContext;
	/** 车牌号 */
	private TextView mCar_TextView;
	/** 案件号 */
	private TextView mCasenumber_TextView;
	/** 车主 */
	private TextView mCarmaster_TextView;
	/** 联系电话 */
	private TextView mPhone_TextView;
	/** 案发时间 */
	private TextView mTime_TextView;
	/** 案发地点 */
	private TextView mAddress_TextView;

	private ContentValues mContentValues;

	private String mUserClass;
	private String mUserName;

	
	private Button mSure_button;
	
	private TextView mMoneyText;
	
	
	public static final int RECEPTIONIST = 33;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_receptionist);
		mContext = this;
		initTitle();

	}


	public void initTitle() {
		initview();
		
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		
		
		if(mUserClass.equals(Main.RECEPTIONIST)){
			titlebar.setCenterText(R.string.app_name_recommend);
		}else{
			titlebar.setCenterText(R.string.app_name);
		}
		
		
	}

	private void initview() {
		Intent intent = getIntent();
		mContentValues = intent.getParcelableExtra("values");
		mCaseNo = mContentValues.get(DBModle.Task.CaseNo).toString();
		mTaskID = mContentValues.getAsString(DBModle.Task.TaskID);
		mTasktype = mContentValues.get(DBModle.Task.TaskType).toString();

		mUserName = UserManager.getInstance().getUserName();
		mUserClass = UserManager.getInstance().getUserClass();
		mAccessToken = UserManager.getInstance().getAccessToken();

		mCar_TextView = (TextView) this.findViewById(R.id.car);
		mCar_TextView.setText(mContentValues.get(DBModle.Task.CarMark).toString());
		mCasenumber_TextView = (TextView) this.findViewById(R.id.casenumber);
		mCasenumber_TextView.setText(mContentValues.get(DBModle.Task.CaseNo).toString());
		mCarmaster_TextView = (TextView) this.findViewById(R.id.carmaster);
		mCarmaster_TextView.setText(mContentValues.get(DBModle.Task.CarOwner).toString());
		mPhone_TextView = (TextView) this.findViewById(R.id.phone);
		mPhone_TextView.setText(mContentValues.get(DBModle.Task.Telephone).toString());
		mTime_TextView = (TextView) this.findViewById(R.id.time);
		mTime_TextView.setText(mContentValues.get(DBModle.Task.AccidentTime).toString());
		mAddress_TextView = (TextView) this.findViewById(R.id.address);
		mAddress_TextView.setText(mContentValues.get(DBModle.Task.Address).toString());
		
		mSure_button = (Button)this.findViewById(R.id.button);
		mSure_button.setOnClickListener(mSureButtonOnClickListener);
		
		mMoneyText = (TextView)this.findViewById(R.id.moneyText);
		
		if(mContentValues.getAsString(DBModle.Task.BackState).equals(DBModle.TaskLog.FrontState_FINISH)){
			mSure_button.setEnabled(false);
		}
	}
	
	
	
	
	/**确定*/
	OnClickListener mSureButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			/*final EditText editText = new EditText(mContext);
			editText.setInputType(InputType.TYPE_CLASS_NUMBER);
			Dialog.edittextDialog(mContext, mContext.getResources().getString(R.string.dialog_titlemess),editText,new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String caseMoey = editText.getText().toString();
					if(caseMoey.equals("")){
						Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.receptionist_money));
					}else{
						String pushmoney =  editText.getText().toString();
						DataHandler dataHandler = new DataHandler(mContext);
						dataHandler.UpdateTask(mCaseNo, mTaskID, mTasktype, mUserName, "BackPrice|BackState", pushmoney+"|5", mAccessToken, httpResponseHandler);
					}
				}
			});*/
			
			String pushmoney = mMoneyText.getText().toString();
			if(pushmoney.equals("")){
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.receptionist_money));
				return;
			}
			
			DataHandler dataHandler = new DataHandler(mContext);
			dataHandler.UpdateTask(mCaseNo, mTaskID, mTasktype, mUserName, "BackPrice|BackState", pushmoney+"|5", mAccessToken, httpResponseHandler);
			
			
			
		}
	};

	
	HttpResponseHandler httpResponseHandler = new HttpResponseHandler(){
		@Override
		public void response(boolean success, String response, Throwable error) {
			// TODO Auto-generated method stub
			super.response(success, response, error);
			if(success){
				try {
					JSONObject jsonObject = new JSONObject(response);
					String code = jsonObject.getString("code");
					if(code.equals("0")){
						Intent intent = new Intent();
						setResult(RECEPTIONIST, intent);
						finish();
					}else{
						Dialog.negativeAndPositiveDialog(mContext, mContext.getResources().getString(R.string.receptionist_changegarage), new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								Intent intent = new Intent();
								setResult(RECEPTIONIST, intent);
								finish();
							}
						});
					}
				}catch (Exception e) {
					// TODO: handle exception
					Dialog.negativeAndPositiveDialog(mContext, mContext.getResources().getString(R.string.receptionist_fail), new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
				}
			}
		}
		
	};
	
}
