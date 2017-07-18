package com.whhcxw.MobileCheck;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.GridViewAdapter;
import com.whhcxw.camera.CameraMain;
import com.whhcxw.camera.picture.PictureManager;
import com.whhcxw.camera.picture.PictureManager.IstoragePictureListener;
import com.whhcxw.camera.picture.PictureParameters;
import com.whhcxw.global.G;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

public class CaseDetails extends BaseActivity {

	private static final String TAG = "CaseDetails";

	private String mPath_str;
	private ArrayList<HashMap<String, Object>> mPaths_list;

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

	private View mApply_layout;

	private Animation mAnimation_show;
	private Animation mAnimation_hide;


	private View mLarge_layout;
	private View mDanger_layout;

	private Button mMore_button;
	private Button mUserphone_button;
	private Button mRepair_button;
	private Button mTackpicture_button;
	private Button mFinishcase_button;

	private String mTID;
	private String mLongitude="0.0";
	private String mLatitude="0.0";
	private String mLinkCaseNo;
	private String mTasktype;
	private String mFrontOperator;


	/**大案*/
	public static final String LARGE = "LARGE";
	/**风险*/
	public static final String DANGER = "DANGER";

	/**大案风险上报*/
	public static final int REPORT = 101;

	/**相片编辑状态  1 编辑状态 2 不可编辑 */
	private int mPicEdit = 2;
	private Button mEdit_Button;
	private Button mOff_Button;
	private Button mSure_Button;
	private View mEdit_layout;

	private ContentValues mContentValues;

	private GridView mGridView;
	private GridViewAdapter mGridViewAdapterr;

	private View mNopicture_layout;
	private String mUserClass;

