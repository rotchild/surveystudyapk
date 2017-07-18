package com.whhcxw.MobileCheck;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.whhcxw.ui.CaseDetailsScrollView;
import com.whhcxw.ui.HorizontalScrollview;
import com.whhcxw.ui.HorizontalScrollviewListAdapter;
import com.whhcxw.ui.JasonClosedSlidingDrawer;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.utils.CatchException;
import com.whhcxw.utils.CommonUtil;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CaseDetails2 extends BaseActivity implements HorizontalScrollview.OnHorizontalScrollviewItemSelectedListener, HorizontalScrollview.OnScrollChangedListener {

	private static final String TAG = "CaseDetails";

	private String mPath_str;
	private ArrayList<HashMap<String, Object>> mPaths_list = new ArrayList<HashMap<String,Object>>();


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


	private Button mTackpicture_button;

	private String mTID;
	private String mLongitude="0.0";
	private String mLatitude="0.0";
	private String mCaseNo;
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
	private Gallery gallery ;
	private GridViewAdapter mGridViewAdapterr;
	private HorizontalScrollviewListAdapter mHorizontaladapter;

	private View mNopicture_layout;
	private String mUserClass;
	private LinearLayout preView,nextView;
	private ArrayList<HashMap<String, Object>> menuList = new ArrayList<HashMap<String,Object>>();
	private boolean viewState = true;
	private int			logoId = -1;
	
	private String mLinkCaseNo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_casedetails2);
		mContext = this;
		initview();
		initTitle();
		initPhotoView();
		getBottomMenuData();
		initBottomBar();
		
	}

	public void initTitle() {
		Titlebar2 titlebar = (Titlebar2) findViewById(R.id.titlebar);
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
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		
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
		}else 
			titlebar.setCenterText(R.string.app_name);
		}
	
	/**初始化案件详情*/
	private void initCaseDetais(ContentValues values){
		CaseDetailsScrollView caseDetailsScrollView = (CaseDetailsScrollView)findViewById(R.id.CaseDetailsScrollView);
		
		
		DisplayMetrics DM = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(DM);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)caseDetailsScrollView.getLayoutParams();
		int a = CommonUtil.dip2px(mContext, 335);
		
		layoutParams.height = DM.heightPixels-a;
		caseDetailsScrollView.setLayoutParams(layoutParams);
		
		caseDetailsScrollView.setCar_TextView(values);
		caseDetailsScrollView.setCasenumber_TextView(values);
		caseDetailsScrollView.setCarmaster_TextView(values);
		caseDetailsScrollView.setPhone_TextView(values);
		caseDetailsScrollView.setTime_TextView(values);
		caseDetailsScrollView.setAddress_TextView(values);
		caseDetailsScrollView.setCarType(values);
		caseDetailsScrollView.setCarDirver(values);
		caseDetailsScrollView.setAccidentType(values);
		caseDetailsScrollView.setCaseType(values);
		caseDetailsScrollView.setInsuranceType(values);
		caseDetailsScrollView.setInsuranceContactPeople(values);
		caseDetailsScrollView.setInsuranceContactTelephone(values);
		caseDetailsScrollView.setAccidentDescribe(values);
		caseDetailsScrollView.setMemo(values);
		caseDetailsScrollView.setPaymentAmount(values);
		caseDetailsScrollView.setIsRisk(values);
		caseDetailsScrollView.setThirdCar(values);
		caseDetailsScrollView.setLinkCase(values);
		caseDetailsScrollView.setInsurance(values);
	}
	
	private void initBottomBar(){
	
		HorizontalScrollview mHorizontalScrollview = (HorizontalScrollview) findViewById(R.id.case_detail_bottombar_scrollView);
		mHorizontaladapter = new HorizontalScrollviewListAdapter(this, R.layout.horizon_list_item, menuList,CommonUtil.getPhoneWidth(this)-CommonUtil.dip2px(this, 70));
		mHorizontalScrollview.setAdapter(this, mHorizontaladapter);
		mHorizontalScrollview.setOnHorizontalScrollviewItemSelectedListener(this);
		
		mHorizontalScrollview.setOnScrollChangedListener(this);
		preView = (LinearLayout) findViewById(R.id.case_detail_bottombar_pre);
		nextView = (LinearLayout) findViewById(R.id.case_detail_bottombar_next);
		if (menuList.size()>4) {
			nextView.setVisibility(View.VISIBLE);
		}else {
			nextView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void initPhotoView(){
	        
	    	mGridViewAdapterr = new GridViewAdapter(mContext,mPaths_list,R.layout.gridview);
			mGridView.setOnItemClickListener(browseOnClickListener);
			mGridView.setAdapter(mGridViewAdapterr);
			mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			mGridView.setVisibility(View.GONE);
//			
			gallery = (Gallery) findViewById(R.id.photo_content_horizon);
			gallery.setOnItemClickListener(browseOnClickListener);
			gallery.setAdapter(mGridViewAdapterr);
	    	
	    	
			JasonClosedSlidingDrawer slidingDrawer = (JasonClosedSlidingDrawer) findViewById(R.id.photo_sliding);
			   slidingDrawer.setOnDrawerCloseListener(new JasonClosedSlidingDrawer.OnDrawerCloseListener() {

		            public void onDrawerClosed() {
		            	mGridView.setVisibility(View.GONE);
		            	gallery.setVisibility(View.VISIBLE);
		            }
		        });

		        slidingDrawer.setOnDrawerOpenListener(new JasonClosedSlidingDrawer.OnDrawerOpenListener() {

		            public void onDrawerOpened() {
		            	mGridView.setVisibility(View.VISIBLE);
		            	gallery.setVisibility(View.GONE);
		            }
		        });
	}
	
	@Override
	public void onSelected(View view,int position,HashMap<String, Object> itemData) {
		String tag = itemData.get("tag").toString();
		String[] mRoleTypeName = getResources().getStringArray(R.array.system_roleType_array_name);
		String[] mRoleTypeName_defauly = getResources().getStringArray(R.array.system_roleType_array_name_default);
			//案件推修
			if (tag.equals(mRoleTypeName[0])) {
				mRepairCase();
			}
			//大案上报
			if (tag.equals(mRoleTypeName[1])) {
				reportLargeCase();
			}
			//风险上报
			if (tag.equals(mRoleTypeName[2])) {
				dangerCaseReport();
			}
//			//风险标记
//			if (tag.equals(mRoleTypeName[3])) {
//			
//			}
			//销案
			if (tag.equals(mRoleTypeName[4])) {
				clearTask();
				
			}
			//复堪
			if (tag.equals(mRoleTypeName[5])) {
				
			}
			//公估费
			if (tag.equals(mRoleTypeName[6])) {
				
			}
			//连接任务
			if (tag.equals(mRoleTypeName[8])) {
				linkCase();
			}
			//录入三者车车牌号
			if (tag.equals(mRoleTypeName[9])) {
				thirdCar();
			}
			
			//拒绝任务
//			if (tag.equals(mRoleTypeName[12])) {
//				refuseCase();
//			} 
			
			//用户电话
			if (tag.equals(mRoleTypeName_defauly[0])) {
				Dialog.telUserDialog(mContext, mUserPhone);
				
			}
			//完成任务
			if (tag.equals(mRoleTypeName_defauly[1])) {
				finishCase();
			}
		
			
			
	}
	
	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (menuList.size()>4) {
			if (l<=0) {
				preView.setVisibility(View.INVISIBLE);
				nextView.setVisibility(View.VISIBLE);
			}else {
				preView.setVisibility(View.VISIBLE);
				nextView.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	/**
	 * 生成底部栏数据
	 * @return
	 */
	private ArrayList<HashMap<String,Object>> getBottomMenuData(){
		
		//获取对应的显示按钮项
		String[] mRoleTypeName = getResources().getStringArray(R.array.system_roleType_array_name);
		String[] mRoleTypes = getResources().getStringArray(R.array.system_roleType_array_values);
		String[] mRoleTypeName_defauly = getResources().getStringArray(R.array.system_roleType_array_name_default);
		String[] mRoleTypes_detault = getResources().getStringArray(R.array.system_roleType_array_values_default);
		
		String mRecieves = UserManager.getInstance().getRoleTypes();
		
		ArrayList<HashMap<String, String>> resultRoleType = new ArrayList<HashMap<String,String>>();
		
		//添加默认按钮
		for (int i = 0; i < mRoleTypes_detault.length; i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("tag", mRoleTypeName_defauly[i]);
			map.put("title", mRoleTypes_detault[i]);
			resultRoleType.add(map);
		}
		
		for (int i = 0; i < mRoleTypeName.length; i++) {
			//在此判断是否是新版本
			if (mRecieves!=null&&!mRecieves.trim().equals("")) {
				if (mRecieves.contains(mRoleTypeName[i])) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("tag", mRoleTypeName[i]);
					map.put("title", mRoleTypes[i]);
					resultRoleType.add(map);
				}
			}else {
				//默认版本是只有拨打电话和完成任务两个选项
				
			}
			
		}
		menuList.clear();
		String frontState = mContentValues.getAsString(DBModle.Task.FrontState);
		if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
			viewState = false;
		}
		for (int i = 0; i < resultRoleType.size(); i++) {
			String tag = resultRoleType.get(i).get("tag");
			//案件推修
			if (tag.equals(mRoleTypeName[0])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_repair_bg_selector);
				map.put("picP", R.drawable.casedetails_repair_back);
				map.put("enable", viewState);
				menuList.add(map);
			}
			//大案上报
			if (tag.equals(mRoleTypeName[1])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_large_bg_selector);
				map.put("picP", R.drawable.casedetails_large_back);
				map.put("enable", viewState);
				menuList.add(map);}
			//风险上报
			if (tag.equals(mRoleTypeName[2])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_danger_bg_selector);
				map.put("picP", R.drawable.casedetails_danger_back);
				map.put("enable", viewState);
				menuList.add(map);
			}
			//销案
			if (tag.equals(mRoleTypeName[4])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_clear_bg_selector);
				map.put("picP", R.drawable.casedetail_menu_clear_p);
				map.put("enable", viewState);
				menuList.add(map);
			}
