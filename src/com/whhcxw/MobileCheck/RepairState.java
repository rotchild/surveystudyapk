package com.whhcxw.MobileCheck;

import java.math.BigDecimal;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RepairState extends BaseActivity {

	private final String TAG = this.getClass().getName();
	
	private Titlebar2 titlebar;
	private Context mContext;
	
	private String mTID;
	private String mCaseNo;
	private String mTaskType;
	private String mOperator;
	private String mAccessToke;
	
	/** 车牌号 */
	private TextView mCar_TextView;
	/** 案件号 */
	private TextView mCasenumber_TextView;
	/** 查勘员 */
	private TextView mSurvey_TextView;
	/** 审查员 */
	private TextView mVerify_TextView;
	/** 车主 */
	private TextView mCarmaster_TextView;
	/** 联系电话 */
	private TextView mPhone_TextView;
	/** 案发时间 */
	private TextView mTime_TextView;
	/** 案发地点 */
	private TextView mAddress_TextView;
	private TextView mGarages_TextView;
	
	private String mUserPhone;

	private ContentValues mContentValues;

	private Button mAlready_button;
	private Button mNoKnow_button;
	private Button mChange_button;
	private Button mPhone_button;
	
	private String Longitude;
	private String Latitude;
	
	
	
	private boolean isCooperation;
	
	/**推修状态*/
	public static final int REPAIRSTATE_FINISH = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.repairstate);
		mContext = this;
		initTitle();
		initView();
	}
		
	public void initTitle() {
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);
	}
	
	public void initView(){
	
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mTID = bundle.getString("TID");
		isCooperation = bundle.getBoolean("isCooperation");

		if(isCooperation){
			mContentValues = DBOperator.getCooperationGarageCase(mTID);
		}else {
			mContentValues = DBOperator.getGarageCase(mTID);
		}
		mUserPhone = mContentValues.get(DBModle.Task.Telephone).toString();
		
		mCaseNo = mContentValues.getAsString(DBModle.Task.CaseNo);
		mTaskType = mContentValues.getAsString(DBModle.Task.TaskType);
		mOperator = UserManager.getInstance().getUserName();
		mAccessToke = UserManager.getInstance().getAccessToken();
		
		mCar_TextView = (TextView) this.findViewById(R.id.car);
		mCar_TextView.setText(mContentValues.get(DBModle.Task.CarMark).toString());
		mCasenumber_TextView = (TextView) this.findViewById(R.id.casenumber);
		mCasenumber_TextView.setText(mContentValues.get(DBModle.Task.CaseNo).toString());
//		mSurvey_TextView = (TextView) this.findViewById(R.id.survey);
//		mSurvey_TextView.setText(mContentValues.get("survey").toString());
//		mVerify_TextView = (TextView) this.findViewById(R.id.verify);
//		mVerify_TextView.setText(mContentValues.get("verify").toString());
		mCarmaster_TextView = (TextView) this.findViewById(R.id.carmaster);
		mCarmaster_TextView.setText(mContentValues.get(DBModle.Task.CarOwner).toString());
		mPhone_TextView = (TextView) this.findViewById(R.id.phone);
		mPhone_TextView.setText(mUserPhone);
		mTime_TextView = (TextView) this.findViewById(R.id.time);
		mTime_TextView.setText(mContentValues.get(DBModle.Task.AccidentTime).toString());
		mAddress_TextView = (TextView) this.findViewById(R.id.address);
		mAddress_TextView.setText(mContentValues.get(DBModle.Task.Address).toString());
		
		mGarages_TextView = (TextView)this.findViewById(R.id.garages);
		String garagename =mContentValues.getAsString(DBModle.Garage.ShortName);
		if(garagename.equals("")){
			garagename = mContentValues.getAsString(DBModle.Garage.FullName);
		}
		mGarages_TextView.setText(garagename);
		
		mAlready_button = (Button)this.findViewById(R.id.already_button);
		mAlready_button.setOnClickListener(mAlreadyButtonOnClickListener);
		mNoKnow_button = (Button)this.findViewById(R.id.noKnow_button);
		mNoKnow_button.setOnClickListener(mNoKnowButtonOnClickListener);
		mChange_button = (Button)this.findViewById(R.id.change_button);
		mChange_button.setOnClickListener(mChangeButtonOnClickListener);
		mPhone_button = (Button)this.findViewById(R.id.phone_button);
		mPhone_button.setOnClickListener(mPhoneButtonOnClickListener);
		
		String frontState = mContentValues.getAsString(DBModle.Task.FrontState);
		if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
			mAlready_button.setEnabled(false);
			mNoKnow_button.setEnabled(false);
			mChange_button.setEnabled(false);
		}
		
	}
	
	/**已去*/
	OnClickListener mAlreadyButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Longitude = BaiduLocation.instance().LOCAITON_LONITUDE;
			Latitude = BaiduLocation.instance().LOCAITON_LATITUDE;
			
			final String frontPrice = mContentValues.getAsString(DBModle.Task.FrontPrice);
			
			final EditText editText = new EditText(mContext);
			editText.setText(frontPrice);
			editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			Dialog.edittextDialog(mContext, mContext.getResources().getString(R.string.repair_verify), editText, new AlertDialog.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String pushPrice = editText.getText().toString();
					float f = Float.parseFloat(pushPrice);
					BigDecimal bigDecimal = new BigDecimal(f);
					float f2 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					pushPrice = String.valueOf(f2);
					if(frontPrice.equals(pushPrice)){
						boolean b = DBOperator.updateTaskSate(mTID, DBModle.TaskLog.FrontState_FINISH, Longitude, Latitude);
						if(b){
							if(DBOperator.repairUpdateState(mCaseNo,mTID,mTaskType,mOperator,mAccessToke)){
								UploadWork.sendDataChangeBroadcast(mContext);
								Intent intent = new Intent();
								setResult(CaseManager.CASESTATE, intent);
								finish();
							}
						}
					} else {
						if(DBOperator.finishTask(pushPrice, mTID, Longitude, Latitude)){
							UploadWork.sendDataChangeBroadcast(mContext);
							Intent intent = new Intent();
							setResult(CaseManager.CASESTATE, intent);
							finish();
						}
						
						
						
					}
				}
			});
		}
	};
	
	/**未知*/
	OnClickListener mNoKnowButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//未知不做任何操作，只关闭当前页面
//			boolean b = DBOperator.updateTaskSate(mTID, DBModle.TaskLog.FrontState_ASSIGN_ERROE, Longitude, Latitude);
//			if(b){
				finish();
//			}
		}
	};
	
	/**更换*/
	OnClickListener mChangeButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext,RepairRecommend.class);
			intent.putExtra("TID", mTID);
			intent.putExtra("update", true);
			intent.putExtra("updateisCooperation", isCooperation);
			startActivity(intent);
			
			int isCooperation = mContentValues.getAsInteger(DBModle.Task.IsCooperation);
			
			if(isCooperation != -1){
				ContentValues contentValues = new ContentValues();
				contentValues.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION);
				boolean b = DBOperator.updateTask(mTID, contentValues);
				if(b){
					Log.d(TAG, "updateTask err");
				}
			}
			
			finish();
		}
	};
	
	/**用户电话*/
	OnClickListener mPhoneButtonOnClickListener =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Dialog.telUserDialog(mContext, mUserPhone);
		}
	};
	
}
