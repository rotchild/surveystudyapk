
package com.whhcxw.MobileCheck;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DBModle.Task;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.ui.tableview.BasicItem;
import com.whhcxw.ui.tableview.IListItem;
import com.whhcxw.ui.tableview.UITableView;
import com.whhcxw.ui.tableview.UITableView.ClickListener;
import com.whhcxw.ui.tableview.ViewItem;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor")
public class CreateCase2 extends BaseActivity implements ClickListener{

	private static Context mContext;
	private Button mSurvey_Button;
	private Button mRepeat_Button;
	
	private String mUserName;
	
	/**查勘是true  复勘是false*/
	private boolean mSurveyRepeatBoolean=true;
	
	private Button mCreate_Button;
	
	private String mUserClass;
	
	private Titlebar2 titlebar;
	/**必填的项目*/
	private UITableView tableViewNotNull;
	/**选填的项目*/
	private UITableView tableViewOptional;
	/**默认的项目*/
	private UITableView tableViewDefault;
	
	private String mRecieves;
	
	private HashMap<String, IListItem> viewItemsMap;
	
	private static final int DEFAULT_TYPE = 0;
	private static final int NOT_NULL_TYPE = 1;
	private static final int OPTIONAL_TYPE = 2;
	
	private String selectedItem;
	private int mInsuranceId = -1;
	private String mAccidentType = "",PaymentMethod = "";
	private EditText history;//事故记录描述
	private EditText others;//备注
	
