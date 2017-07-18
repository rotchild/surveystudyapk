package com.whhcxw.MobileCheck;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DBModle.Task;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.IntoCaseAdapter;
import com.whhcxw.global.G;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class IntoCase extends BaseActivity {

	private Context mContext;
	private Titlebar2 titlebar;

	private TextView mIntoOver_text;
	private TextView mTime_TextView;
	private TextView mPhone_TextView;
	private ListView mIntocase_list;
	private Button mCreate_button;
	private Button mClear_button;

	/** 短信的body累加结果 */
	private String mMess = "";
	/** 短信的电话号 */
	private String mPhone;

	private View mOver_mess_layout;
	private TextView mHint_View;

	private String mUserName;
	private IntoCaseAdapter mIntoAdapter;
	
	/** 标记已经选过的短信 */
	private ArrayList<HashMap<String, Object>> mAlreadyMes_List;

	private ArrayList<ContentValues> mData_list;

//	public static String PINGANSMSREG = "^被保险人/*(.*?(?=[，])).([0-9]+)[ ]*(.+?(?=驾)).{3}(.*?(?=[，])).(.*?(?=[，])).(.*?(?=[%])).([0-9|-]+)";

	//江西平安
//	public static String PINGANSMSREG ="被保险人/*(.*?(?=[，])).([0-9]+).*(.{2}-.*(?=驾)).{3}(.*?(?=[，])).(.*?(?=[，])).(.*?(?=[%])).([0-9|-]+)(.*?)/.*【中国平安】";
	//武汉人保
//	public static String PINGANSMSREG ="([A-Z]+\\d+),车辆(.*?),.*,联系人(.*)联系电话(\\d+),地点(.*),出险时间(.*?),.*【人保财险】";
	public static String PINGANSMSREG ="【人保财险】.*([A-Z]+\\d+),车辆(.*?),.*,联系人(.*)联系电话(\\d+),地点(.*),出险时间(.*?),.*";

	public static final int INTOCASE=888;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.intocase);
		mContext = this;
		initTitle();
	}

	public void initTitle() {
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);
//		titlebar.setRightBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.showRight(mRefreshOnClickListener);
		titlebar.setRightImageRes(R.drawable.ui_title_refresh);

		initView();
	}

	/** title刷新 */
	OnClickListener mRefreshOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			setData();
			clearData();
		}
	};

	public void initView() {

//		Bundle bundle = this.getIntent().getExtras();
//		mUserName = bundle.getString("mUserName");
		
		boolean isLogin = UserManager.getInstance().isLogin();
		
		if(!isLogin){
			Intent intent = new Intent(mContext,Login.class);
			startActivity(intent);
			finish();
		}

		
		mUserName = UserManager.getInstance().getUserName();
		mIntocase_list = (ListView) this.findViewById(R.id.intocase_list);
		mIntocase_list.setOnItemClickListener(mIntocaseListOnItemClickListener);

		mIntoOver_text = (TextView) this.findViewById(R.id.into_over);
		mTime_TextView = (TextView) this.findViewById(R.id.time);
		mPhone_TextView = (TextView) this.findViewById(R.id.phone);

		mCreate_button = (Button) this.findViewById(R.id.create_button);
		mCreate_button.setOnClickListener(mCreateButtonOnClickListener);
		mClear_button = (Button) this.findViewById(R.id.clear_button);
		mClear_button.setOnClickListener(mClearButtonOnClickListener);
		mHint_View = (TextView) this.findViewById(R.id.hint);
		mOver_mess_layout = (View) this.findViewById(R.id.over_mess_layout);

		mAlreadyMes_List = new ArrayList<HashMap<String, Object>>();

		

		setData();
	}

	/** 适配数据 */
	public void setData() {
		mData_list = getPhoneMess();
		mIntoAdapter = new IntoCaseAdapter(mContext, mData_list,R.layout.intocase_list);
		mIntocase_list.setAdapter(mIntoAdapter);
	}

	/** Itemclick响应事件 */
	OnItemClickListener mIntocaseListOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			IntoCaseAdapter mIntoCaseAdapter = (IntoCaseAdapter) arg0.getAdapter();
			ContentValues contentValues = (ContentValues) mIntoCaseAdapter.getItem(position);
			boolean b = (Boolean) contentValues.get("check");
			if (b) {// 追加短信

				if (mAlreadyMes_List.size() == 0) {// 当第一次选择时，隐藏“合并后的短信” 并显示短信格式
					mHint_View.setVisibility(View.GONE);
					mOver_mess_layout.setVisibility(View.VISIBLE);
					mPhone = contentValues.get("phone").toString();
					mTime_TextView.setText(contentValues.get("time").toString());
					mPhone_TextView.setText(mPhone);

				} else if (mAlreadyMes_List.size() == 3) {// 当多于三条短信时，不能再选，弹框提示
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.intocase_maximum));
					return;
				}

				contentValues.remove("check");
				contentValues.put("check", false);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("position", position);
				
				//判断是否为多条短信，若是去除（）
				String mess = contentValues.get("mess").toString();
				String bracketLeft= mess.substring(0,1);
				String mess2;
				String mMes;
				if(bracketLeft.equals("(")){
					mess2 =  mess.substring(5,mess.length());
					mMes = mess2;
				}else{
					mMes =mess;
				}
				
				if (mPhone.equals(contentValues.get("phone").toString())) {// 判断是否同一个电话号的短信
					mMess += mMes;
				} else {
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.intocase_nosame));
					return;
				}
				mIntoOver_text.setText(mMess);

				
				
				//添加到短信存储
				map.put("mess", mMes);
				
				mAlreadyMes_List.add(map);
				
				
			
			} else {// 减少短信
				int a = -1;
				for (int i = 0; i < mAlreadyMes_List.size(); i++) {
					a = (Integer) mAlreadyMes_List.get(i).get("position");
					if (a == position) {
						mAlreadyMes_List.remove(i);
					}
				}

				mMess = "";
				for (int j = 0; j < mAlreadyMes_List.size(); j++) {
					mMess += mAlreadyMes_List.get(j).get("mess").toString();
				}

				mIntoOver_text.setText(mMess);
				if (mAlreadyMes_List.size() == 0) {// 当选择又么得时，显示“合并后的短信” 并隐藏短信格式
					mHint_View.setVisibility(View.VISIBLE);
					mOver_mess_layout.setVisibility(View.GONE);
				}
				contentValues.remove("check");
				contentValues.put("check", true);
			}

			mIntoCaseAdapter.updata(mData_list);

		}
	};

	/** 获取手机短信 */
	public ArrayList<ContentValues> getPhoneMess() {

		ArrayList<ContentValues> data_list = new ArrayList<ContentValues>();
		final String SMS_URI_ALL = "content://sms/"; // 获取所有短信

		try {
			ContentResolver cr = getContentResolver();
			String[] projection = new String[] { "_id", "address", "person","body", "date", "type" };
			Uri uri = Uri.parse(SMS_URI_ALL);
			Cursor cur = cr.query(uri, projection, "body not like '%已拼接%' ",new String[] {}, " _id desc  limit 50");

			if (cur.moveToFirst()) {
				String phoneNumber;
				String smsbody;
				String date;
				String type;
				ContentValues values;
				int phoneNumberColumn = cur.getColumnIndex("address");// 手机号
				int smsbodyColumn = cur.getColumnIndex("body");// 短信内容
				int dateColumn = cur.getColumnIndex("date");// 日期
				int typeColumn = cur.getColumnIndex("type");// 收发类型 1表示接受 2表示发送
				do {
					phoneNumber = cur.getString(phoneNumberColumn);
					smsbody = cur.getString(smsbodyColumn);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					Date d = new Date(Long.parseLong(cur.getString(dateColumn)));
					date = dateFormat.format(d);
					int typeId = cur.getInt(typeColumn);

					// 只有接收短信才读取出来
					if (typeId == 1) {
						type = "接收";
						values = new ContentValues();
						values.put("phone", phoneNumber);
						values.put("mess", smsbody);
						values.put("time", date);
						values.put("check", true);
						data_list.add(values);

					} else if (typeId == 2) {
						type = "发送";
					} else {
						type = "";
					}

					if (smsbody == null)
						smsbody = "";
				} while (cur.moveToNext());
			} else {
				G.showToast(mContext, "没有短信", false);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return data_list;
	}

	/** 拆分短信 */
	public ContentValues parserSMS(String smsS,String UserName,Context context) {
//		String sms= smsS.substring(7);
		//去除短信中的空格及换行
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(smsS);
		String smsInfo = m.replaceAll("");

		ContentValues contentValues;
		String uName;
		String uPhone;
		String carNum;
		String carDriver = "";
//		String caseAddressType;
		String caseAddress;
		String caseNo;
//		String other;
		String garageShortName = ""; 
		String accidentTime;
		try {
			contentValues = new ContentValues();

			Pattern pattern = Pattern.compile(PINGANSMSREG);
			Matcher matcher = pattern.matcher(smsInfo);
			// int i=matcher.groupCount();
			if (matcher.find()) {
				// String ss = matcher.group(0);
//				uName = matcher.group(1);
//				uPhone = matcher.group(2);
//				carNum = matcher.group(3);
//				carDriver = matcher.group(4).replace("\\", "/");
////				caseAddressType = matcher.group(5);
//				caseAddress = matcher.group(6);
//				caseNo = matcher.group(7);
//				garageShortName = matcher.group(8);
////				other = "";
				caseNo = matcher.group(1);
				carNum = matcher.group(2);
				uName = matcher.group(3);
				uPhone = matcher.group(4);
				caseAddress = matcher.group(5);
				accidentTime = matcher.group(6);
				
			} else {
				return null;
			}

			if(!garageShortName.equals("")){
				ContentValues garageContentValues = DBOperator.getGarageContentValues(garageShortName);
				if(garageContentValues != null){
					contentValues.put(DBModle.Task.GarageID, garageContentValues.getAsString(DBModle.Garage.GarageID));
					contentValues.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION_YES);
				} else {
					contentValues.put(DBModle.Task.GarageID, "-1");
					contentValues.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION);
				}
			} else {
				contentValues.put(DBModle.Task.GarageID, "-1");
				contentValues.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION_NO);
			}
			
			contentValues.put(DBModle.Task.CaseNo, caseNo);
			contentValues.put(DBModle.Task.CarMark, carNum);
			contentValues.put(DBModle.Task.CarDriver, carDriver);
			if(uName.length() >50){
				uName = uName.substring(0, 49);
			}
			contentValues.put(DBModle.Task.CarOwner, uName);
			contentValues.put(DBModle.Task.Telephone, uPhone);
			contentValues.put(DBModle.Task.Address, caseAddress);
			contentValues.put(DBModle.Task.TaskType, context.getResources().getString(R.string.tasktype_survey));// 查勘
			contentValues.put(DBModle.Task.FrontOperator, UserName);
			contentValues.put(DBModle.Task.FrontState,DBModle.TaskLog.FrontState_NO_ASSIGN);
			contentValues.put(DBModle.Task.BackState, DBModle.TaskLog.FrontState_NO_ASSIGN);
			contentValues.put(DBModle.Task.BackOperator, "");
			// 经纬度
//			contentValues.put(DBModle.Task.Latitude, BaiduLocation.instance().LOCAITON_LATITUDE);
//			contentValues.put(DBModle.Task.Longitude, BaiduLocation.instance().LOCAITON_LONITUDE);
			contentValues.put(DBModle.Task.Latitude, "0");
			contentValues.put(DBModle.Task.Longitude, "0");
			contentValues.put(DBModle.Task.Watcher, "");
			contentValues.put(DBModle.Task.Memo, "");
			contentValues.put(DBModle.Task.StateUpdateTime,CreateCase.getCurrentTime());
			contentValues.put(DBModle.Task.AccidentTime,accidentTime);
			contentValues.put(DBModle.Task.FrontPrice, "0");
			contentValues.put(DBModle.Task.FixedPrice, "0");
			contentValues.put(Task.BackPrice, "0");
			contentValues.put(DBModle.Task.IsNew, "1");
			contentValues.put(DBModle.Task.LinkCaseNo, caseNo);
			contentValues.put(DBModle.Task.ImgPath, "");
			contentValues.put(DBModle.Task.AccidentType, "");
			contentValues.put(DBModle.Task.IsRisk, "0");
			contentValues.put(DBModle.Task.InsuranceContactPeople, "");
			contentValues.put(DBModle.Task.CaseType, "");
			contentValues.put(DBModle.Task.AutoGenerationNO, "");
			contentValues.put(DBModle.Task.AccidentDescribe, "");
			contentValues.put(DBModle.Task.InsuranceType, "");
			contentValues.put(DBModle.Task.ThirdCar, "");
			
			String jxpa = context.getResources().getString(R.string.jxpa);
			
			ContentValues insuranceValues = DBOperator.getInsuranceByName(jxpa);
			int InsuranceID = 0;
			if(insuranceValues != null){
				InsuranceID = insuranceValues.getAsInteger(DBModle.Insurance.InsuranceID);
			}
			//人保
//			InsuranceID=1;
			contentValues.put(DBModle.Task.InsuranceID, InsuranceID);
			contentValues.put(DBModle.Task.TaskID, "-1");
			contentValues.put(DBModle.Task.PaymentAmount, "");
			contentValues.put(DBModle.Task.PaymentMethod, "");
			contentValues.put(DBModle.Task.InsuranceContactTelephone, "");
			contentValues.put(DBModle.Task.CarType,"");
			contentValues.put(DBModle.Task.CarTypeID,"-1");
			contentValues.put(DBModle.Task.AreaID, "");
		} catch (Exception e) {
			contentValues = null;
		}
		return contentValues;
	}

	/** 创建短信任务 */
	OnClickListener mCreateButtonOnClickListener = new OnClickListener() {
		ContentValues contentValues;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String mess = mIntoOver_text.getText().toString();
			if (mess.equals("")) {
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.intocase_nomess));
			} else {
				contentValues = parserSMS(mess,mUserName,mContext);
				if(contentValues == null){
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.intocase_errorform));
					return;
				}
				Dialog.negativeAndPositiveDialog(mContext, mContext.getResources().getString(R.string.intocase_success),new AlertDialog.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						boolean b = DBOperator.createTask(contentValues);
						if (b) {
							clearData();
							Intent intent = new Intent();
							setResult(INTOCASE, intent);
							finish();
						} else {
							ArrayList<ContentValues> taskList = DBOperator.getTasks(mUserName);
							for(int i = 0;i<taskList.size();i++){
								if(taskList.get(i).get(DBModle.Task.CaseNo).toString().equals(contentValues.get(DBModle.Task.CaseNo))){
									Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.intocase_alreadycreate));
									return;
								}
							}
								
							Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.intocase_error));
						}
						
						
					}
				});
			}
		}
	};
	
	/**清空所选项*/
	OnClickListener mClearButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			clearData();
		}
	};

	/**清除选的数据*/
	private void clearData(){
		mMess = "";
		mAlreadyMes_List.removeAll(mAlreadyMes_List);
		ContentValues contentValues ;
		for(int i=0;i<mData_list.size();i++){
			contentValues = mData_list.get(i);
			contentValues.put("check", true);
		}
		mIntoOver_text.setText("");
		mHint_View.setVisibility(View.VISIBLE);
		mOver_mess_layout.setVisibility(View.GONE);
		mIntoAdapter.updata(mData_list);
	}
	
}
