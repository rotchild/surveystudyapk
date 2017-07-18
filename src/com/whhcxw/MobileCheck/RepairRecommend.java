package com.whhcxw.MobileCheck;

import java.math.BigDecimal;
import java.util.ArrayList;

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

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

public class RepairRecommend extends BaseActivity{

	private final String TAG = this.getClass().getName();
	
	private Context mContext;
	private String mTID;
	private ContentValues mContentValues ;
	
	private String mCaseNO;
	
	private Button mCooperation_button;
	private Button mNoCooperation_button;
	private View mFactory_layout;
	private View mCartype_layou;
	private ContentValues mCartype_Values;
	private ContentValues mGarages_Values;
	private TextView mAlery_cartype_view;
	private TextView mAlery_garages_view;
	private View mAlery_garagesaddress_layout;
	private TextView mGarages_phone;
	private TextView mGarages_address;
	private ArrayList<ContentValues> mCartype_List;
	private ArrayList<ContentValues> mGarages_List;
	private Button mSuer_Button;
	private String mCarTypeID;
	private String mGaragesID;
	private String mNoCooperationGaragesID;
	private String mGaragesUserName;
	private String mNoCooperationGaragesUserName;
	private String mUpdateCarTypeName;
	private String mUpDataGaragetName;
	/**true 修改  false 创建*/
	private boolean mUpdate;
	
	private View mNoCooperation_layout;
	
	private View mCooperation_layout;
	
	private View mOrganization_layou;
	private TextView mAlery_organization_view;
	
	private View mEnterprise_layou;
	private TextView mAlery_enterprise_view;
	
	private View mCategory_layou;
	private TextView mAlery_category_view;
	
	private View mCooperation_alery_factory_layout;
	
	private ArrayList<ContentValues> mOrganization_List;
	private ArrayList<ContentValues> mNoOrganization_List;
	private ContentValues mOrganization_Values;
	private ContentValues mNoOrganization_Values;
	
	private int mEnterprisechoices_checked = -1;
	private int mCategorychoices_checked = -1;
	
	private String[] mCategorychoices;
	private String[] mEnterprisechoices;
	
	/** true:车行保单   false:非车行保单 */
	private boolean isCooperation = true;
	
	private TextView mAlery_cooperation_garages_view;
	private View mAlery_cooperation_garagesaddress_layout;
	private TextView mCooperation_garages_phone;
	private TextView mCooperation_garages_address;
	
	
	private boolean mUpdateIsCooperation;
	
