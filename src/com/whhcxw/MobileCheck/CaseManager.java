package com.whhcxw.MobileCheck;

import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.Activity;
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

public class CaseManager extends BaseActivity {

	private Titlebar2 titlebar;

	private Context mContext;
	/** 车牌号 */
	private TextView mCar_TextView;
	/** 案件号 */
	private TextView mCasenumber_TextView;
//	/** 查勘员 */
//	private TextView mSurvey_TextView;
//	/** 审查员 */
//	private TextView mVerify_TextView;
	/** 车主 */
	private TextView mCarmaster_TextView;
	/** 联系电话 */
	private TextView mPhone_TextView;
	/** 案发时间 */
	private TextView mTime_TextView;
	/** 案发地点 */
	private TextView mAddress_TextView;
	
	private String mUserPhone;

	private Button mReceive_Button;
	private Button mRefuse_Button;
	private Button mUserphone_Button;

	private ContentValues mContentValues;
	
	private String mTID;
	private String mLongitude;
	private String mLatitude;
	
	
	private Button mSanzheReceive;
	private Button mBiaodiReceive;
	
	
	public static final int CASESTATE = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_casemanage);
		mContext = this;

		initTitle();
		initview();
	}

	public void initTitle() {
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);

	}

	private void initview() {
			Intent intent = getIntent();
		
			mTID= intent.getStringExtra("TID");
			mContentValues = DBOperator.getTask(mTID);
			try {
				mLongitude = mContentValues.get(DBModle.Task.Longitude).toString();
				mLatitude = mContentValues.get(DBModle.Task.Latitude).toString();
			} catch (Exception e) {
				// TODO: handle exception
				mLongitude = "o";
				mLatitude = "0";
			}
			
			mUserPhone = mContentValues.get(DBModle.Task.Telephone).toString();
			
			mCar_TextView = (TextView) this.findViewById(R.id.car);
			mCar_TextView.setText(mContentValues.get(DBModle.Task.CarMark).toString());
			mCasenumber_TextView = (TextView) this.findViewById(R.id.casenumber);
			mCasenumber_TextView.setText(mContentValues.get(DBModle.Task.CaseNo).toString());
//			mSurvey_TextView = (TextView) this.findViewById(R.id.survey);
//			mSurvey_TextView.setText(mContentValues.get("survey").toString());
//			mVerify_TextView = (TextView) this.findViewById(R.id.verify);
//			mVerify_TextView.setText(mContentValues.get("verify").toString());
			mCarmaster_TextView = (TextView) this.findViewById(R.id.carmaster);
			mCarmaster_TextView.setText(mContentValues.get(DBModle.Task.CarOwner).toString());
			mPhone_TextView = (TextView) this.findViewById(R.id.phone);
			mPhone_TextView.setText(mContentValues.get(DBModle.Task.Telephone).toString());
			mTime_TextView = (TextView) this.findViewById(R.id.time);
			mTime_TextView.setText(mContentValues.get(DBModle.Task.AccidentTime).toString());
			mAddress_TextView = (TextView) this.findViewById(R.id.address);
			mAddress_TextView.setText(mContentValues.get(DBModle.Task.Address).toString());
		

			mReceive_Button = (Button) this.findViewById(R.id.receive);
			mReceive_Button.setOnClickListener(receiveOnClickListener);
			mReceive_Button.setTag("receive");
			mRefuse_Button = (Button) this.findViewById(R.id.refuse);
			mRefuse_Button.setOnClickListener(refuseOnClickListener);
			mUserphone_Button = (Button) this.findViewById(R.id.userphone);
			mUserphone_Button.setOnClickListener(userphoneOnClickListener);

			
			mSanzheReceive = (Button)this.findViewById(R.id.sanzhereceive);
			mSanzheReceive.setOnClickListener(receiveOnClickListener);
			mSanzheReceive.setTag("sanzhe");
			
			mBiaodiReceive = (Button)this.findViewById(R.id.biaodireceive);
			mBiaodiReceive.setOnClickListener(receiveOnClickListener);
			mBiaodiReceive.setTag("biaodi");
			
//			String recieves = UserManager.getInstance().getRoleTypes();
//			if(recieves.contains(mContext.getResources().getString(R.string.RoleType_M006_name))){
//				mSanzheReceive.setVisibility(View.VISIBLE);
//				mBiaodiReceive.setVisibility(View.VISIBLE);
//			}else {
				mReceive_Button.setVisibility(View.VISIBLE);
				mRefuse_Button.setVisibility(View.VISIBLE);
				
//			}
			
	}

	/**接受任务*/
	OnClickListener receiveOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String tag = (String) v.getTag();
			ContentValues updateValues = new ContentValues();
			updateValues.put(DBModle.Task.IsNew,"0");
			if(tag.equals("sanzhe")){
				updateValues.put(DBModle.Task.CaseNo, mContentValues.getAsString(DBModle.Task.CaseNo)+"-"+getString(R.string.casemanage_sanzhe));
				updateValues.put(DBModle.Task.LinkCaseNo, mContentValues.getAsString(DBModle.Task.CaseNo)+"-"+getString(R.string.casemanage_sanzhe));
			} else if(tag.equals("biaodi")){
				updateValues.put(DBModle.Task.CaseNo, mContentValues.getAsString(DBModle.Task.CaseNo)+"-"+getString(R.string.casemanage_biaodi));
				updateValues.put(DBModle.Task.LinkCaseNo, mContentValues.getAsString(DBModle.Task.CaseNo)+"-"+getString(R.string.casemanage_biaodi));
			}
			
			boolean bIsNewUpdate = DBOperator.updateTask(mTID, updateValues);
			String stage = mContentValues.get(DBModle.Task.FrontState).toString();
			stage = DBModle.TaskLog.FrontState_ACCEPT;
			boolean b = DBOperator.updateTaskSate(mTID, stage, mLongitude, mLatitude);
			
			UploadWork.sendDataChangeBroadcast(mContext);
			if(b){
				Main.DATA_CHEANGE = true;
				Intent intent = new Intent(mContext,CaseDetails2.class);
				intent.putExtra("TID", mContentValues.get(DBModle.Task.TID).toString());
				startActivityForResult(intent,CASESTATE);
//				startActivity(intent);
//				finish();
			}else{
//				G.showToast(mContext, "接收任务失败",false);
				
				Dialog.negativeDialog(mContext, "接收任务失败");
			}
			
		}
	};
	/**拒绝任务*/
	OnClickListener refuseOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			boolean b = DBOperator.refuseTask(mTID, mLongitude, mLatitude);
			if(b){
				UploadWork.sendDataChangeBroadcast(mContext);
				Dialog.positiveDialog(mContext, mContext.getResources().getString(R.string.casemanage_dialog_refuse_success),new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub						
						Intent intent = new Intent();
						setResult(CASESTATE, intent);
						finish();
					}
				});
				
				
			} else {
				Dialog.positiveDialog(mContext, mContext.getResources().getString(R.string.casemanage_dialog_refuse_fail),new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						finish();
					}
				});
			}
			
		}
	};
	/**用户电话*/
	OnClickListener userphoneOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Dialog.telUserDialog(mContext, mUserPhone);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		switch (requestCode) {
		case CASESTATE:
			Intent intent = new Intent();
			setResult(CASESTATE, intent);
			finish();
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	

}