	private UserManager mUserManager ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.createcase2);
		mContext = this;
		
		initTitle();
	}
	
	public void initTitle() {
		initView();
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
//		titlebar.setRightBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		if(mUserClass.equals(Main.OVERHAUL)){
			titlebar.setCenterText(R.string.app_name_overhaul);
		}else{
			titlebar.setCenterText(R.string.app_name);
		} 
	}

	public void initView(){
		mUserManager = UserManager.getInstance();
		mUserName = mUserManager.getUserName();
		mUserClass = mUserManager.getUserClass();
		//是否显示任务类型选择
		LinearLayout task_select = (LinearLayout) findViewById(R.id.create_task_select_tasktype_linear);
		
		
		mSurvey_Button = (Button)this.findViewById(R.id.survey_button);
		mSurvey_Button.setOnClickListener(mSurveyButtonOnClickListener);
		mRepeat_Button = (Button)this.findViewById(R.id.repeat_button);
		mRepeat_Button.setOnClickListener(mRepeatButtonOnClickListener);
		
		history= (EditText) findViewById(R.id.createcase_default_optional_accident_history);
		others= (EditText) findViewById(R.id.createcase_default_optional_others);
		
		
		tableViewNotNull = (UITableView) findViewById(R.id.createcase_default_not_null);
		tableViewOptional = (UITableView) findViewById(R.id.createcase_default_optional);
		
		tableViewDefault = (UITableView) findViewById(R.id.createcase_default);
		
		tableViewDefault.setClickListener(this);
		tableViewNotNull.setClickListener(this);
		tableViewOptional.setClickListener(this);

		viewItemsMap = new HashMap<String, IListItem>();
		
		mRecieves = mUserManager.getRoleTypes();
		
		if(mRecieves.contains(this.getResources().getString(R.string.RoleType_M006_name))){
			String[] exceptItem  = new String[]{DBModle.Task.CarType,DBModle.Task.PaymentAmount,DBModle.Task.PaymentMethod,DBModle.Task.AccidentType};
			viewItemsMap.putAll(makeTableViews(tableViewOptional, makeItems(OPTIONAL_TYPE, exceptItem)));
			viewItemsMap.putAll(makeTableViews(tableViewNotNull, makeItems(NOT_NULL_TYPE,DBModle.Task.InsuranceID)));
		}else {
			task_select.setVisibility(View.GONE);
			
			tableViewOptional.setVisibility(View.GONE);
			if (mRecieves.contains(this.getResources().getString(R.string.RoleType_M007_name))) {
				String[] exceptItem  = new String[]{DBModle.Task.PaymentAmount,DBModle.Task.PaymentMethod};
				viewItemsMap.putAll(makeTableViews(tableViewOptional, makeItems(OPTIONAL_TYPE, exceptItem)));
			}else {
				viewItemsMap.putAll(makeTableViews(tableViewOptional, makeItems(OPTIONAL_TYPE, null)));
			}
			viewItemsMap.putAll(makeTableViews(tableViewNotNull, makeItems(NOT_NULL_TYPE,DBModle.Task.CaseNo)));
		}
		
	
		mCreate_Button = (Button)this.findViewById(R.id.create);
		mCreate_Button.setOnClickListener(mCreateOnClickListener);
		
		if(mUserClass.equals(Main.OVERHAUL)){
			mSurvey_Button.setVisibility(View.GONE);
			mRepeat_Button.setVisibility(View.GONE);
		}
		//设置事故类型默认值
//		mAccidentType = mContext.getResources().getStringArray(R.array.create_case_info_items_accident_type)[0];
		
		//默认保险公司
		ArrayList<ContentValues> list = DBOperator.getInsuranceList();
		if(list.size()==1){
			ContentValues insuranceIdValues = list.get(0);
			mInsuranceId = insuranceIdValues.getAsInteger(DBModle.Task.InsuranceID);
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
			
			String taskType; 
			if(mUserClass.equals(Main.OVERHAUL)){
				taskType = mContext.getResources().getString(R.string.tasktype_overhaul);
				cv.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_START);
			}else {
				if(mSurveyRepeatBoolean){
					taskType =mContext.getResources().getString(R.string.tasktype_survey);
				}else{
					taskType = mContext.getResources().getString(R.string.tasktype_repeat);
				}
				
				cv.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_NO_ASSIGN);
			}

			//江西的手动填写报案号，远东自动生成
			String CaseNo = "";
			if(mRecieves.contains(mContext.getResources().getString(R.string.RoleType_M006_name))){
				CaseNo = getEditText(DBModle.Task.CaseNo);
				if (!filtText(CaseNo, DBModle.Task.CaseNo)) {
					return;
				}
			}else {
				//自动生成报案号    用户ID + 时间
				CaseNo = mUserManager.getUserID() + G.getPhoneCurrentTime("yyyyMMddHHmmss"); 
			}
			if(CaseNo.contains("-")||CaseNo.contains("*")||CaseNo.contains(":")||CaseNo.contains("/")||CaseNo.contains("<")||CaseNo.contains(">")||CaseNo.contains("?")||CaseNo.contains("\"")||CaseNo.contains("\\")){
				Dialog.negativeDialog(mContext, getString(R.string.createcase_zifu_caseno));
				return;
			}
			
			cv.put(DBModle.Task.CaseNo,CaseNo);
			
			
			String CarMark = getEditText(DBModle.Task.CarMark);
			if (!filtText(CarMark, DBModle.Task.CarMark)) {
				return;
			}
			////:*?"<>|
			if(CarMark.contains("-")||CarMark.contains(":")||CarMark.contains("/")||CarMark.contains("<")||CarMark.contains(">")||CarMark.contains("?")||CarMark.contains("\"")||CarMark.contains("\\")){
				Dialog.negativeDialog(mContext, getString(R.string.createcase_zifu_carmark));
				return;
			}
			
			
			cv.put(DBModle.Task.CarMark,CarMark);
			
			String CarDriver = getEditText(DBModle.Task.CarDriver);
			if (!filtText(CarDriver, DBModle.Task.CarDriver)) {
				return;
			}
			cv.put(DBModle.Task.CarDriver,CarDriver);
			
			String CarOwner = getEditText(DBModle.Task.CarOwner);
			if (!filtText(CarOwner, DBModle.Task.CarOwner)) {
				return;
			}
			cv.put(DBModle.Task.CarOwner,CarOwner);
			
			String Telephone = getEditText(DBModle.Task.Telephone);
			if (!filtText(Telephone, DBModle.Task.Telephone)) {
				return;
			}
			cv.put(DBModle.Task.Telephone,Telephone);
			
			String Address = getEditText(DBModle.Task.Address);
			if (!filtText(Address, DBModle.Task.Address)) {
				return;
			}
			cv.put(DBModle.Task.Address,Address);
			
			cv.put(DBModle.Task.TaskType,taskType);
			cv.put(DBModle.Task.FrontOperator,mUserName);
			cv.put(DBModle.Task.BackState, DBModle.TaskLog.FrontState_NO_ASSIGN);
			cv.put(DBModle.Task.BackOperator, "");
			//经纬度