	private View mNo_organization_layou;
	private TextView mAlery_no_organization_view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.repairrcommend);
		mContext = this;
		
		mCartype_List = new ArrayList<ContentValues>();
		mGarages_List = new ArrayList<ContentValues>();
		initTitle();
	}                                                               
	
	public void initTitle() {
		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);
		titlebar.setRightText(R.string.title_positive);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mCategorychoices = new String[]{mContext.getResources().getString(R.string.repairrecommend_no4s) ,mContext.getResources().getString(R.string.repairrecommend_4s)};
		mEnterprisechoices = new String[]{mContext.getResources().getString(R.string.repairrecommend_nocollaborate),mContext.getResources().getString(R.string.repairrecommend_collaborate)};
		
		mOrganization_List = new ArrayList<ContentValues>();
		mNoOrganization_List = new ArrayList<ContentValues>();
		
		Intent intent = getIntent();
		mTID = intent.getStringExtra("TID");
		mContentValues = DBOperator.getTask(mTID);
		mCaseNO = mContentValues.getAsString(DBModle.Task.CaseNo);
		mUpdate = intent.getBooleanExtra("update", false);
		isCooperation = intent.getBooleanExtra("updateisCooperation", true);

		int cooperationStr = mContentValues.getAsInteger(DBModle.Task.IsCooperation);
		if(cooperationStr == DBModle.TaskLog.COOPERATION_YES){
			isCooperation = true;
		}else if(cooperationStr == DBModle.TaskLog.COOPERATION_NO){
			isCooperation = false;
		}
		
		mUpdateIsCooperation = isCooperation;
		
		mCooperation_button = (Button)this.findViewById(R.id.cooperation_button);
		mCooperation_button.setOnClickListener(mCooperationButtonOnClickListener);
		mNoCooperation_button = (Button)this.findViewById(R.id.nocooperation_button);
		mNoCooperation_button.setOnClickListener(mNOCooperationButtonOnClickListener);
		
		mFactory_layout = (View)this.findViewById(R.id.factory_layout);
		mFactory_layout.setOnClickListener(mFactoryLayoutButtonOnClickListener);
		
		mCartype_layou = (View)this.findViewById(R.id.cartype_layou);
		mCartype_layou.setOnClickListener(mCartypeLayouButtonOnClickListener);
		
		mAlery_cartype_view = (TextView)this.findViewById(R.id.alery_cartype_view);
		
		mAlery_garages_view = (TextView)this.findViewById(R.id.alery_garages_view);
		mAlery_garagesaddress_layout = (View)this.findViewById(R.id.alery_garagesaddress_layout);

		mGarages_phone = (TextView)this.findViewById(R.id.garages_phone);
		mGarages_address = (TextView)this.findViewById(R.id.garages_address);
		mSuer_Button = (Button)this.findViewById(R.id.button);
		mSuer_Button.setOnClickListener(mSuer_ButtonOnClickListener);
		
		mCooperation_layout = (View)this.findViewById(R.id.cooperation_layout);
		mNoCooperation_layout = (View)this.findViewById(R.id.nocooperation_layout);
		
		mOrganization_layou = (View)this.findViewById(R.id.organization_layou);
		mOrganization_layou.setOnClickListener(mOrganizationLayouOnClickListener);
		mAlery_organization_view = (TextView)this.findViewById(R.id.alery_organization_view);
		
		
		mNo_organization_layou = (View)this.findViewById(R.id.no_organization_layou);
		mNo_organization_layou.setOnClickListener(mNoOrganizationLayouOnClickListener);
		mAlery_no_organization_view = (TextView)this.findViewById(R.id.alery_no_organization_view);
		
		
		
		mEnterprise_layou = (View)this.findViewById(R.id.enterprise_layou);
		mEnterprise_layou.setOnClickListener(mEnterpriseLayouOnClickListener);
		mAlery_enterprise_view = (TextView)this.findViewById(R.id.alery_enterprise_view);
		
		mCategory_layou = (View)this.findViewById(R.id.category_layou);
		mCategory_layou.setOnClickListener(mCategoryLayouOnClickListener);
		mAlery_category_view = (TextView)this.findViewById(R.id.alery_category_view);
		
		mCooperation_alery_factory_layout = (View)this.findViewById(R.id.cooperation_alery_factory_layout);
		mCooperation_alery_factory_layout.setOnClickListener(mCooperationAleryFactoryLayoutOnClickListener);
		
		mAlery_cooperation_garages_view = (TextView)this.findViewById(R.id.alery_cooperation_garages_view);
		mAlery_cooperation_garagesaddress_layout = (View)this.findViewById(R.id.alery_cooperation_garagesaddress_layout);
		mCooperation_garages_phone = (TextView)this.findViewById(R.id.cooperation_garages_phone);
		mCooperation_garages_address = (TextView)this.findViewById(R.id.cooperation_garages_address);
		
		if(!isCooperation){
			showNoCooperation();
		} else {
			showCooperation();
		}
		
		if(cooperationStr == DBModle.TaskLog.COOPERATION_YES){
			String garageID = mContentValues.getAsString(DBModle.Task.GarageID);
			ContentValues garageValues = DBOperator.getGarage(garageID);
			if(garageValues != null){
				String areaID = garageValues.getAsString(DBModle.Garage.AreaID);
				ContentValues areaValues = DBOperator.getArea(areaID);
				if(areaValues != null){
					mAlery_organization_view.setText(areaValues.getAsString(DBModle.Areas.AreaName));		
					mOrganization_Values = areaValues;
				}
				
				int category = garageValues.getAsInteger(DBModle.Garage.Is4s);
				mAlery_category_view.setText(mCategorychoices[category]);
				mCategorychoices_checked = category;
				
				int enterprise = garageValues.getAsInteger(DBModle.Garage.IsPartner);
				mAlery_enterprise_view.setText(mEnterprisechoices[enterprise]);
				mEnterprisechoices_checked = enterprise;
				
				
				
				String garagename = garageValues.getAsString(DBModle.Garage.ShortName);
				if(garagename.equals("")){
					garagename = garageValues.getAsString(DBModle.Garage.FullName);
				}
				
				mAlery_cooperation_garages_view.setText(garagename);
				mAlery_cooperation_garagesaddress_layout.setVisibility(View.VISIBLE);
				mCooperation_garages_phone.setText(garageValues.getAsString(DBModle.Garage.Telephone));
				mCooperation_garages_address.setText(garageValues.getAsString(DBModle.Garage.Address));
				
				mGarages_Values = garageValues ;
				mGaragesID = garageID ;
				mGaragesUserName = mGarages_Values.getAsString(DBModle.Garage.UserName);
			}
		} else if(cooperationStr == DBModle.TaskLog.COOPERATION_NO){
			String AreaID=UserManager.getInstance().getAreaID();
			ContentValues areaContentValues = DBOperator.getArea(AreaID);
			if(areaContentValues != null){
				mAlery_no_organization_view.setText(areaContentValues.getAsString(DBModle.Areas.AreaName));		
				mOrganization_Values = areaContentValues;
			}
			
		} else {
			String AreaID=UserManager.getInstance().getAreaID();
			ContentValues areaContentValues = DBOperator.getArea(AreaID);
			if(areaContentValues != null){
				mAlery_organization_view.setText(areaContentValues.getAsString(DBModle.Areas.AreaName));		
				mOrganization_Values = areaContentValues;
				
				mAlery_category_view.setText(mCategorychoices[0]);
				mCategorychoices_checked = 0;
				
				mAlery_enterprise_view.setText(mEnterprisechoices[1]);
				mEnterprisechoices_checked = 1;
			}
		}
		
		getRepairMess();
	}
	
	/**当是更换操作时，加载推修的信息*/
	public void getRepairMess(){
		if(isCooperation){
			if(mUpdate){
				ContentValues garageValues = DBOperator.getGarage(mContentValues.getAsString(DBModle.Task.GarageID));
				ContentValues areaValues = DBOperator.getArea(garageValues.getAsString(DBModle.Garage.AreaID));
				mAlery_organization_view.setText(areaValues.getAsString(DBModle.Areas.AreaName));
				String categorychoices = garageValues.getAsString(DBModle.Garage.Is4s);
				if(categorychoices.equals("0")){
					mAlery_category_view.setText(mCategorychoices[0]);
					mCategorychoices_checked = 0;
				}else {
					mAlery_category_view.setText(mCategorychoices[1]);
					mCategorychoices_checked = 1;
				}
				String enterprisechoices = garageValues.getAsString(DBModle.Garage.IsPartner);
				if(enterprisechoices.equals("0")){
					mAlery_enterprise_view.setText(mEnterprisechoices[0]);
					mEnterprisechoices_checked = 0;
				} else {
					mAlery_enterprise_view.setText(mEnterprisechoices[1]);
					mEnterprisechoices_checked = 1;
				}
				
				String garagename = garageValues.getAsString(DBModle.Garage.ShortName);
				if(garagename.equals("")){
					garagename = garageValues.getAsString(DBModle.Garage.FullName);
				}
				
				mAlery_cooperation_garages_view.setText(garagename);
				mAlery_cooperation_garagesaddress_layout.setVisibility(View.VISIBLE);
				mCooperation_garages_phone.setText(garageValues.getAsString(DBModle.Garage.Telephone));
				mCooperation_garages_address.setText(garageValues.getAsString(DBModle.Garage.Address));
				
				//当更换维修厂时，赋默认值
				mGarages_Values = garageValues;
				mGaragesID = mGarages_Values.getAsString(DBModle.Garage.GarageID);
				mGaragesUserName = mGarages_Values.getAsString(DBModle.Garage.UserName);
			}
		}else {
			if(mUpdate){
				ContentValues carTypeValues = DBOperator.getCarType(mContentValues.getAsString(DBModle.Task.CarTypeID));
				
				mUpdateCarTypeName = carTypeValues.getAsString(DBModle.CarType.TypeName);
				mAlery_cartype_view.setText(mUpdateCarTypeName);
				
				ContentValues garageValues = DBOperator.getGarage(mContentValues.getAsString(DBModle.Task.GarageID));	
				
				ContentValues noOrganization_Values =DBOperator.getArea(garageValues.getAsString(DBModle.Garage.AreaID));
				mNoOrganization_Values = noOrganization_Values;
				mAlery_no_organization_view.setText(noOrganization_Values.getAsString(DBModle.Areas.AreaName));
				
				String garagename = garageValues.getAsString(DBModle.Garage.ShortName);
				if(garagename.equals("")){
					garagename = garageValues.getAsString(DBModle.Garage.FullName);
				}
				
				
				mUpDataGaragetName = garagename;
				mAlery_garages_view.setText(mUpDataGaragetName);
				mAlery_garagesaddress_layout.setVisibility(View.VISIBLE);
				mGarages_phone.setText(garageValues.getAsString(DBModle.Garage.Telephone));
				mGarages_address.setText(garageValues.getAsString(DBModle.Garage.Address));
				
				mCartype_Values = carTypeValues;
				mGarages_Values = garageValues;
				mNoCooperationGaragesID = garageValues.getAsString(DBModle.Garage.GarageID);
				mNoCooperationGaragesUserName = garageValues.getAsString(DBModle.Garage.UserName);
				
				mCarTypeID = carTypeValues.getAsString(DBModle.CarType.CarTypeID);
			}		
		}
	}
	
	
	/**推修确定*/
	OnClickListener mSuer_ButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isCooperation){
				if(mGarages_Values == null || mGaragesID == null){
					Dialog.negativeDialog(mContext, getString(R.string.repairrecommend_dialog_cooperation_garages_mess));
					return;
				}
			}else {
				if(mCartype_Values == null || mGarages_Values == null || mNoCooperationGaragesID == null){
					Dialog.negativeDialog(mContext, getString(R.string.repairrecommend_dialogmess));
					return;
				}
			}
			
			if(mUpdate){
				final EditText editText = new EditText(mContext);
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				Dialog.edittextDialog(mContext, mContext.getResources().getString(R.string.dialog_titlemess), editText, new AlertDialog.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String pushPrice = editText.getText().toString();
						if(pushPrice.equals("")){
							Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_nomomey));
							return;
						}
						
						float f = Float.parseFloat(pushPrice);
						BigDecimal bigDecimal = new BigDecimal(f);
						float f2 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						pushPrice = String.valueOf(f2);
						
						
						ContentValues contentValues = DBOperator.getCooperationGarageCase(mTID);
						if(contentValues == null){
							contentValues = DBOperator.getGarageCase(mTID);
						}else {
							if(isCooperation){
								mCarTypeID = null;
							}
						}
						float a = Float.parseFloat(contentValues.getAsString(DBModle.Task.FrontPrice));
						if(isCooperation){
							mNoCooperationGaragesID = "-1";	
						}else {
							mGaragesID = "-1";
						}
						
						if((contentValues.getAsString(DBModle.Task.GarageID).equals(mGaragesID) && a == Float.parseFloat(pushPrice))||(contentValues.getAsString(DBModle.Task.GarageID).equals(mNoCooperationGaragesID) && a == Float.parseFloat(pushPrice))){
							if(!mUpdateIsCooperation){
								if(mUpdateIsCooperation == isCooperation && contentValues.getAsString(DBModle.Task.CarTypeID).equals(mCarTypeID)){
									Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.repairrecommend_noupdate));
									return;
								}
							} else {
								Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.repairrecommend_noupdate));
								return;
							}
						}
						
						String message = mContentValues.getAsString(DBModle.Task.Memo);
						
						ContentValues taskValues=new ContentValues();
						taskValues.put(DBModle.TaskLog.StateUpdateTime, G.getPhoneCurrentTime());
						boolean b = DBOperator.updateTaskLog(mTID,taskValues,mContentValues.getAsString(DBModle.Task.FrontState));
						Log.d(TAG, "Update TaskLog StateUpdateTime:"+b);
						
						if(isCooperation){
							if(DBOperator.updateGarageTask(mTID, mGaragesID, mCarTypeID, pushPrice, mGaragesUserName, "1" ,message)){
								UploadWork.sendDataChangeBroadcast(mContext);
									
								finish();
							}else{
								Dialog.negativeDialog(mContext,  mContext.getResources().getString(R.string.repairrecommend_updatefail));
							}
						}else {
							if(DBOperator.updateGarageTask(mTID, mNoCooperationGaragesID , mCarTypeID, pushPrice, mNoCooperationGaragesUserName, "1" ,message)){
								UploadWork.sendDataChangeBroadcast(mContext);
									
								finish();
							}else{
								Dialog.negativeDialog(mContext,  mContext.getResources().getString(R.string.repairrecommend_updatefail));
							}
						}
					}
				});
			}else{

				String estprice="0";
				ContentValues repeatvalues;
				ContentValues surveyvalues = DBOperator.getTask(mCaseNO, mContext.getResources().getString(R.string.tasktype_survey));
				if(surveyvalues == null){
					repeatvalues = DBOperator.getTask(mCaseNO, mContext.getResources().getString(R.string.tasktype_survey));
					if(repeatvalues != null){
						estprice = mContentValues.getAsString(DBModle.Task.FrontPrice);	
					}
				}else{
					estprice = mContentValues.getAsString(DBModle.Task.FrontPrice);
				}
					
			if(estprice.equals("0")){
				final EditText editText = new EditText(mContext);
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				Dialog.edittextDialog(mContext, mContext.getResources().getString(R.string.dialog_titlemess),editText,new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String caseMoey = editText.getText().toString();
						if(caseMoey.equals("")|| caseMoey == "0"){
							Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_nomomey));
						}else{
							//创建推修
							mContentValues.put(DBModle.Task.TaskType , getString(R.string.tasktype_recommend));
							mContentValues.put(DBModle.Task.StateUpdateTime , CreateCase.getCurrentTime());
							mContentValues.put(DBModle.Task.AccidentTime , CreateCase.getCurrentTime());
							
							mContentValues.put(DBModle.Task.CarTypeID, mCarTypeID);
							if(isCooperation){
								mContentValues.put(DBModle.Task.GarageID, mGaragesID);
								mContentValues.put(DBModle.Task.BackOperator, mGaragesUserName);
							}else {
								mContentValues.put(DBModle.Task.GarageID, mNoCooperationGaragesID);
								mContentValues.put(DBModle.Task.BackOperator, mNoCooperationGaragesUserName);                    
							}
							
							float f = Float.parseFloat(caseMoey);
							BigDecimal bigDecimal = new BigDecimal(f);
							float f2 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

							mContentValues.put(DBModle.Task.FrontPrice, f2);
							mContentValues.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_START);
							
							mContentValues.put(DBModle.Task.BackState, DBModle.TaskLog.FrontState_START);
							mContentValues.remove(DBModle.Task.TID);
							
							if(DBOperator.createGarageTask(mContentValues, mContentValues.getAsString(DBModle.Task.BackOperator), "1",mContentValues.getAsString(DBModle.Task.BackOperator))){
								UploadWork.sendDataChangeBroadcast(mContext);
								ContentValues values = DBOperator.getTask(mContentValues.get(DBModle.Task.CaseNo).toString(), mContentValues.get(DBModle.Task.TaskType).toString());
								String TID = values.get(DBModle.Task.TID).toString();

								Bundle bundle = new Bundle();
								bundle.putString("TID", TID);
								bundle.putString("CarTypeID", mCarTypeID);
								if(isCooperation){
									mContentValues.put(DBModle.Task.GarageID, mGaragesID);
								}else {
									mContentValues.put(DBModle.Task.GarageID, mNoCooperationGaragesID);
								}
								bundle.putBoolean("isCooperation", isCooperation);
								Intent intent = new Intent(mContext,RepairState.class);
								intent.putExtras(bundle);
//								startActivityForResult(intent, CaseManager.CASESTATE);
								startActivity(intent);
								finish();
										
							}else{
								Dialog.negativeDialog(mContext,  mContext.getResources().getString(R.string.repairrecommend_fail));
							}	
						}
					}
				});
			}
			}
		}
	};
	
	/**车行保单*/
	OnClickListener mCooperationButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showCooperation();
		}
	};
	/**显示车行保单*/
	private void showCooperation(){
		isCooperation = true;
		mCooperation_layout.setVisibility(View.VISIBLE);
		mNoCooperation_layout.setVisibility(View.GONE);
		mCooperation_button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_survey_back));
		mNoCooperation_button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_repeat_front));
		
	}
	/**非车行保单*/
	OnClickListener mNOCooperationButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showNoCooperation();	
		}
	};
	
	/**显示非车行保单*/
	private void showNoCooperation(){
		isCooperation = false;
		mCooperation_layout.setVisibility(View.GONE);
		mNoCooperation_layout.setVisibility(View.VISIBLE);
		mCooperation_button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_survey_front));
		mNoCooperation_button.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.createcase_repeat_back));
	
	}
	
	
	/**品牌选择*/
	OnClickListener mCartypeLayouButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext ,RepairCarType.class);
			intent.putExtra("cartypedata", mCartype_List);
			startActivityForResult(intent, 0);
						
		}
	};
	
	/**非车行保单维修厂选择*/
	OnClickListener mFactoryLayoutButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if((mOrganization_Values != null && mCartype_Values != null) || ( mCartype_Values != null && mNoOrganization_Values != null)){
				Intent intent = new Intent(mContext ,RepairGarages.class);
				intent.putExtra("cartype", mCartype_Values.getAsString(DBModle.CarType.CarTypeID));
				intent.putExtra("mGarages_data", mGarages_List);
				intent.putExtra("isCooperation", isCooperation);
				if(!isCooperation){
					intent.putExtra("areaid", mNoOrganization_Values.getAsInteger(DBModle.Areas.AreaID));
				}else {
					intent.putExtra("areaid",mOrganization_Values.getAsInteger(DBModle.Areas.AreaID));
				}
				startActivityForResult(intent, 0);
			}else {
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.repairrecommend_dialog_garages_mess));
			}
			
			
		}
	};
	
	
	/**车行保单--出险机构*/
	OnClickListener mOrganizationLayouOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext ,RepairOrganization.class);
			intent.putExtra("organizationdata", mOrganization_List);
			intent.putExtra("isCooperation", true);
			startActivityForResult(intent, 0);
			
		}
	};
	

	/**非车行保单--出险机构*/
	OnClickListener mNoOrganizationLayouOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext ,RepairOrganization.class);
			intent.putExtra("organizationdata", mNoOrganization_List);
			intent.putExtra("isCooperation", false);
			startActivityForResult(intent, 0);
			
		}
	};
	
	
	
	/**是否是合作单位*/
	OnClickListener mEnterpriseLayouOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Dialog.checkButtonDialog(mContext, mContext.getResources().getString(R.string.repairrecommend_collaborate_choice), mEnterprisechoices , mEnterprisechoices_checked , new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(mEnterprisechoices_checked != which){
						mAlery_category_view.setText("");
						mAlery_cooperation_garages_view.setText("");
						mCooperation_garages_phone.setText("");
						mCooperation_garages_address.setText("");
						mAlery_cooperation_garagesaddress_layout.setVisibility(View.GONE);
						
						mCategorychoices_checked = -1;
						
						mGaragesID = null;
						mGaragesUserName = null;
						
					}
					
					mEnterprisechoices_checked = which;
					mAlery_enterprise_view.setText(mEnterprisechoices[which]);
					dialog.dismiss();
					
				}
			});
		}
	};
	
	/**是否是4S店*/
	OnClickListener mCategoryLayouOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Dialog.checkButtonDialog(mContext, mContext.getResources().getString(R.string.repairrecommend_4s_choice), mCategorychoices , mCategorychoices_checked , new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(mCategorychoices_checked != which){
						mAlery_cooperation_garages_view.setText("");
						mCooperation_garages_phone.setText("");
						mCooperation_garages_address.setText("");
						mAlery_cooperation_garagesaddress_layout.setVisibility(View.GONE);
						
						mGaragesID = null;
						mGaragesUserName = null;
					}
					
					mCategorychoices_checked = which;
					mAlery_category_view.setText(mCategorychoices[which]);
					dialog.dismiss();
				}
			});
		}
	};
	
	
	/**车行保单维修厂选择*/
	OnClickListener mCooperationAleryFactoryLayoutOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			if(mEnterprisechoices_checked == -1 || mCategorychoices_checked == -1 || mOrganization_Values == null){
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.repairrecommend_dialog_cooperation_garages_mess));
				return;
			}
		
			mGarages_List = new ArrayList<ContentValues>();
			
			Intent intent = new Intent(mContext ,RepairGarages.class);
			intent.putExtra("isEnterprisechoices", mEnterprisechoices_checked);
			intent.putExtra("isCategorychoices", mCategorychoices_checked);
			intent.putExtra("areaid", mOrganization_Values.getAsInteger(DBModle.Areas.AreaID));
			intent.putExtra("mGarages_data", mGarages_List);
			intent.putExtra("isCooperation", isCooperation);
			startActivityForResult(intent, 0);
		}
	};
	

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (resultCode) {
		case RepairCarType.CARTYPE:
			mCartype_List = (ArrayList<ContentValues>) data.getSerializableExtra("cartypedata");
			mCartype_Values = data.getParcelableExtra("cartype");
			mAlery_cartype_view.setText(mCartype_Values.getAsString(DBModle.CarType.TypeName));
			String cartypeID = mCartype_Values.getAsString(DBModle.CarType.CarTypeID);
			if(!cartypeID.equals(mCarTypeID)){
				mAlery_garages_view.setText("");
				mAlery_garagesaddress_layout.setVisibility(View.GONE);
				mGarages_phone.setText("");
				mGarages_address.setText("");
				mGarages_List = new ArrayList<ContentValues>();
			}
			mCarTypeID = cartypeID;
			mGarages_Values = null;
			
			break;
			
		case RepairGarages.GARAGES:
			mGarages_List =  (ArrayList<ContentValues>) data.getSerializableExtra("garagesdata");
			mGarages_Values = data.getParcelableExtra("garages");
			String garagename = mGarages_Values.getAsString(DBModle.Garage.ShortName);
			if(garagename.equals("")){
				garagename = mGarages_Values.getAsString(DBModle.Garage.FullName);
			}
			if(isCooperation){
				mAlery_cooperation_garages_view.setText(garagename);
				mAlery_cooperation_garagesaddress_layout.setVisibility(View.VISIBLE);
				mCooperation_garages_phone.setText(mGarages_Values.getAsString(DBModle.Garage.Telephone));
				mCooperation_garages_address.setText(mGarages_Values.getAsString(DBModle.Garage.Address));
				mGaragesID = mGarages_Values.getAsString(DBModle.Garage.GarageID);
				mGaragesUserName = mGarages_Values.getAsString(DBModle.Garage.UserName);
			}else {
				
				mAlery_garages_view.setText(garagename);
				mAlery_garagesaddress_layout.setVisibility(View.VISIBLE);
				mGarages_phone.setText(mGarages_Values.getAsString(DBModle.Garage.Telephone));
				mGarages_address.setText(mGarages_Values.getAsString(DBModle.Garage.Address));
				mNoCooperationGaragesID = mGarages_Values.getAsString(DBModle.Garage.GarageID);
				mNoCooperationGaragesUserName = mGarages_Values.getAsString(DBModle.Garage.UserName);
			}
			break;
			
		case RepairOrganization.ORGANIZATION:
			
			boolean isCooperation = data.getBooleanExtra("isCooperation", true);
			if(isCooperation){
				mOrganization_List = (ArrayList<ContentValues>) data.getSerializableExtra("organizationdata");
				mOrganization_Values = data.getParcelableExtra("organization");
				String areaName =  mOrganization_Values.getAsString(DBModle.Areas.AreaName);
				if(!areaName.equals(mAlery_organization_view.getText().toString())){
					mAlery_enterprise_view.setText("");
					mAlery_category_view.setText("");
					mAlery_cooperation_garages_view.setText("");
					mCooperation_garages_phone.setText("");
					mCooperation_garages_address.setText("");
					mAlery_cooperation_garagesaddress_layout.setVisibility(View.GONE);
					
					mEnterprisechoices_checked = -1;
					mCategorychoices_checked = -1;
					
					mGaragesID = null;
					mGaragesUserName = null;
				}
				mAlery_organization_view.setText(areaName);
			}else {
				mNoOrganization_List = (ArrayList<ContentValues>) data.getSerializableExtra("organizationdata");
				mNoOrganization_Values = data.getParcelableExtra("organization");
				String areaName =  mNoOrganization_Values.getAsString(DBModle.Areas.AreaName);
				if(!areaName.equals(mAlery_no_organization_view.getText().toString())){
					
					mAlery_cartype_view.setText("");
					
					mAlery_garages_view.setText("");
					mAlery_garagesaddress_layout.setVisibility(View.GONE);
					mGarages_phone.setText("");
					mGarages_address.setText("");
					
					mCartype_Values = null;
					mGarages_Values = null;
				}
				mAlery_no_organization_view.setText(areaName);
			}
			
			break;
			
		default:
			break;
		}
	}
}	

