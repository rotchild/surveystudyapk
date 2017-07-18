package com.whhcxw.MobileCheck;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DBModle.Task;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CreateCase extends BaseActivity{

	private Context mContext;
	private Button mSurvey_Button;
	private Button mRepeat_Button;
	
	private String mUserName;
	
	/**案件号*/
	private EditText mCasenu_edit;
	/**车牌号*/
	private EditText mCarnu_edit;
	/**车主*/
	private EditText mOwner_edit;
	/**驾驶员*/
	private EditText mCarDriver_edit;
	/**车主电话*/
	private EditText mPhone_edit;
	/**事故地点*/
	private EditText mAddress_edit;
	/**查勘是true  复勘是false*/
	private boolean mSurveyRepeatBoolean=true;
	
	private Button mCreate_Button;
	
	private String mUserClass;
	
	private Titlebar2 titlebar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.createcase);
		mContext = this;
		initTitle();
		
	}
	
	public void initTitle() {
		initView();
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		if(mUserClass.equals(Main.OVERHAUL)){
			titlebar.setCenterText(R.string.app_name_overhaul);
		}else{
			titlebar.setCenterText(R.string.app_name);
		} 
	}

	public void initView(){
		mUserName = UserManager.getInstance().getUserName();
		mUserClass = UserManager.getInstance().getUserClass();
		mSurvey_Button = (Button)this.findViewById(R.id.survey_button);
		mSurvey_Button.setOnClickListener(mSurveyButtonOnClickListener);
		mRepeat_Button = (Button)this.findViewById(R.id.repeat_button);
		mRepeat_Button.setOnClickListener(mRepeatButtonOnClickListener);
		
		mCasenu_edit = (EditText)this.findViewById(R.id.casenu_edit);
		mCarnu_edit = (EditText)this.findViewById(R.id.carnu_edit);
		mOwner_edit = (EditText)this.findViewById(R.id.owner_edit);
		mCarDriver_edit = (EditText)this.findViewById(R.id.cardriver_edit);
		mPhone_edit = (EditText)this.findViewById(R.id.phone_edit);
		mAddress_edit = (EditText)this.findViewById(R.id.address_edit);
		
		mCreate_Button = (Button)this.findViewById(R.id.create);
		mCreate_Button.setOnClickListener(mCreateOnClickListener);
		
		if(mUserClass.equals(Main.OVERHAUL)){
			mSurvey_Button.setVisibility(View.GONE);
			mRepeat_Button.setVisibility(View.GONE);
		}
	}
	
	/**查勘Button事件*/
	OnClickListener mSurveyButtonOnClickListener= new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mSurveyRepeatBoolean = true;
			mSurvey_Button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_survey_back));
			mRepeat_Button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_repeat_front));
		}
	};
	
	/**复勘Button事件*/
	OnClickListener mRepeatButtonOnClickListener= new OnClickListener() {
		@Override
		public void onClick(View v) {
			mSurveyRepeatBoolean = false;
			// TODO Auto-generated method stub
			mSurvey_Button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_survey_front));
			mRepeat_Button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_repeat_back));
		}
	};
	
	OnClickListener mCreateOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final ContentValues cv = new ContentValues();
			String casenu_str = mCasenu_edit.getText().toString();
			if(casenu_str.length() >= 25){
				Dialog.negativeDialog(mContext, "\""+getString(R.string.createcase_casenu)+"\"" + getString(R.string.createcase_outstrip));
				return;
			}
			String carnu_str = mCarnu_edit.getText().toString();
			if(carnu_str.length() >= 25){
				Dialog.negativeDialog(mContext, "\""+getString(R.string.createcase_carnu) +"\""+ getString(R.string.createcase_outstrip));
				return;
			}
			String owner_str = mOwner_edit.getText().toString();
			if(owner_str.length() >= 25){
				Dialog.negativeDialog(mContext, "\""+getString(R.string.createcase_owner) +"\""+ getString(R.string.createcase_outstrip));
				return;
			}
			String carDriver_str = mCarDriver_edit.getText().toString();
			if(carDriver_str.length() >= 25){
				Dialog.negativeDialog(mContext, "\""+getString(R.string.createcase_pilot) +"\""+ getString(R.string.createcase_outstrip));
				return;
			}
			String phone_str = mPhone_edit.getText().toString();
			if(phone_str.length() >= 25){
				Dialog.negativeDialog(mContext,"\""+ getString(R.string.createcase_phone) +"\"" + getString(R.string.createcase_outstrip));
				return;
			}
			String address_str = mAddress_edit.getText().toString();
			if(address_str.length() >= 25){
				Dialog.negativeDialog(mContext, "\""+getString(R.string.createcase_address) +"\""+ getString(R.string.createcase_outstrip));
				return;
			}
			
			String taskType; 
			if(mUserClass.equals(Main.OVERHAUL)){
				taskType = mContext.getResources().getString(R.string.tasktype_overhaul);
				cv.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_ACCEPT);
			}else {
				if(mSurveyRepeatBoolean){
					taskType =mContext.getResources().getString(R.string.tasktype_survey);
				}else{
					taskType = mContext.getResources().getString(R.string.tasktype_repeat);
				}
				cv.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_NO_ASSIGN);
			}

			cv.put(DBModle.Task.CaseNo, casenu_str );
			cv.put(DBModle.Task.CarMark, carnu_str);
			cv.put(DBModle.Task.CarDriver, carDriver_str);
			cv.put(DBModle.Task.CarOwner, owner_str);
			cv.put(DBModle.Task.Telephone, phone_str);
			cv.put(DBModle.Task.Address,address_str);
			cv.put(DBModle.Task.TaskType,taskType);
			cv.put(DBModle.Task.FrontOperator,mUserName);
			cv.put(DBModle.Task.BackState, DBModle.TaskLog.FrontState_NO_ASSIGN);
			cv.put(DBModle.Task.BackOperator, "");
			//经纬度
			String latitude= BaiduLocation.instance().LOCAITON_LATITUDE;
			String longitude = BaiduLocation.instance().LOCAITON_LONITUDE;
			cv.put(DBModle.Task.Latitude, latitude);
			cv.put(DBModle.Task.Longitude, longitude);
			
			cv.put(DBModle.Task.Watcher, "");
			cv.put(Task.FrontPrice, "0");
			cv.put(Task.BackPrice, "0");
			cv.put(Task.FixedPrice, "0");
			cv.put(DBModle.Task.Memo, "");
			cv.put(DBModle.Task.StateUpdateTime, getCurrentTime());
			cv.put(DBModle.Task.AccidentTime, getCurrentTime());
			cv.put(DBModle.Task.IsNew, "0");
			cv.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION);
			cv.put(DBModle.Task.GarageID, "-1");
			cv.put(DBModle.Task.LinkCaseNo, casenu_str);
			
			if(casenu_str.equals("") || carnu_str.equals("")){
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.createcase_yuandong_mess));
			}else{
				if(DBOperator.createTask(cv)){
					Intent intent = new Intent();
					setResult(Main.FrontState_CREATE, intent);
					finish();
				}else {
					Dialog.negativeDialog(mContext,  mContext.getResources().getString(R.string.createcase_fail));
				}
			}
			
		}
	};
	
	
	/**
	 * 获取当前时间 返回 MM-dd mm:ss 格式
	 * 
	 * @return 返回 MM-dd mm:ss 格式当前时间字符串
	 * */
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTime(){
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = date.format(Calendar.getInstance().getTime());
		String month = time.substring(5,10);
		String hour = time.substring(11,16);
		String accidentTime = month +"\u0020\u0020"+hour;
		return accidentTime;
	}
}