//			String latitude= BaiduLocation.instance().LOCAITON_LATITUDE;
//			String longitude = BaiduLocation.instance().LOCAITON_LONITUDE;
//			cv.put(DBModle.Task.Latitude, latitude);
//			cv.put(DBModle.Task.Longitude, longitude);
			cv.put(DBModle.Task.Latitude, "0");
			cv.put(DBModle.Task.Longitude, "0");
			
			cv.put(DBModle.Task.Watcher, "");
			cv.put(Task.FrontPrice, "0");
			cv.put(Task.BackPrice, "0");
			cv.put(Task.FixedPrice, "0");
			cv.put(DBModle.Task.Memo, "");
			cv.put(DBModle.Task.StateUpdateTime, getCurrentTime());
			cv.put(DBModle.Task.AccidentTime, getCurrentTime());
			cv.put(DBModle.Task.IsNew, 1);
			cv.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION);
			cv.put(DBModle.Task.GarageID, "-1");
			
			cv.put(DBModle.Task.CaseType, taskType);
			cv.put(DBModle.Task.AutoGenerationNO, "");
			
			String CarType = getEditText(DBModle.Task.CarType);
			if (!filtText(CarType, DBModle.Task.CarType)) {
				return;
			}
			cv.put(DBModle.Task.CarType,CarType);
			
			cv.put(DBModle.Task.AccidentType, mAccidentType);
			cv.put(DBModle.Task.InsuranceType, "");
			cv.put(DBModle.Task.InsuranceID, mInsuranceId);
			cv.put(DBModle.Task.InsuranceContactPeople, "");
			cv.put(DBModle.Task.InsuranceContactTelephone, "");
			cv.put(DBModle.Task.PaymentAmount, "");
			cv.put(DBModle.Task.PaymentMethod, PaymentMethod);
			cv.put(DBModle.Task.IsRisk, "0");
			cv.put(DBModle.Task.AccidentDescribe, "");
			
			cv.put(DBModle.Task.LinkCaseNo, CaseNo);
			cv.put(DBModle.Task.ImgPath, "");
			cv.put(DBModle.Task.TaskID, "-1");
			cv.put(DBModle.Task.CarTypeID, "-1");
			
			cv.put(DBModle.Task.ThirdCar, "");
			cv.put(DBModle.Task.AreaID, UserManager.getInstance().getAreaID());
			
			if(!isCommpleted()){
				if(mRecieves.contains(mContext.getResources().getString(R.string.RoleType_M006_name))){
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.createcase_jiangxi_mess));
				}else{
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.createcase_yuandong_mess));
				}
			}else{
				Dialog.negativeAndPositiveDialog(mContext, mContext.getResources().getString(R.string.createcase_success),  new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(DBOperator.createTask(cv)){
							UploadWork.sendDataChangeBroadcast(mContext);
							Intent intent = new Intent();
							setResult(Main.FrontState_CREATE, intent);
							finish();
						}else {
							Dialog.negativeDialog(mContext,  mContext.getResources().getString(R.string.createcase_fail));
						}
					}
				});
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
	
	
	/**
	 * 获取要显示的items
	 * @param type 根据类型获取
	 * @param exceptItem 不需要显示的
	 * @return
	 */
	private List<ModelUnit> makeItems(int type,String... exceptItem){
		List<ModelUnit> modelUnits = new ArrayList<ModelUnit>();
		
		ModelUnit[] units = null;
		switch (type) {
		case DEFAULT_TYPE:
			units = CaseInfoModel.titleDeafault;
			break;
		case NOT_NULL_TYPE:
			units = CaseInfoModel.titleNotNull;
			break;
		case OPTIONAL_TYPE:
			units = CaseInfoModel.titleOptional;
			break;

		default:
			break;
		}
		for (int i = 0; i < units.length; i++) {
			modelUnits.add(units[i]);
		}
		if (exceptItem!=null&&exceptItem.length>0) {
			int[] removeIndex = new int[exceptItem.length];
			int m = 0;
			for (int i = 0; i < modelUnits.size(); i++) {
				for (int j = 0; j < exceptItem.length; j++) {
					if (modelUnits.get(i).getTag().equals(exceptItem[j])) {
						removeIndex[m++]=i;
					}
				}
				
			}
			for (int i = removeIndex.length-1; i >-1 ; i--) {
				modelUnits.remove(removeIndex[i]);
			}
		}
		
		return modelUnits;
	}
	
	/**
	 * 生成一个TAbleView
	 * @param tableView
	 * @param titles
	 */
	@SuppressLint("ResourceAsColor")
	private HashMap<String, IListItem> makeTableViews(UITableView tableView,List<ModelUnit> modelUnits){
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		HashMap<String, IListItem> map = new HashMap<String, IListItem>();
		
		for (int i = 0; i < modelUnits.size(); i++) {
			ModelUnit modelUnit = modelUnits.get(i);
			if (modelUnit.getTag().equals(DBModle.Task.InsuranceID)) {
				BasicItem basicItem = new BasicItem(modelUnit.getTitle());
				basicItem.setTag(modelUnit.getTag());
				if (getInsurance().length !=0) {
					if(getInsurance().length == 1){
						basicItem.setContent(getInsurance()[0]);
					}
				} else {
					basicItem.setContent(mContext.getResources().getStringArray(R.array.create_case_info_items_insurance_company)[0]);
				}
				
				tableView.addBasicItem(basicItem);
				map.put(modelUnit.getTag(),basicItem );
			}else if (modelUnit.getTag().equals(DBModle.Task.AccidentType)) {
				BasicItem basicItem = new BasicItem(modelUnit.getTitle());
				basicItem.setTag(modelUnit.getTag());
				basicItem.setContent(mContext.getResources().getStringArray(R.array.create_case_info_items_accident_type)[0]);
				tableView.addBasicItem(basicItem);
				map.put(modelUnit.getTag(),basicItem );
			}else if (modelUnit.getTag().equals(DBModle.Task.PaymentMethod)) {
				BasicItem basicItem = new BasicItem(modelUnit.getTitle());
				basicItem.setTag(modelUnit.getTag());
				basicItem.setContent(null);
				tableView.addBasicItem(basicItem);
				map.put(modelUnit.getTag(),basicItem );
			}else if (modelUnit.getTag().equals(DBModle.Task.FrontOperator)
					||modelUnit.getTag().equals(DBModle.Task.CaseType)) {
				BasicItem basicItem = new BasicItem(modelUnit.getTitle());
				basicItem.setTag(modelUnit.getTag());
				basicItem.setbImageVisibility(false);
				basicItem.setContent(modelUnit.getContent());
				tableView.addBasicItem(basicItem);
				map.put(modelUnit.getTag(),basicItem );
			}else {
				RelativeLayout view = (RelativeLayout) mInflater.inflate(R.layout.create_case_item_with_edittext, null);
				TextView title = (TextView) view.findViewById(R.id.create_case_item_edit_title);
				final EditText content = (EditText) view.findViewById(R.id.create_case_item_edit_edit);
				if (modelUnit.getTag().equals(DBModle.Task.Telephone)) {
					content.setInputType(InputType.TYPE_CLASS_PHONE);
				}
				if (!modelUnit.getTag().equals(DBModle.Task.Memo)||!modelUnit.getTag().equals(DBModle.Task.AccidentDescribe)) {
					
					content.addTextChangedListener(new TextWatcher() {
						
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							if (s.length()>25) {
								Toast.makeText(CreateCase2.this, getResources().getString(R.string.create_case_tips3), Toast.LENGTH_SHORT).show();
							}else {
							}
							
						}
						
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count,
								int after) {
							
						}
						
						@Override
						public void afterTextChanged(Editable s) {
							if (s.length()>25) {
								s.delete(25, 26);
							}
							
						}
					});
				}
				
				title.setText(modelUnit.getTitle());
				ViewItem viewItem = new ViewItem(view);
				map.put(modelUnit.getTag(),viewItem);
				
				tableView.addViewItem(viewItem);
			}
			
			
		}
		tableView.commit();
		
		return map;
	}
	
	
	/**
	 * 获取当前的EditText 的内容
	 * @param a
	 * @param b
	 * @return
//	 */
	private String getEditText(String tag){
		ViewItem item = (ViewItem)(viewItemsMap.get(tag));
		if (item==null) {
			return "";
		}
		View view =  item.getView();
		EditText editText = (EditText)(view.findViewById(R.id.create_case_item_edit_edit));
		String content = editText.getText().toString();
		return content;
	}
	
	/**
	 * 过滤字符
	 * @param text
	 * @param tag
	 * @return
	 */
	private boolean filtText(String text,String tag){
		boolean isAccess = true;
		if (text!=null&&!text.trim().equals("")) {
			if(text.length() >= 25){
				Dialog.negativeDialog(CreateCase2.this, "\""+((TextView)((ViewItem)viewItemsMap.get(tag)).getView().findViewById(R.id.create_case_item_edit_title)).getText().toString() +"\""+ getString(R.string.createcase_outstrip));
				isAccess = false;
			}
		}
		return isAccess;
		
	}
	
	/**
	 * 检查是否填写完毕
	 */
	private boolean isCommpleted(){
		boolean judgeJXorYD = false;
		if(mRecieves.contains(mContext.getResources().getString(R.string.RoleType_M006_name))){
			judgeJXorYD = getEditText(DBModle.Task.CarMark).trim().equals("") || getEditText(DBModle.Task.CaseNo).trim().equals("");
		}else {
			judgeJXorYD = getEditText(DBModle.Task.CarMark).trim().equals("") || mInsuranceId == -1;
		}
		
		boolean isCommpleted = true;
			if (judgeJXorYD) {
				isCommpleted = false;
			}
		return isCommpleted;
	}
	
	
	/**
	 * 用于弹出选择框
	 * @param items
	 */
	private void showChooseDialog(final String[] items,String title,final boolean isBasicItem,final IListItem item,final View view){
		AlertDialog.Builder builder = new Builder(this);
		builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				selectedItem = new String(items[which]);
				if (selectedItem !=null) {
					((TextView) view.findViewById(R.id.contenttext)).setText(selectedItem);
//					((ImageView) view.findViewById(R.id.chevron)).setVisibility(View.GONE);
				}
				if (isBasicItem) {
					BasicItem basicItem = (BasicItem) item;
				if (basicItem.getTag().toString().equals(DBModle.Task.InsuranceID)) {
					mInsuranceId = DBOperator.getInsuranceList().get(which).getAsInteger(DBModle.Insurance.InsuranceID);
				}
				if (basicItem.getTag().toString().equals(DBModle.Task.AccidentType)) {
					mAccidentType = new String(items[which]);
				}
				if (basicItem.getTag().toString().equals(DBModle.Task.PaymentMethod)) {
					PaymentMethod = items[which];
				}
				}
				dialog.dismiss();
			}
		});
		builder.setCancelable(true);
		builder.setTitle(title);
		builder.show();
	}
	
	/**
	 * 获取保险公司列表
	 * */
	public String[] getInsurance(){
		
		ArrayList<ContentValues> list = DBOperator.getInsuranceList();
		if (list != null) {
			String[] insuranceList = new String[list.size()]; 
			for(int i = 0 ; i<list.size() ; i++){
				insuranceList[i] = list.get(i).getAsString(DBModle.Insurance.InsuranceName);
			}
			return insuranceList;
		}else {
			return this.getResources().getStringArray(R.array.create_case_info_items_insurance_company);
		}
	}
 	
	@Override
	public void onClick(boolean isBasicItem,IListItem item,View view,int index) {
		if (isBasicItem) {
			BasicItem basicItem = (BasicItem) item;
			if (basicItem.getTag().toString().equals(DBModle.Task.InsuranceID)) {
				showChooseDialog(getInsurance(), basicItem.getTitle(),true,basicItem,view);
			}
			if (basicItem.getTag().toString().equals(DBModle.Task.AccidentType)) {
				showChooseDialog(mContext.getResources().getStringArray(R.array.create_case_info_items_accident_type), basicItem.getTitle(),true,basicItem,view);
			}
			if (basicItem.getTag().toString().equals(DBModle.Task.PaymentMethod)) {
			}
			
		}else {
			
		}
	}
	
	
	static class CaseInfoModel {
		/**
		 * 案件号
		 */
		static ModelUnit MODEL_CASE_NO = new ModelUnit(ModelTitle.CASE_NO,DBModle.Task.CaseNo );
		/**
		 * 电话号码
		 */
		static ModelUnit MODEL_TELEPHONE = new ModelUnit(ModelTitle.TELEPHONE,DBModle.Task.Telephone );
		 /**
		  * 公估费
		  */
		static ModelUnit MODEL_EVALUATE_COST = new ModelUnit(ModelTitle.EVALUATE_COST,DBModle.Task.PaymentAmount );
		 /**
		  * 公估费收取方式
		  */
		static ModelUnit MODEL_PAY_TYPE = new ModelUnit(ModelTitle.PAY_TYPE,DBModle.Task.PaymentMethod );
		 /**
		  * 被保险人
		  */
		static ModelUnit MODEL_INSURED_PERSON = new ModelUnit(ModelTitle.INSURED_PERSON,DBModle.Task.CarOwner );
		 /**
		  * 保险公司
		  */
		static ModelUnit MODEL_INSURACE_COMPANY = new ModelUnit(ModelTitle.INSURACE_COMPANY,DBModle.Task.InsuranceID );
		 /**
		  *案件类型
		  */
		static ModelUnit MODEL_CASE_TYPE = new ModelUnit(ModelTitle.CASE_TYPE,DBModle.Task.CaseType, "查勘");
		 /**
		  *案件受理人
		  */
		static ModelUnit MODEL_CASE_PERSON = new ModelUnit(ModelTitle.CASE_PERSON,DBModle.Task.FrontOperator,UserManager.getInstance().getUserName());
		 /**
		  * 案件时间
		  */
		static ModelUnit MODEL_CASE_TIME = new ModelUnit(ModelTitle.CASE_TIME,DBModle.Task.AccidentTime );
		
		 /**
		  * 驾驶员
		  */
		static ModelUnit MODEL_DRIVER = new ModelUnit(ModelTitle.DRIVER,DBModle.Task.CarDriver );
		 /**
		  * 车牌号
		  */
		static ModelUnit MODEL_CAR_NO = new ModelUnit(ModelTitle.CAR_NO,DBModle.Task.CarMark );
		 /**
		  * 案件地址
		  */
		static ModelUnit MODEL_CASE_ADDRESS = new ModelUnit(ModelTitle.CASE_ADDRESS,DBModle.Task.Address );
		 /**
		  * 厂牌号
		  */
		static ModelUnit MODEL_CAR_BAND = new ModelUnit(ModelTitle.CAR_BAND,DBModle.Task.CarType );
		/**
		 * 事故经过
		 */
		static ModelUnit MODEL_CASE_HISTORY = new ModelUnit(ModelTitle.CASE_HISTORY,DBModle.Task.AccidentDescribe );
		/**
		 * 备注
		 */
		static ModelUnit MODEL_CASE_OTHERS = new ModelUnit(ModelTitle.CASE_OTHERS,DBModle.Task.Memo );
		/**
		 * 事故种类
		 */
		static ModelUnit MODEL_ACCIDENT_TYPE = new ModelUnit(ModelTitle.ACCIDENT_TYPE,DBModle.Task.AccidentType );
		
		/*默认选项*/
		static ModelUnit[] titleDeafault= new ModelUnit[]{MODEL_CASE_PERSON,MODEL_CASE_TYPE};
	
		/* 必填选项*/
		static ModelUnit[] titleNotNull = new ModelUnit[]{MODEL_CASE_NO,MODEL_CAR_NO,MODEL_INSURACE_COMPANY};
		
		/*选填选项*/
		static ModelUnit[] titleOptional = new ModelUnit[]{
				MODEL_DRIVER,MODEL_TELEPHONE,
				MODEL_CASE_ADDRESS,MODEL_ACCIDENT_TYPE,
				MODEL_CAR_BAND,MODEL_INSURED_PERSON,
				MODEL_EVALUATE_COST,MODEL_PAY_TYPE};
	}
	
	static class ModelTitle{
		/**
		 * 案件号
		 */
		static String CASE_NO = mContext.getString(R.string.create_case_caseno);
		
		/**
		 * 电话号码
		 */
		static String TELEPHONE = mContext.getString(R.string.create_case_tel);
		 /**
		  * 公估费
		  */
		static String EVALUATE_COST = mContext.getString(R.string.create_case_evaluate);
		 /**
		  * 被保险人
		  */
		static String INSURED_PERSON = mContext.getString(R.string.create_case_insured);
		 /**
		  * 保险公司
		  */
		static String INSURACE_COMPANY = mContext.getString(R.string.create_case_commpany);
		 /**
		  *案件类型
		  */
		static String CASE_TYPE = mContext.getString(R.string.create_case_caseType);
		 /**
		  *案件受理人
		  */
		static String CASE_PERSON = mContext.getString(R.string.create_case_person);
		 /**
		  * 案件时间
		  */
		static String CASE_TIME = mContext.getString(R.string.create_case_time);
		 /**
		  * 公估费收取方式
		  */
		static String PAY_TYPE = mContext.getString(R.string.create_case_payType);
		 /**
		  * 驾驶员
		  */
		static String DRIVER = mContext.getString(R.string.create_case_driver);
		 /**
		  * 车牌号
		  */
		static String CAR_NO = mContext.getString(R.string.create_case_carNo);
		 /**
		  * 案件地址
		  */
		static String CASE_ADDRESS = mContext.getString(R.string.create_case_address);
		 /**
		  * 厂牌号
		  */
		static String CAR_BAND = mContext.getString(R.string.create_case_band);
		/**
		 * 事故经过
		 */
		static String CASE_HISTORY = mContext.getString(R.string.create_case_accident_history);
		/**
		 * 备注
		 */
		static String CASE_OTHERS = mContext.getString(R.string.create_case_others);
		/**
		 * 事故种类
		 */
		static String ACCIDENT_TYPE = mContext.getString(R.string.create_case_accident_type);
	}
	
	static class ModelUnit{
		/**
		 * item 的标题
		 */
		String title;
		/**
		 * item 的tag
		 */
		String tag;
		/**
		 * item 的textview 中可能存在的内容
		 */
		String content;
		
		public ModelUnit(String title,String tag,String content){
			this.title = title;
			this.tag = tag;
			this.content = content;
			
		}
		public ModelUnit(String title,String tag){
			this.title = title;
			this.tag = tag;
			this.content = content;
			
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}