	private View mDependence_layout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_casedetails);
		mContext = this;
		initTitle();

	}

	public void initTitle() {
		initview();
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				setResult(CaseManager.CASESTATE, intent);
				finish();
			}
		});
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		
		if(mUserClass.equals(Main.SURVEY)){
			titlebar.setCenterText(R.string.app_name);
		}else if(mUserClass.equals(Main.SUPERVISOR)){
			titlebar.setCenterText(R.string.app_name_large);
		}else if(mUserClass.equals(Main.DANGER)){
			titlebar.setCenterText(R.string.app_name_danger);
		}else if(mUserClass.equals(Main.RECEPTIONIST)){
			titlebar.setCenterText(R.string.app_name_recommend);
		}else if(mUserClass.equals(Main.OVERHAUL)){
			titlebar.setCenterText(R.string.app_name_overhaul);
		}else {
			titlebar.setCenterText(R.string.app_name);
		}
	}

	private void initview() {
		Intent intent = getIntent();
		mTID = intent.getStringExtra("TID");

		mContentValues = DBOperator.getTask(mTID);
		mLinkCaseNo =  mContentValues.getAsString(DBModle.Task.LinkCaseNo);
		mTasktype = mContentValues.getAsString(DBModle.Task.TaskType);
		mUserPhone =mContentValues.getAsString(DBModle.Task.Telephone);
		mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
		mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;
		mFrontOperator = mContentValues.getAsString(DBModle.Task.FrontOperator);

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
		mPhone_TextView.setText(mContentValues.get(DBModle.Task.Telephone).toString());
		mTime_TextView = (TextView) this.findViewById(R.id.time);
		mTime_TextView.setText(mContentValues.get(DBModle.Task.AccidentTime).toString());
		mAddress_TextView = (TextView) this.findViewById(R.id.address);
		mAddress_TextView.setText(mContentValues.get(DBModle.Task.Address).toString());

		mNopicture_layout = (View)this.findViewById(R.id.nopicture_layout);

		//		boolean b = getPictures();
		mGridView = (GridView) this.findViewById(R.id.gridview);
		//		if(b){
		//			setGridViewAdapter();
		//		}

		mApply_layout = (View) this.findViewById(R.id.apply_layout);
		mApply_layout.setOnClickListener(mApplylayoutOnClickListener);


		mMore_button = (Button) this.findViewById(R.id.more_button);
		mMore_button.setOnClickListener(moreOnClickListener);

		mUserphone_button = (Button) this.findViewById(R.id.userphone_button);
		mUserphone_button.setOnClickListener(mUserphoneButtonOnClickListener);

		mRepair_button = (Button) this.findViewById(R.id.repair_button);
		mRepair_button.setOnClickListener(mRepairButtonOnClickListener);


		mTackpicture_button = (Button) this.findViewById(R.id.tackpicture_button);
		mTackpicture_button.setOnClickListener(mTackpictureButtonOnClickListener);

		mFinishcase_button = (Button) this.findViewById(R.id.finishcase_button);
		mFinishcase_button.setOnClickListener(mFinishcaseButtonOnClickListener);


		mEdit_layout = (View)this.findViewById(R.id.edit_layout);
		mEdit_Button = (Button)this.findViewById(R.id.editbutton);
		mEdit_Button.setOnClickListener(mEditButtonOnClickListener);
		mOff_Button = (Button)this.findViewById(R.id.offbutton);
		mOff_Button.setOnClickListener(mOffButtonOnClickListener);
		mSure_Button = (Button)this.findViewById(R.id.surebutton);
		mSure_Button.setOnClickListener(mSureButtonOnClickListener);

		mLarge_layout = (View) this.findViewById(R.id.large_layout);
		mLarge_layout.setOnClickListener(mLargelayoutOnClickListener);
		mDanger_layout = (View) this.findViewById(R.id.danger_layout);
		mDanger_layout.setOnClickListener(mDangerlayoutOnClickListener);

		String frontState = mContentValues.getAsString(DBModle.Task.FrontState);
		if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
			mRepair_button.setEnabled(false);
			mFinishcase_button.setEnabled(false);
			mMore_button.setEnabled(false);
			mEdit_Button.setEnabled(false);
		}

		// 初始化动画
		mAnimation_show = AnimationUtils.loadAnimation(this,R.anim.casedetails_mapply_layout_show);
		mAnimation_hide = AnimationUtils.loadAnimation(this,R.anim.casedetails_mapply_layout_hide);


		mUserClass = UserManager.getInstance().getUserClass();
		if(mUserClass.equals(Main.OVERHAUL)){
			View repairLayout = (View)this.findViewById(R.id.repair_layout);
			repairLayout.setVisibility(View.GONE);
			View moreLayout = (View)this.findViewById(R.id.more_layout);
			moreLayout.setVisibility(View.GONE);
		}

		
		mDependence_layout = (View)this.findViewById(R.id.dependence_layout);
		mDependence_layout.setOnClickListener(mDependenceLayoutOnClickListener);
		
		View linkLayout = (View)this.findViewById(R.id.linklayout);
		TextView link_TextView = (TextView)this.findViewById(R.id.link);
		
		String caseNo = mContentValues.getAsString(DBModle.Task.CaseNo);
		if(!caseNo.equals(mLinkCaseNo)){
			linkLayout.setVisibility(View.VISIBLE);
			link_TextView.setText(mLinkCaseNo);
		}
	}

	/**相片编辑*/
	OnClickListener mEditButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mPicEdit = 1;
			mEdit_layout.setVisibility(View.VISIBLE);
			mEdit_Button.setVisibility(View.GONE);

		}
	};

	/**编辑确定*/
	OnClickListener mSureButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mPaths_list.size() == 0){
				offPicEdit();
				return;
			}

			ArrayList<HashMap<String, Object>> deleteList = new ArrayList<HashMap<String, Object>>(); 
			HashMap<String , Object> map;
			File picFile;
			for(int i = 0; i<mPaths_list.size();i++){
				map = mPaths_list.get(i);
				if(!(Boolean) map.get("check")){
					picFile = new File(map.get("path").toString());
					picFile.delete();
					deleteList.add(map);
				}
			}

			for(int i = 0 ;i<deleteList.size();i++){
				HashMap<String, Object> deleteHashMap = deleteList.get(i);
				for(int j = 0 ; j<mPaths_list.size();j++){
					if(deleteHashMap.toString().equals(mPaths_list.get(j).toString())){
						mPaths_list.remove(j);
					}
				}
			}

			mGridViewAdapterr.nitifyData(mPaths_list);
			offPicEdit();

			if(mPaths_list.size() == 0){
				mNopicture_layout.setVisibility(View.VISIBLE);
			}
		}
	};

	/**编辑取消*/
	OnClickListener mOffButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			offPicEdit();
		}
	};

	/**取消编辑*/
	public void offPicEdit(){
		mPicEdit = 2;
		mEdit_layout.setVisibility(View.GONE);
		mEdit_Button.setVisibility(View.VISIBLE);

		if(mPaths_list.size() == 0){
			return;
		}

		HashMap<String, Object> map;
		for(int i = 0;i<mPaths_list.size();i++){
			map = mPaths_list.get(i);
			map.put("check", true);
		}
		mGridViewAdapterr.nitifyData(mPaths_list);
	}

	/**获取照片路径*/
	public boolean getPictures(String caseNo){
		boolean b =false;
		mPath_str = G.STORAGEPATH + caseNo+"/"+mTasktype+"/";
		File file = new File(mPath_str);
		if(!file.exists()){
			file.mkdir();
		}
		mPaths_list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map;
		File[] files = file.listFiles();
		if(files != null){
			for (int i = 0; i < files.length; i++) {
				map = new HashMap<String, Object>();
				map.put("path", files[i].getAbsolutePath());
				map.put("check", true);
				mPaths_list.add(map);
			}
			b = true;
		}
		if(mPaths_list.size() == 0){
			mNopicture_layout.setVisibility(View.VISIBLE);
		}else {
			mNopicture_layout.setVisibility(View.GONE);
		}
		return b;
	}

	/**设置照片的加载*/
	public void setGridViewAdapter(){
		mGridViewAdapterr = new GridViewAdapter(mContext,mPaths_list,R.layout.gridview);
		mGridView.setOnItemClickListener(browseOnClickListener);
		mGridView.setAdapter(mGridViewAdapterr);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean b = getPictures(mLinkCaseNo);
		if(b){
			setGridViewAdapter();
		}
	}


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mApply_layout.getVisibility() == View.VISIBLE) {
			mApply_layout.setVisibility(View.GONE);
			//mApply_layout.startAnimation(mAnimation_hide);
			return;
		}
	}




	/**消失更多应用*/
	OnClickListener mApplylayoutOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mApply_layout.getVisibility() == View.VISIBLE) {
				mApply_layout.setVisibility(View.GONE);
				mApply_layout.startAnimation(mAnimation_hide);
				return;
			}
		}
	};



	/** 用户电话 */
	OnClickListener mUserphoneButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Dialog.telUserDialog(mContext, mUserPhone);
		}
	};

	/** 案件推修 */
	OnClickListener mRepairButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ArrayList<ContentValues> caseArrayList = DBOperator.getTasks(mFrontOperator);
			for(ContentValues values : caseArrayList){
				String caseNo =  values.get(DBModle.Task.CaseNo).toString();
				String taskType = values.getAsString(DBModle.Task.TaskType);
				String TID =values.getAsString(DBModle.Task.TID);
				if(mLinkCaseNo.equals(caseNo)&&taskType.equals(mContext.getResources().getString(R.string.tasktype_recommend))){
					Intent intent = new Intent(mContext,RepairState.class);
					intent.putExtra("TID", TID);
					if(values.get(DBModle.Task.CarTypeID) == null){
						intent.putExtra("isCooperation", true);
					}else {
						intent.putExtra("isCooperation", false);
					}

					startActivity(intent);
					return;
				}
			}

			Intent intent = new Intent(mContext,RepairRecommend.class);
			intent.putExtra("TID",mTID);
			intent.putExtra("update", false);
			intent.putExtra("updateisCooperation", true);
			//				startActivityForResult(intent, CaseManager.CASESTATE);
			startActivity(intent);
		}
	};

	/** 拍照 */
	OnClickListener mTackpictureButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			//开始查勘
			PictureManager.initInstance(mContext).setStorageCallback(new IstoragePictureListener() {
				@Override
				public PictureParameters setStorageParameters(long dateTaken) {
					//更新位置信息
					mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
					mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;

					PictureParameters parameters = new PictureParameters();						
					parameters.setDirectory(mPath_str);
					parameters.setLatitude(Double.parseDouble(mLatitude));
					parameters.setLongtitude(Double.parseDouble(mLongitude));
					parameters.setTitle(mLinkCaseNo+"_"+mTasktype+"_"+dateTaken);
					parameters.setTime(CreateCase.getCurrentTime());
					parameters.setResId(R.drawable.pingan_logo);//水印logo
					parameters.setUserNo(UserManager.getInstance().getUserName());//员工编号
					return parameters;
				}

				@Override
				public void afterStorage(String path, boolean success) {

					/*MD5 md5 = new MD5();
					String fileMa5 = "";
					try {
						fileMa5 = md5.getFileMD5String(new File(path));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e(TAG, "文件找不到 :"+e);
						return;
					}*/
					//72169F15B5622A559CFAEF3E8BCCDA85
					boolean bb =DBOperator.addPicture(mTID,mLinkCaseNo, path);

					if(bb){							
						DBOperator.updateTaskSate(mTID, DBModle.TaskLog.FrontState_START, mLongitude, mLatitude);
						UploadWork.sendDataChangeBroadcast(mContext);							
						Log.d(TAG, "插入照片成功 :");
					}else{
						Log.e(TAG, "插入照片失败 :");
					}

				}
			});


			//打开拍照 视频服务
			Intent intent = new Intent(CaseDetails.this, CameraMain.class);
			String ip= HttpParams.getHost(mContext);
			intent.putExtra("IP" , ip);
			int userID= Integer.parseInt(UserManager.getInstance().getUserID());
			intent.putExtra("SIGN", userID);

			String pw_ph = UserManager.getInstance().getPw_ph();
			String[] pwph = new String[2];
			if(!pw_ph.equals("")){
				pwph = pw_ph.split("x");
				intent.putExtra("PW", Integer.parseInt(pwph[0]));//幅面宽度
				intent.putExtra("PH", Integer.parseInt(pwph[1]));//幅面高度
			}

			String BITstr = UserManager.getInstance().getBIT();
			if(!BITstr.equals("")){
				intent.putExtra("BIT", Integer.parseInt(BITstr));//码率
			}

			String FPSstr = UserManager.getInstance().getFPS();
			if(!FPSstr.equals("")){
				intent.putExtra("FPS", Integer.parseInt(FPSstr));//
			}

			startActivity(intent);


			if(mPicEdit == 1){
				offPicEdit();
			}
		}
	};

	/**完成任务 */
	OnClickListener mFinishcaseButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//重新加载一次数据
			mContentValues = DBOperator.getTask(mTID);

			mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
			mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;
			String estprice="0";
			ContentValues recommendValues = DBOperator.getTask(mLinkCaseNo, mContext.getResources().getString(R.string.tasktype_recommend));
			if(recommendValues != null){
				estprice = recommendValues.getAsString(DBModle.Task.FrontPrice);
			}
			if(!estprice.equals("0")){
				boolean b = DBOperator.finishTask(estprice, mTID, mLongitude, mLatitude);
				if(b){
					UploadWork.sendDataChangeBroadcast(mContext);

					Dialog.negativeAndPositiveDialog(mContext, mContext.getResources().getString(R.string.casedetails_success), new AlertDialog.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							setResult(CaseManager.CASESTATE, intent);
							finish();
						}
					});
				}else {
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_fail));
				}

			}else{
				final EditText editText = new EditText(mContext);
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				Dialog.edittextDialog(mContext, mContext.getResources().getString(R.string.dialog_titlemess),editText,new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String caseMoey = editText.getText().toString();
						
						if(caseMoey.equals("")){
							Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_nomomey));
						} else{
							float f = Float.parseFloat(caseMoey);
							BigDecimal bigDecimal = new BigDecimal(f);
							float f2 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
							
							//更新位置信息
							boolean b = DBOperator.finishTask(String.valueOf(f2), mTID, mLongitude, mLatitude);
							if(b){
								UploadWork.sendDataChangeBroadcast(mContext);
								Dialog.positiveDialog(mContext, mContext.getResources().getString(R.string.casedetails_success),new AlertDialog.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										Intent intent = new Intent();
										setResult(CaseManager.CASESTATE, intent);
										finish();
									}
								});
							}else{
								Dialog.negativeDialog(mContext,mContext.getResources().getString(R.string.casedetails_fail));
							}
						}

					}
				});
			}
		}
	};


	/** 更多应用 */
	OnClickListener moreOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			missmore();
		}
	};

	/**更多应用的消失与显示*/
	public void missmore(){
		if (mApply_layout.getVisibility() == View.GONE) {
			mApply_layout.setVisibility(View.VISIBLE);
			mApply_layout.startAnimation(mAnimation_show);
		} else {
			mApply_layout.setVisibility(View.GONE);
			mApply_layout.startAnimation(mAnimation_hide);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (mApply_layout.getVisibility() == View.VISIBLE && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
			mApply_layout.setVisibility(View.GONE);
			mApply_layout.startAnimation(mAnimation_hide);
			return false;
		}

		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			setResult(CaseManager.CASESTATE, intent);
			finish();
			return false;

		}
		return super.onKeyDown(keyCode, event);
	}

	/** 预览照片 */
	OnItemClickListener browseOnClickListener = new OnItemClickListener() {

		@SuppressWarnings("static-access")
		@Override
		public void onItemClick(AdapterView<?> view, View arg1, int postion,long arg3) {
			if(mPicEdit==2){
				ArrayList<String> arrayList = new ArrayList<String>();
				for(int i = 0 ;i<mPaths_list.size();i++){
					arrayList.add(mPaths_list.get(i).get("path").toString());
				}
				PictureManager.initInstance(mContext).showPicture(arrayList,mContext,postion);
			}else {
				HashMap<String, Object> map = mPaths_list.get(postion);
				if((Boolean) map.get("check")){
					map.put("check", false);
				}else{
					map.put("check", true);
				}
				mGridViewAdapterr.notifyDataSetChanged();
			}
		}
	};

	/** 大案上报 */
	OnClickListener mLargelayoutOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putString("mTID", mTID);
			bundle.putString("TaskType",LARGE);

			if(verify(R.string.tasktype_large)){
				Intent intent = new Intent(mContext,Report.class);
				intent.putExtras(bundle);
				//				startActivityForResult(intent, REPORT);
				startActivity(intent);
			}
		}
	};

	/**校验是否是上报过了么*/
	public boolean  verify(int largeOrdanger){
		//		SharedPreferences preferences = getSharedPreferences("user",0);
		//		String username = preferences.getString("mUserName", null);

		String username = UserManager.getInstance().getUserName();
		ArrayList<ContentValues> arrayList = DBOperator.getTasks(username);
		String caseNo;
		String tasktype;
		for(int i = 0 ;i<arrayList.size();i++){
			caseNo = arrayList.get(i).get(DBModle.Task.CaseNo).toString();
			tasktype = arrayList.get(i).get(DBModle.Task.TaskType).toString();
			if(caseNo.equals(mLinkCaseNo)&&(tasktype.equals(mContext.getResources().getString(largeOrdanger))||tasktype.equals(mContext.getResources().getString(largeOrdanger)))){
				Intent intent = new Intent(mContext,LargerDangerCaseDetails.class);
				intent.putExtra("values", arrayList.get(i));
				startActivity(intent);
				//				finish();
				//				Dialog.negativeDialog(mContext, mContext.getResources().getString(largeOrdanger)+mContext.getResources().getString(R.string.casedetails_alreadycreate));
				//				missmore();
				return false;
			}
		}
		return true;
	}

	/** 风险上报 */
	OnClickListener mDangerlayoutOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putString("mTID", mTID);
			bundle.putString("TaskType",DANGER);

			if(verify(R.string.tasktype_danger)){
				Intent intent = new Intent(mContext,Report.class);
				intent.putExtras(bundle);
				//startActivityForResult(intent, REPORT);
				startActivity(intent);
			}

		}
	};
	
	OnClickListener mDependenceLayoutOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putString("TID", mTID);

			Intent intent = new Intent(mContext,DependenceCase.class);
			intent.putExtras(bundle);
			startActivityForResult(intent,DependenceCase.LINKCASE);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CaseManager.CASESTATE:
			setResult(CaseManager.CASESTATE, data);
			finish();
			break;
		case REPORT:
			//			finish();
			break;
			
		case DependenceCase.LINKCASE :
			ContentValues values  = DBOperator.getTask(mTID);
			String linkCaseNo = values.getAsString(DBModle.Task.LinkCaseNo);
			boolean b = getPictures(linkCaseNo);
			if(b){
				setGridViewAdapter();
			}
			break;
		default:
			break;
		}
	}
}
