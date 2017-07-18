package com.whhcxw.ui;

import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class CaseDetailsScrollView extends ScrollView {
	
	private Resources mResources;
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
	
	private TextView cartype_text;
	
	private TextView cardriver;
	
	private TextView accident_text;
	
	private TextView casetype_text;
	
	private TextView insurancetype_text;
	
	private TextView insuranceContactPeople_text;
	
	private TextView insuranceContactTelephone_text;
	
	private TextView AccidentDescribe_text;
	
	private TextView memo_text;
	
	private TextView payAmount;
	
	private TextView isRisk;
	
	private TextView mThirdcar;
	
	private TextView mLinkcase_text;
	
	private TextView Insurance_text;
	
	private Context mContext;
	
	private boolean isJXOrYD = false;
	public CaseDetailsScrollView(Context context) {
		super(context);
		this.mContext = context;
		initView();
	}
	
	public CaseDetailsScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		initView();
	}

	public CaseDetailsScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initView();
	}

	private void initView(){
		
		//江西 显示，山东隐藏
		String recieves = UserManager.getInstance().getRoleTypes();
		if(recieves.contains(mContext.getResources().getString(R.string.RoleType_M006_name))){
			isJXOrYD = true;
		}
		
		
		
		mResources = mContext.getResources();
		LayoutInflater.from(mContext).inflate(R.layout.main_casedetailsscrollview, this, true);
		
		mCar_TextView = (TextView) this.findViewById(R.id.car);
		mCasenumber_TextView = (TextView) this.findViewById(R.id.casenumber);
		mCarmaster_TextView = (TextView) this.findViewById(R.id.carmaster);
		mPhone_TextView = (TextView) this.findViewById(R.id.phone);
		mTime_TextView = (TextView) this.findViewById(R.id.time);
		mAddress_TextView = (TextView) this.findViewById(R.id.address);
		
		//车辆品牌
		cartype_text = (TextView)this.findViewById(R.id.cartype_text); 
		
		//驾驶员
		cardriver = (TextView)this.findViewById(R.id.cardriver_text); 
		
		//事故类型
		accident_text = (TextView)this.findViewById(R.id.accident_text); 
		
		//案件类型
		casetype_text = (TextView)this.findViewById(R.id.casetype_text); 
		
		//保险险种
		insurancetype_text = (TextView)this.findViewById(R.id.insurancetype_text); 

		//保险公司联系人
		insuranceContactPeople_text = (TextView)this.findViewById(R.id.insuranceContactPeople_text); 

		//保险公司联系电话
		insuranceContactTelephone_text = (TextView)this.findViewById(R.id.insuranceContactTelephone_text); 
		
		//事故经过
		AccidentDescribe_text = (TextView)this.findViewById(R.id.AccidentDescribe_text); 
			
		//事故经过
		memo_text = (TextView)this.findViewById(R.id.memo_text); 
		
		payAmount = (TextView) findViewById(R.id.paymentAmount_text);
		
		isRisk = (TextView) findViewById(R.id.isrisk_text);
		
		mThirdcar = (TextView) this.findViewById(R.id.thirdcar_text);
		
		
		mLinkcase_text = (TextView)this.findViewById(R.id.linkcase_text);
		
		Insurance_text = (TextView)this.findViewById(R.id.Insurance_text);
	}
	
	/**设置车牌号*/
	public void setCar_TextView(ContentValues values){
		mCar_TextView.setText(values.getAsString(DBModle.Task.CarMark));
	}
	

	/**案件号*/
	public void setCasenumber_TextView(ContentValues values){
		mCasenumber_TextView.setText(values.getAsString(DBModle.Task.CaseNo));
	}
	
	/**车主*/
	public void setCarmaster_TextView(ContentValues values){
		mCarmaster_TextView.setText(values.getAsString(DBModle.Task.CarOwner));
	}
	
	
	/**电话号*/
	public void setPhone_TextView(ContentValues values){
		mPhone_TextView.setText(values.getAsString(DBModle.Task.Telephone));
	}
	
	
	/**案发时间 */
	public void setTime_TextView(ContentValues values){
		mTime_TextView.setText(values.getAsString(DBModle.Task.AccidentTime));
	}
	
	
	/**案发地点*/
	public void setAddress_TextView(ContentValues values){
		mAddress_TextView.setText(values.getAsString(DBModle.Task.Address));
	}
	
	
	/**车辆品牌*/
	public void setCarType(ContentValues values){
		if(isJXOrYD){
			cartype_text.setVisibility(View.GONE);
			return;
		}
		String cartype_Value = values.getAsString(DBModle.Task.CarType);
		String cartypeStr= mResources.getString(R.string.casedetails_cartype);
		cartypeStr = String.format(cartypeStr, cartype_Value);
		cartype_text.setText(cartypeStr);
	}
	
	
	/**驾驶员 */
	public void setCarDirver(ContentValues values){
		if(isJXOrYD){
			cardriver.setVisibility(View.GONE);
			return;
		}
		
		String carDirver_Value = values.getAsString(DBModle.Task.CarDriver);
		String cardirverStr=  mResources.getString(R.string.casedetails_cardriver);
		cardirverStr = String.format(cardirverStr, carDirver_Value);
		cardriver.setText(cardirverStr);
	}
	
	
	/**保险公司 */
	public void setInsurance(ContentValues values){
		if(isJXOrYD){
			Insurance_text.setVisibility(View.GONE);
			return;
		}
		
		String InsuranceID = values.getAsString(DBModle.Task.InsuranceID);
		ContentValues insuranceValues = DBOperator.getInsurance(InsuranceID);
		if(insuranceValues!=null){
			Insurance_text.setVisibility(View.VISIBLE);
			String insuranceName= insuranceValues.getAsString(DBModle.Insurance.InsuranceName);
			String InsuranceStr=  mResources.getString(R.string.casedetails_Insurancer);
			InsuranceStr = String.format(InsuranceStr, insuranceName);
			Insurance_text.setText(InsuranceStr);
		}
	}
	
	/**事故类型*/
	public void setAccidentType(ContentValues values){
		if(isJXOrYD){
			accident_text.setVisibility(View.GONE);
			return;
		}
		
		String accident_Value = values.getAsString(DBModle.Task.AccidentType);
		if(!accident_Value.equals("")){
			String accidentStr=  mResources.getString(R.string.casedetails_accident);
			accidentStr = String.format(accidentStr, accident_Value);
			accident_text.setText(accidentStr);
		}else {
			String accidentStr=  mResources.getString(R.string.casedetails_accident);
			accidentStr = String.format(accidentStr, "");
			accident_text.setText(accidentStr);
		}
		
	}
	
	/**案件类型（调查、本地查勘、异地代查勘） */
	public void setCaseType(ContentValues values){
		if(isJXOrYD){
			casetype_text.setVisibility(View.GONE);
			return;
		}
		String casetype_Value = values.getAsString(DBModle.Task.CaseType);
			String casetypeStr=  mResources.getString(R.string.casedetails_casetype);
			casetypeStr = String.format(casetypeStr, casetype_Value);
			casetype_text.setText(casetypeStr);
	}
	
	
	/**险种（商业，交强，商业交强） */
	public void setInsuranceType(ContentValues values){
		if(isJXOrYD){
			insurancetype_text.setVisibility(View.GONE);
			return;
		}
		String insurancetype_Value = values.getAsString(DBModle.Task.InsuranceType);
			String insurancetypeStr=  mResources.getString(R.string.casedetails_insurancetype);
			insurancetypeStr = String.format(insurancetypeStr, insurancetype_Value);
			insurancetype_text.setText(insurancetypeStr);
			
	}
	
	
	/**保险公司联系人 */
	public void setInsuranceContactPeople(ContentValues values){
		String insuranceContactPeople_Value = values.getAsString(DBModle.Task.InsuranceContactPeople);
			String insuranceContactPeopleStr=  mResources.getString(R.string.casedetails_insuranceContactPeople);
			insuranceContactPeopleStr = String.format(insuranceContactPeopleStr, insuranceContactPeople_Value);
			insuranceContactPeople_text.setText(insuranceContactPeopleStr);
			if(isJXOrYD){
				insuranceContactPeople_text.setVisibility(View.GONE);
			}
	}
	
	/**保险公司联系电话 */
	public void setInsuranceContactTelephone(ContentValues values){
		String insuranceContactTelephone_Value = values.getAsString(DBModle.Task.InsuranceContactTelephone);
		String insuranceContactTelephoneStr=  mResources.getString(R.string.casedetails_insuranceContactPhone);
		insuranceContactTelephoneStr = String.format(insuranceContactTelephoneStr, insuranceContactTelephone_Value);
		insuranceContactTelephone_text.setText(insuranceContactTelephoneStr);
		
		if(isJXOrYD){
			insuranceContactTelephone_text.setVisibility(View.GONE);
		}
	}
	
	
	/**事故经过*/
	public void setAccidentDescribe(ContentValues values){
		String AccidentDescribe_Value = values.getAsString(DBModle.Task.AccidentDescribe);
		String AccidentDescribeStr=  mResources.getString(R.string.casedetails_AccidentDescribe);
		AccidentDescribeStr = String.format(AccidentDescribeStr, AccidentDescribe_Value);
		AccidentDescribe_text.setText(AccidentDescribeStr);
		if(isJXOrYD){
			AccidentDescribe_text.setVisibility(View.GONE);
		}
	}
	
	
	//备注
	public void setMemo(ContentValues values){
		String memo_Value = values.getAsString(DBModle.Task.Memo);
			String memoStr=  mResources.getString(R.string.casedetails_memo);
			memoStr = String.format(memoStr, memo_Value);
			memo_text.setText(memoStr);
	}
	
	//公估费
	public void setPaymentAmount(ContentValues values){
//		if (!values.getAsString(DBModle.Task.PaymentAmount).equals("")&&!values.getAsString(DBModle.Task.PaymentAmount).trim().equals("")) {
			String paymentAmount=  mResources.getString(R.string.PaymentAmount);
			paymentAmount = String.format(paymentAmount, values.getAsString(DBModle.Task.PaymentAmount));
			payAmount.setText(paymentAmount);
//		}
			
		if(isJXOrYD){
			payAmount.setVisibility(View.GONE);
		}
	}
	
	//是否风险案件（0不是，1是 ）
	public void setIsRisk(ContentValues values){
		if (values.getAsInteger(DBModle.Task.IsRisk)!=null&&values.getAsInteger(DBModle.Task.IsRisk)==1) {
			isRisk.setVisibility(View.VISIBLE);
		}
	}
	
	//三者车
	public void setThirdCar(ContentValues values){
		if(values.getAsString(DBModle.Task.ThirdCar).equals("")){
			mThirdcar.setVisibility(View.GONE);
		}else {
			mThirdcar.setVisibility(View.VISIBLE);
			String thirdCar=  mResources.getString(R.string.casedetails_thirdcar);
			thirdCar = String.format(thirdCar, values.getAsString(DBModle.Task.ThirdCar));
			mThirdcar.setText(thirdCar);
		}
	}
	
	//连接案件
	public void setLinkCase(ContentValues values){
		if(values.getAsString(DBModle.Task.LinkCaseNo).equals(values.getAsString(DBModle.Task.CaseNo))){
			mLinkcase_text.setVisibility(View.GONE);
		}else {
			mLinkcase_text.setVisibility(View.VISIBLE);
			String linkcase=  mResources.getString(R.string.casedetails_linkcase);
			linkcase = String.format(linkcase, values.getAsString(DBModle.Task.LinkCaseNo));
			mLinkcase_text.setText(linkcase);
		}
	}
}