//			//公估费
//			if (tag.equals(mRoleTypeName[6])) {
//				HashMap<String, Object> map = new HashMap<String, Object>();
//				map.put("title", resultRoleType.get(i).get("title"));
//				map.put("tag", tag);
//				map.put("picN", R.drawable.casedetails_pay_bg_selector);
//				map.put("picP", R.drawable.casedetail_menu_pay_p);
//				map.put("enable", true);
//				menuList.add(map);
//			}
			//连接任务
			if (tag.equals(mRoleTypeName[8])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_connect_bg_selector);
				map.put("picP", R.drawable.casedetail_menu_connect_p);
				map.put("enable", viewState);
				menuList.add(map);
			}
			//录入三者车车牌号
			if (tag.equals(mRoleTypeName[9])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_record_bg_selector);
				map.put("picP", R.drawable.casedetail_menu_record_p);
				map.put("enable", viewState);
				menuList.add(map);
			}
			//照片水印 平安
			if (tag.equals(mRoleTypeName[5])) {
				//logoId = R.drawable.pingan_logo;
				logoId = R.drawable.logo_renbao;
			}
			//照片水印 远东
			if (tag.equals(mRoleTypeName[11])) {
				logoId = R.drawable.logo_yuandong;
			}
			
			//拒绝任务
//			if (tag.equals(mRoleTypeName[12])) {
//				HashMap<String, Object> map = new HashMap<String, Object>();
//				map.put("title", resultRoleType.get(i).get("title"));
//				map.put("tag", tag);
//				map.put("picN", R.drawable.casedetails_break_bg_selector);
//				map.put("picP", R.drawable.casedetails_break_back);
//				map.put("enable", viewState);
//				menuList.add(map);
//			}
			
			
			//用户电话
			if (tag.equals(mRoleTypeName_defauly[0])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_userphone_bg_selector);
				map.put("picP", R.drawable.casedetails_phone_back);
				map.put("enable", true);
				menuList.add(map);
			}
			//完成任务
			if (tag.equals(mRoleTypeName_defauly[1])) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", resultRoleType.get(i).get("title"));
				map.put("tag", tag);
				map.put("picN", R.drawable.casedetails_finishcase_bg_selector);
				map.put("picP", R.drawable.casedetails_finishcase_back);
				map.put("enable", viewState);
				menuList.add(map);
			}
			
		}
		return menuList;
	}
	
	private void initview() {
		
		Intent intent = getIntent();
		mTID = intent.getStringExtra("TID");

		mContentValues = DBOperator.getTask(mTID);


		mCaseNo =  mContentValues.get(DBModle.Task.CaseNo).toString();
		mTasktype = mContentValues.get(DBModle.Task.TaskType).toString();
		mUserPhone =mContentValues.get(DBModle.Task.Telephone).toString();
		mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
		mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;
		mFrontOperator = mContentValues.get(DBModle.Task.FrontOperator).toString();
		mLinkCaseNo = mContentValues.getAsString(DBModle.Task.LinkCaseNo);

		mPath_str = G.STORAGEPATH + mLinkCaseNo +"/"+mTasktype+"/";

		mNopicture_layout = (View)this.findViewById(R.id.nopicture_layout);

		mGridView = (GridView) this.findViewById(R.id.gridview);

		mTackpicture_button = (Button) this.findViewById(R.id.case_detail_bottombar_capture_image);
		mTackpicture_button.setOnClickListener(mTackpictureButtonOnClickListener);
		
		String frontState =  mContentValues.getAsString(DBModle.Task.FrontState);
		
		if(frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE) || frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE)){
			mTackpicture_button.setEnabled(false);
		}
		
		
		mEdit_layout = (View)this.findViewById(R.id.edit_layout);
		mEdit_Button = (Button)this.findViewById(R.id.editbutton);
		mEdit_Button.setOnClickListener(mEditButtonOnClickListener);
		mOff_Button = (Button)this.findViewById(R.id.offbutton);
		mOff_Button.setOnClickListener(mOffButtonOnClickListener);
		mSure_Button = (Button)this.findViewById(R.id.surebutton);
		mSure_Button.setOnClickListener(mSureButtonOnClickListener);
		
		if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)||frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE)||frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE)){
		viewState = false;
		}

		// 初始化动画
		mAnimation_show = AnimationUtils.loadAnimation(this,R.anim.casedetails_mapply_layout_show);
		mAnimation_hide = AnimationUtils.loadAnimation(this,R.anim.casedetails_mapply_layout_hide);


		mUserClass = UserManager.getInstance().getUserClass();
		if(mUserClass.equals(Main.OVERHAUL)){
//			View repairLayout = (View)this.findViewById(R.id.repair_layout);
//			repairLayout.setVisibility(View.GONE);
//			View moreLayout = (View)this.findViewById(R.id.more_layout);
//			moreLayout.setVisibility(View.GONE);
		}
		
		
		initCaseDetais(mContentValues);
		
		if(mUserClass.equals(Main.OVERHAUL)){
			DBOperator.updateTaskSate(mTID, DBModle.Task.IsNew,"0");//更新表中的角标IsNew字段
		}
		
		
	}
	
	class AsynInitView extends AsyncTask<String, Void, ArrayList<HashMap<String, Object>>>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			
		}
		@Override
		protected ArrayList<HashMap<String, Object>> doInBackground(String... params) {
			
			if (params!=null&&params.length>0&&params[0]!=null&&!params[0].trim().equals("")) {
				return getPictures(params[0]);
			}else {
				return getPictures(mPath_str);
			}
			
		}
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, Object>> result) {
			super.onPostExecute(result);
			if(result.size() == 0){
				mNopicture_layout.setVisibility(View.VISIBLE);
			}else {
				mNopicture_layout.setVisibility(View.GONE);
			}
			mGridViewAdapterr.notifyDataSetChanged();
			if (result.size()>2) {
				gallery.setSelection(1);
			}
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
	public ArrayList<HashMap<String, Object>> getPictures(String path){
		File file = new File(path);
		if(!file.exists()){
			file.mkdir();
		}
		mPaths_list.clear();
		HashMap<String, Object> map;
		File[] files = file.listFiles();
		if(files != null){
			for (int i = 0; i < files.length; i++) {
				map = new HashMap<String, Object>();
				map.put("path", files[i].getAbsolutePath());
				map.put("check", true);
				map.put("isUpload", false);
				mPaths_list.add(map);
			}
		}
	
		return mPaths_list;
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
//		boolean b = getPictures();
//		if(b){
//			setGridViewAdapter();
//		}
		AsynInitView initViewTask = new AsynInitView();
		initViewTask.execute();
		
	}


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		if (mApply_layout.getVisibility() == View.VISIBLE) {
//			mApply_layout.setVisibility(View.GONE);
//			//mApply_layout.startAnimation(mAnimation_hide);
//			return;
//		}
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
	private void mRepairCase(){
		ArrayList<ContentValues> caseArrayList = DBOperator.getTasks(mFrontOperator);
		for(ContentValues values : caseArrayList){
			String caseNo =  values.get(DBModle.Task.CaseNo).toString();
			String taskType = values.getAsString(DBModle.Task.TaskType);
			String TID =values.getAsString(DBModle.Task.TID);
			if(mCaseNo.equals(caseNo)&&taskType.equals(mContext.getResources().getString(R.string.tasktype_recommend))){
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
	
	/**
	 * 连接案件
	 */
	private void linkCase(){
		Bundle bundle = new Bundle();
		bundle.putString("TID", mTID);

		Intent intent = new Intent(mContext,DependenceCase.class);
		intent.putExtras(bundle);
		startActivityForResult(intent,DependenceCase.LINKCASE);
	}
	
	/**
	 * 拒绝任务
	 */
	private void refuseCase(){
		
		Dialog.negativeAndPositiveDialog(mContext, getString(R.string.casedetails_dialog_refuse_case), new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				boolean b = DBOperator.refuseTask(mTID, mLongitude, mLatitude);
				if(b){
					UploadWork.sendDataChangeBroadcast(mContext);		
					dialog.dismiss();
					Intent intent = new Intent();
					setResult(CaseManager.CASESTATE, intent);
					finish();
				}else {
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
		});
	}

	/**
	 * 三者车牌
	 */
	private void thirdCar(){
		final EditText editText = new EditText(mContext);
		Dialog.edittextDialog(mContext, mContext.getResources().getString(R.string.casedetails_thirdcar_dialog), editText, new AlertDialog.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String thirdCar = editText.getText().toString();
				ContentValues contentValues = new ContentValues();
				contentValues.put(DBModle.Task.ThirdCar, thirdCar);
				
				if( DBOperator.updateTask(mTID, contentValues)){
					DBOperator.uploadCaseService(mTID,mContentValues.getAsString(DBModle.Task.FrontState));
					UploadWork.sendDataChangeBroadcast(mContext);		
					ContentValues values2 = DBOperator.getTask(mTID);
					initCaseDetais(values2);
				}
					
				 /*if(DBOperator.uploadCaseService(contentValues , mTID, mLongitude , mLatitude , mContentValues.getAsString(DBModle.Task.FrontState))){
					 UploadWork.sendDataChangeBroadcast(mContext);		
					 ContentValues values2 = DBOperator.getTask(mTID);
					 initCaseDetais(values2);
				 }*/
			}
		});
	}
	
	/**
	 * 补拍与完成之间的转换
	 */
	private void supplyFinishTackPicture(){
		mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
		mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;
		String frontState = mContentValues.getAsString(DBModle.Task.FrontState);
		if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
			String taskType = mContentValues.getAsString(DBModle.Task.TaskType);
			String operator = mContentValues.getAsString(DBModle.Task.FrontOperator);
			String accessToken = UserManager.getInstance().getAccessToken();
			if(DBOperator.SupplyPictureState( mLongitude ,mLatitude , mCaseNo,mTID,taskType,operator,"","0",accessToken,DBModle.TaskLog.FrontState_SUPPLY)){
			   UploadWork.sendDataChangeBroadcast(mContext);		
			   mContentValues = DBOperator.getTask(mTID);			   
		    }
		} 
	}
	
	private String linkCaseNo;
	/** 拍照 */
	OnClickListener mTackpictureButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//开始查勘
			PictureManager.initInstance(mContext).setStorageCallback(new IstoragePictureListener() {
				@Override
				public PictureParameters setStorageParameters(long dateTaken) {
					linkCaseNo  = mContentValues.getAsString(DBModle.Task.LinkCaseNo);
					//更新位置信息
					PictureParameters parameters = new PictureParameters();						
					parameters.setDirectory(mPath_str);
					parameters.setLatitude(Double.parseDouble(mLatitude));
					parameters.setLongtitude(Double.parseDouble(mLongitude));
					//保险公司-车牌号-报案号_任务类型_时间_编号
					ArrayList<HashMap<String, Object>> pictureList = getPictures(mPath_str);
					int picture = pictureList.size()+1;
					String title =  G.getPhoneCurrentTime("yyyyMMddHHmmss") + "_" + picture ;
					
					parameters.setTitle(title);
					parameters.setTime(G.getPhoneCurrentTime());
					parameters.setResId(logoId);//水印logo
					parameters.setUserNo(UserManager.getInstance().getUserName()+"-"+getVersionName());//员工编号
					parameters.setColor(getResources().getColor(R.color.orange));
					parameters.setAlpha(true);
					return parameters;
				}

				/**获取当前版本号*/
				public String getVersionName() {
					String version = "";
					try {
						// 获取packagemanager的实例
						PackageManager packageManager = getPackageManager();
						// getPackageName()是你当前类的包名，0代表是获取版本信息
						PackageInfo packInfo;
						packInfo = packageManager.getPackageInfo(getPackageName(), 0);
						version = packInfo.versionName;
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return version;
				}
				
				@Override
				public void afterStorage(String path, boolean success) {
					if(success){
						boolean bb =DBOperator.addPicture(mTID,linkCaseNo, path);
						if(bb){	
							DBOperator.updateTaskSate(mTID, DBModle.TaskLog.FrontState_START, mLongitude, mLatitude);
							UploadWork.sendDataChangeBroadcast(mContext);							
							Log.d(TAG, "插入照片成功 :");						
							supplyFinishTackPicture();
							
						}else{
							Log.e(TAG, "插入照片失败 :");
							
							JSONObject jsonObject = new JSONObject();
							try {
								jsonObject.put("CaseDetails2  Tackpicture()  afterStorage", "插入照片失败:"+G.getPhoneCurrentTime() );
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							CatchException.saveException(mContext, jsonObject);
							
						}
						
						//测试用 插入测试用照片
						/*String[] last = path.split("/");
						last[5] = "test";
						StringBuffer testPath = new StringBuffer();
						for(String str : last){
							testPath = testPath.append( str+"/" );
						}
						boolean b =DBOperator.addPicture(mTID,mLinkCaseNo+"_test", testPath.toString());
						Log.d(TAG, "test 插入照片:" +b);
						UploadWork.sendDataChangeBroadcast(mContext);*/		
					}
				}
			});


			String path = mContentValues.getAsString(DBModle.Task.ImgPath);
			if(path!= null&&path.equals("")){
				ContentValues values = new ContentValues();
				values.put(DBModle.Task.ImgPath, mPath_str);
				DBOperator.updateTask(mTID, values);
			}
			
			//打开拍照 
			Intent intent = new Intent(CaseDetails2.this, CameraMain.class);
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
	
	
	
	/**
	 * 销案
	 */
	private void clearTask(){
	
		
		Dialog.negativeAndPositiveDialog(this, getResources().getString(R.string.casedetails_cleartask_tips), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				 String state = null;
				 String tips = null;
				 mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
				 mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;
				if (mPaths_list.size()>=3) {
					state = DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE;
				}else {
					state = DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE;
				}
				if (DBOperator.clearTask(mTID, mLongitude, mLatitude, state)) {
					tips = mContext.getResources().getString(R.string.casedetails_cleartask_suc);
					UploadWork.sendDataChangeBroadcast(mContext);
				}else {
					tips = mContext.getResources().getString(R.string.casedetails_cleartask_fail);
				}
				Dialog.positiveDialog(CaseDetails2.this, tips, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent();
						setResult(CaseManager.CASESTATE, intent);
						finish();
					}
				});
			}
		});
	}

	/**
	 * 校验上传列表中的数据是否有漏掉
	 */
	public boolean checkTaskStack(){
		boolean isadd = false;
		ArrayList<HashMap<String, Object>> localPictureList = getPictures(mPath_str);
		ArrayList<ContentValues> allPictureList = DBOperator.getPicturesSum(mTID,"0,1,2");
		if(localPictureList.size() != allPictureList.size()){
			for (HashMap<String, Object> localMap : localPictureList) {
				String localPath = localMap.get("path").toString();
				for(ContentValues allValues : allPictureList){
					String allValuesPaths = allValues.getAsString("Vals");
					if(allValuesPaths.contains(localPath)){
						localMap.put("isUpload", true);
					}
				}
			}
			
			
			for(HashMap<String, Object> localMap : localPictureList){
				if(!(Boolean) localMap.get("isUpload")){
					DBOperator.addPicture(mTID,mContentValues.getAsString(DBModle.Task.LinkCaseNo), localMap.get("path").toString());
					UploadWork.sendDataChangeBroadcast(mContext);	
					isadd = true;
				}
			}
		}
		return isadd;
	}
	
	
	/**完成任务 */
	private void finishCase(){
		ArrayList<ContentValues> pictureList = DBOperator.getPicturesSum(mTID,"1");
		if(!pictureList.isEmpty()){
			Dialog.negativeDialog(mContext, getString(R.string.casedetails_picturenoupdateload));
			return;
		}
		
		if(checkTaskStack()){
			Dialog.negativeDialog(mContext, getString(R.string.casedetails_picturenoupdateload_upload));
			return;
		}
		
		ArrayList<ContentValues> pictureList2 = DBOperator.getPicturesSum(mTID,"3");
		if(!pictureList2.isEmpty()){
			Dialog.negativeAndPositiveDialog(mContext,  getString(R.string.casedetails_picture_lose), new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					sureSummit();
				}
			});
		}else{
			sureSummit();
		}
		
	}
	
	public void sureSummit(){
		//重新加载一次数据
		mContentValues = DBOperator.getTask(mTID);
		mLongitude = BaiduLocation.instance().LOCAITON_LONITUDE;
		mLatitude = BaiduLocation.instance().LOCAITON_LATITUDE;
		String estprice="0";
		ContentValues recommendValues = DBOperator.getTask(mCaseNo, mContext.getResources().getString(R.string.tasktype_recommend));
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
						final float f2 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						
						Dialog.negativeAndPositiveDialog(mContext, getString(R.string.casedetails_success), new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								boolean b = DBOperator.finishTask(String.valueOf(f2), mTID, mLongitude, mLatitude);
								if(b){
									UploadWork.sendDataChangeBroadcast(mContext);
									Intent intent = new Intent();
									setResult(CaseManager.CASESTATE, intent);
									finish();
								}else {
									Dialog.negativeDialog(mContext,mContext.getResources().getString(R.string.casedetails_fail));
								}
							}
						});
					}
				}
			});
		}
	}


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

//		if (mApply_layout.getVisibility() == View.VISIBLE && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
//			mApply_layout.setVisibility(View.GONE);
//			mApply_layout.startAnimation(mAnimation_hide);
//			return false;
//		}

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
	private void reportLargeCase(){
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
			if(caseNo.equals(mCaseNo)&&(tasktype.equals(mContext.getResources().getString(largeOrdanger))||tasktype.equals(mContext.getResources().getString(largeOrdanger)))){
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
	private void dangerCaseReport(){
		Bundle bundle = new Bundle();
		bundle.putString("mTID", mTID);
		bundle.putString("TaskType",DANGER);

		if(verify(R.string.tasktype_danger)){
			Intent intent = new Intent(mContext,Report.class);
			intent.putExtras(bundle);
			//				startActivityForResult(intent, REPORT);
			startActivity(intent);
		}
	}

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
			String linkPath =  G.STORAGEPATH + linkCaseNo+"/"+mTasktype+"/";
			new AsynInitView().execute(linkPath);
			break;
		default:
			break;
		}

	}




}
