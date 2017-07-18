package com.whhcxw.MobileCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.pushservice.PushConstants;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.service.NetService;
import com.whhcxw.MobileCheck.service.SnycService;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.MainCaseAdapter;
import com.whhcxw.adapter.MainDeleteCaseAdapter;
import com.whhcxw.adapter.MainPagerAdapter;
import com.whhcxw.androidcamera.NetCameraService;
import com.whhcxw.global.G;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.pushservice.PushMessageReceiver;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.PullDownListView;
import com.whhcxw.ui.Titlebar2;
import com.whhcxw.updateapp.UpdateAppDialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class Main extends BaseActivity implements OnClickListener,OnPageChangeListener,PullDownListView.OnRefreshListioner {

	private final String TAG = this.getClass().getName();
	/**
	 * 数据有改变
	 * */
	public static boolean DATA_CHEANGE = false;
	
	private Context mContext;
	//private ImageView mIageView;
	private Display mDisplay;
	private int currentPage = 0;
	private Bitmap mBitmap;
	private int disPlayWidth, offset;
	private ArrayList<View> mArrayList;
//	private View mCurrent_layout;
//	private View mFinish_layout;
//	private View mAll_layout;
//	private View mDelete_layout;
	private ViewPager mViewPager;
	/**创建任务*/
	private View mSet_layout;
	private View mMain_set_layout;
	private View mMain_pushmessage_layout;
	private View mMain_into_layout;
	private View mMain_create_layout;
	private View mSearch_layout;
	private Button mSearch_Button;
	private Titlebar2 titlebar;
//	private ImageView mCurrent_image;
//	private ImageView mFinish_image;
//	private ImageView mAll_image;
//	private ImageView mDelete_image;
	
	
	/*
	 * private TextView mCurrent_text;
	private TextView mFinish_text;
	private TextView mAll_text;
	private TextView mDelete_text;*/
	
	
	private RadioButton current_radioButton,finish_radioButton,all_radioButton,delete_radioButton;
	
	/**当前页面标枳*/
	public int casePage=0;
	/**当前任务*/
	public static final int CURRENT=0;
	/**完成任务*/
	public static final int FINISH=1;
	/**所有任务*/
	public static final int ALL=2;
	/**删除任务*/
	public static final int DELETE=3;
	private Animation mAnimation_show;
	private Animation mAnimation_hide;
	/**2,查勘员*/
	public static final String SURVEY="2";
	/**6,区域督导*/
	public static final String SUPERVISOR="6";
	/**8,风险调查员*/
	public static final String DANGER="8";
	/**9，修理厂接待员*/
	public static final String RECEPTIONIST = "9";
	
	/**7，拆检员*/
	public static final String OVERHAUL = "7";
	
	
	/**2,查勘员  6,区域督导  8,风险调查员 9，修理厂接待员   7,拆检员 */
	private String mUserClass;
	private String mUserName;
	private String mAccessToken;
	private JSONArray mAllData_JsonArray;
	private ArrayList<ContentValues> mCurrentData_list = new ArrayList<ContentValues>();
	private ArrayList<ContentValues> mFinishData_list = new ArrayList<ContentValues>();
	private View mCurrentLayout;
	private View mCurrentNocase_Layout;
	private View mFinishLayout;
	private View mFinishNocase_Layout;
	private View mAllLayout;
	private View mAllNocase_Layout;
	private View mDeleteLayout;
	private View mDeleteNocase_Layout;
	private View mBottom_layout;
	private View mBottom_button_layout;
	private Button mDelete_Button;
	private Button mCancel_Button;
	private View mCheckConnection_layout;
	
	private ArrayList<Map<String, String>> mDelete_Button_Date ;
	private ArrayList<ContentValues> mArrayList_data = new ArrayList<ContentValues>();
//	private Bundle mBundle;
	private PullDownListView mCurrentPullDownView;
	private PullDownListView mFinishPullDownView;
	private PullDownListView mAllPullDownView;
	private PullDownListView mDeletePullDownView;
	/**很出程序*/
	public static final int EXIT = 1;
	/**创建任务*/
	public static final int FrontState_CREATE = 2;
	/**完成任务*/
	public static final int FrontState_FINISH = 5;
	/**注销任务*/
	public static String LOGOUT="logout";
	private MainDeleteCaseAdapter mMainDeleteCaseAdapter;
	private AutoCompleteTextView mSearch_AutoCompleteTextView;
	/** 文字是否编辑完毕 */
	private boolean isTextChangeFinish = false;
	private View mRepairfactory_layout;
	private TextView mFactoryname_TextView;
	private TextView mFactoryaddress_TextView;
	private TextView mFactorymoney_TextView;
    private Button mDeletetext_Button;
	private Handler mHandler = new Handler();
	public static final String PREFERENCES_TASK = "preferences_task";
//	private String isNewCase=""; 
	public static final String NEWTASKTRENDS = "com.whhcxw.broadcaset.newtasktrends";
	public static final String PROGESSBARRECEIVE = "com.whhcxw.broadcaset.progessbarreceive";
	public static final String INTENT_NEWTASK = "FROM_PUSHSERVICE_TASK";
	public static final String NOTCONNECTTION = "com.whhcxw.broadcaset.notconnection";
	
	
	private boolean isLargerDangerReturn = false;
	private DataHandler mDataHandler;
	private MainCaseAdapter mMainCaseAdapter;
	public boolean isExit = false;
	private final int FROM_PUSH_DATA = 0;
	private int getTaskDataType = FROM_PUSH_DATA;
	
	
	private ProgressDialog mProgressDialog;
	public static final String SNYCPROGRESS = "com.whhcxw.broadcaset.snycprogess";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//隐藏标题
		setContentView(R.layout.main);
		mContext = this;
		
		
		if(!UserManager.getInstance().isLogin() || UserManager.getInstance().getRoleTypes().equals("")){
			startActivity(new Intent(mContext,Login.class));
			finish();
			return;
		}
		
		mDataHandler = new DataHandler(mContext); 
		
		mUserClass = UserManager.getInstance().getUserClass();
		mUserName = UserManager.getInstance().getUserName();		
		mAccessToken = UserManager.getInstance().getAccessToken();
		
		Log.d(TAG, " onCreate before server" + G.getPhoneCurrentTime());
		
		//弹出升级APP 对话框
		UpdateAppDialog.showUpdateAppDialog(Main.this,G.UPDATE_APP_SAVE_PATH,R.drawable.logo_code_orange);
		//启动服务
		Intent service = new Intent(mContext,NetService.class);
		startService(service);
		
		PushManager.startBindWork(this, PushConstants.LOGIN_TYPE_API_KEY, PushMessageReceiver.API_KEY, UserManager.getInstance().getUserName());
		PushConstants.restartPushService(mContext);

		if(mUserClass.equals(SURVEY)||mUserClass.equals(OVERHAUL)){
			startVideoService();
		}
		
		initView();
		
		MobileCheckApplocation.activityInitFlag=1;
	}
	
	HttpResponseHandler httpResponseHandler = new HttpResponseHandler(){
		@Override
		public void response(boolean success, String response, Throwable error) {
			// TODO Auto-generated method stub
			try {
				JSONObject jsonObject = new JSONObject(response);
				String code = jsonObject.getString("code");
				if(code.equals("0")){
					Log.d(TAG, "Main baidu Location success");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};
	
	/**启动视频服务*/
	private void startVideoService(){
		Intent ServiceIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("sign",Integer.parseInt(UserManager.getInstance().getUserID()));

		String ip= HttpParams.getHost(mContext);
		bundle.putString("serverIP" , ip);
		bundle.putInt("captureW", 352);
		bundle.putInt("caputreH", 288);
		ServiceIntent.putExtras(bundle);
		ServiceIntent.setClass(mContext, NetCameraService.class);
		startService(ServiceIntent);
	}

	public void initTitle(){
		titlebar = (Titlebar2)findViewById(R.id.titlebar);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
//		titlebar.setRightBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.showLeft(searchOnClickListener);
		titlebar.setLeftImageRes(R.drawable.ui_search);
		
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
		}
		
		titlebar.showRight(createOnClickListener);
		titlebar.setRightImageRes(R.drawable.ui_title_set);
	}
	
	/**初始化数据*/
	public void initView(){
		initTitle();
		
		mDisplay = this.getWindowManager().getDefaultDisplay();
		disPlayWidth = mDisplay.getWidth();

		mSearch_layout=(View)this.findViewById(R.id.title_search);
		mSearch_Button = (Button)this.findViewById(R.id.button);
		mSearch_Button.setOnClickListener(finishSearchOnClickListener);
		mDeletetext_Button = (Button)this.findViewById(R.id.deletetext);
		mDeletetext_Button.setOnClickListener(mDeletetextButtonOnClickListener);
		
		current_radioButton = (RadioButton) findViewById(R.id.current_RadioButton);
		finish_radioButton = (RadioButton) findViewById(R.id.finish_RadioButton);
		all_radioButton = (RadioButton) findViewById(R.id.all_RadioButton);
		delete_radioButton = (RadioButton) findViewById(R.id.delete_RadioButton);
		
		current_radioButton.setOnClickListener(this);
		finish_radioButton.setOnClickListener(this);
		all_radioButton.setOnClickListener(this);
		delete_radioButton.setOnClickListener(this);
		
		current_radioButton.setChecked(true);
		
//		mCurrent_image = (ImageView)this.findViewById(R.id.current_image);
//		mFinish_image = (ImageView)this.findViewById(R.id.finish_image);
//		mAll_image = (ImageView)this.findViewById(R.id.all_image);
//		mDelete_image = (ImageView)this.findViewById(R.id.delete_image);
//		
//		mCurrent_text = (TextView)this.findViewById(R.id.current_text);
		/*
		mFinish_text = (TextView)this.findViewById(R.id.finish_text);
		mAll_text = (TextView)this.findViewById(R.id.all_text);
		mDelete_text = (TextView)this.findViewById(R.id.delete_text);*/
		
		mCheckConnection_layout = (View)this.findViewById(R.id.checkconnection_layout);
		
		mBitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.main_move);
		offset = (disPlayWidth / 4 - mBitmap.getWidth()) / 2;
//		mCurrent_layout = this.findViewById(R.id.current_layout);
//		mFinish_layout = this.findViewById(R.id.finish_layout);
//		mAll_layout = this.findViewById(R.id.all_layout);
//		mDelete_layout = this.findViewById(R.id.delete_layout);

		mViewPager = (ViewPager) this.findViewById(R.id.viewpager);
//		mCurrent_layout.setOnClickListener(this);
//		mFinish_layout.setOnClickListener(this);
//		mAll_layout.setOnClickListener(this);
//		mDelete_layout.setOnClickListener(this);
		
		mSet_layout = (View)this.findViewById(R.id.set_layout);
		mSet_layout.setOnClickListener(mSetlayoutOnClickListener);
		
		mMain_create_layout = (View)this.findViewById(R.id.main_create_layout);
		mMain_create_layout.setOnClickListener(mMainCreateLayoutOnClickListener);
		
		mMain_into_layout = (View)this.findViewById(R.id.main_into_layout);
		mMain_into_layout.setOnClickListener(mMainIntoLyoutOnClickListener);

		
		
		
		if(mUserClass.equals(SURVEY) || mUserClass.equals(OVERHAUL)){
			View line1 = (View)this.findViewById(R.id.line1);
			View line2 = (View)this.findViewById(R.id.line2);
			delete_radioButton.setVisibility(View.VISIBLE);
			mMain_create_layout.setVisibility(View.VISIBLE);
			line1.setVisibility(View.VISIBLE);
			if(mUserClass.equals(SURVEY)){
				//江西 显示，山东隐藏
				String[] recieves = (UserManager.getInstance().getRoleTypes()).split(",");
				for(int i = 0 ; i < recieves.length ; i++){
					if(recieves[i].equals(getString(R.string.RoleType_M014_name))){
						mMain_into_layout.setVisibility(View.VISIBLE);
						line1.setVisibility(View.VISIBLE);
						line2.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		
		mMain_set_layout = (View)this.findViewById(R.id.main_set_layout);
		mMain_set_layout.setOnClickListener(mMainSetLyoutOnClickListener);
		
		mMain_pushmessage_layout = (View)this.findViewById(R.id.main_pushmessage_layout);
		mMain_pushmessage_layout.setOnClickListener(mMainPushMessageLyoutOnClickListener);
		
		mBottom_layout = (View)this.findViewById(R.id.bottom_layout);
		mBottom_button_layout = (View)this.findViewById(R.id.bottom_button_layout);
		mDelete_Button = (Button)this.findViewById(R.id.delete_button);
		mDelete_Button.setOnClickListener(mDeleteButtonOnClickListener);
		mCancel_Button = (Button)this.findViewById(R.id.cancel_button);
		mCancel_Button.setOnClickListener(mCancelButtonOnClickListener);
		
		mSearch_AutoCompleteTextView = (AutoCompleteTextView)this.findViewById(R.id.autoCompleteTextView);
		mSearch_AutoCompleteTextView.addTextChangedListener(mSearchAutoCompleteTextViewTextWatcher);
		
		mProgressDialog = ToolsProgressDialog.getInitProgressDialog(mContext, mContext.getResources().getString(R.string.main_loading)).showProgressDialog();
		
		
		if(mUserClass.equals(RECEPTIONIST)){
			mRepairfactory_layout = (View)this.findViewById(R.id.repairfactory_layout);
			mRepairfactory_layout.setVisibility(View.VISIBLE);
			mFactoryname_TextView = (TextView)this.findViewById(R.id.factoryname);
			mFactorymoney_TextView = (TextView)this.findViewById(R.id.factorymoney);
			mFactoryaddress_TextView = (TextView)this.findViewById(R.id.factoryaddress);
			
			TextView areanameTextView = (TextView)this.findViewById(R.id.areaname);
			
			String areaID = UserManager.getInstance().getAreaID();
			ContentValues contentValues = DBOperator.getArea(areaID);
			if(contentValues != null){
				areanameTextView.setText(contentValues.getAsString(DBModle.Areas.AreaName));
			}
			getFactory();
		}
		
		// 初始化动画
		mAnimation_show = AnimationUtils.loadAnimation(this,R.anim.main_set_layout_show);
		mAnimation_hide = AnimationUtils.loadAnimation(this,R.anim.main_set_layout_hide);
		
		setPage();
	}
	
	/**创建短信任务*/
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			automaticCreateCase();
		}
	};
	
	/**
	 * 短信来时，自动创建短信
	 */
	private void automaticCreateCase(){
		ArrayList<ContentValues> messageSourceList = DBOperator.getMessageSources();
		for (ContentValues contentValues : messageSourceList) {
			int isRead = contentValues.getAsInteger(DBModle.MessageSources.IsRead);
			if(isRead == 1){
				String mes = "";
				try {
					String Contents = contentValues.getAsString(DBModle.MessageSources.Contents);
					JSONObject jsonObject = new JSONObject(Contents);
					mes = jsonObject.getString("msg");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				IntoCase intoCase = new IntoCase();
				String username = UserManager.getInstance().getUserName();
				ContentValues caseValues = intoCase.parserSMS(mes,username,mContext);
				
				boolean b = DBOperator.createTask(caseValues);
				if(b){
					Log.d(TAG, "message case craete success");
					
					String SMSID = contentValues.getAsString(DBModle.MessageSources.SMSID);
					ContentValues updateValues = new ContentValues();
					updateValues.put(DBModle.MessageSources.IsRead, 0);
					
//					G.showToast(mContext, mContext.getResources().getString(R.string.main_create_message_success), true);

					if(DBOperator.updateMessageSources(SMSID, updateValues)){
						Log.d(TAG, "update MessageSource suceess");
						setPage();
					}else {
						Log.d(TAG, "update MessageSource fail");
					}
				}else {
					Log.d(TAG, "message case craete fail");
				}
			}
		}
	}
	
	/**获取维修厂接待员所在的维修厂信息*/
	public void getFactory(){
		mDataHandler.setIsShowProgressDialog(true);
		mDataHandler.GetGarageInfo(mUserName, mAccessToken, getFactoryHttpResponseHandler);
	}
	
	HttpResponseHandler getFactoryHttpResponseHandler = new HttpResponseHandler(){
		@Override
		public void response(boolean success, String response, Throwable error) {
			// TODO Auto-generated method stub
			JSONObject factoryJSONObject = new JSONObject();
			try {
					JSONObject jsonObject  = new JSONObject(response);
					String code = jsonObject.getString("code");
				
					//先把从服务器取出来的数据，转换为ArrayList<ContentValues>类型
					if(success && code.equals("0")){
						factoryJSONObject = jsonObject.getJSONObject("value").getJSONObject("garage_info");
						String garagename = factoryJSONObject.getString(DBModle.Garage.ShortName);
						if(garagename.equals("")){
							garagename = factoryJSONObject.getString(DBModle.Garage.FullName);
						}
						mFactoryname_TextView.setText(garagename);
						
						mFactorymoney_TextView.setText(getString(R.string.main_yang)+factoryJSONObject.getString(DBModle.Garage.MonthDone)+"/"+getString(R.string.main_yang)+factoryJSONObject.getString(DBModle.Garage.MoneyLimit));
						mFactoryaddress_TextView.setText(factoryJSONObject.getString(DBModle.Garage.Address));
					}
				}catch (Exception e) {
					// TODO: handle exception
				}
		}
		
	};
	
	
	@Override
	protected void onStart() {
		super.onStart();
//		PushManager.activityStarted(this);/		
		registerReceiver();
		Log.d(TAG,"regist data");
		progessbarReceiver();
		notConnectionReceiver();
		snycReceiver();
		if(DATA_CHEANGE){
			Log.d(TAG, "data change data refresh" + G.getPhoneCurrentTime());			
			getData();
			DATA_CHEANGE = false;
		}
		
		boolean  mCheckConnection_isshow = UserManager.getInstance().getIsConnection();
		if(mCheckConnection_isshow){
			mCheckConnection_layout.setVisibility(View.GONE);
		}else{
			mCheckConnection_layout.setVisibility(View.VISIBLE);
		}
		
		/**自动创建短信任务*/
		if(mUserClass.equals(SURVEY)){
			 Handler handler = new Handler();
			 handler.post(runnable);
		}
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		BaiduLocation.instance().start();
		Log.d(TAG, " onResume start server finish" + G.getPhoneCurrentTime());
		isExit = false;
	}

	/**进度条广播*/
	private void progessbarReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(PROGESSBARRECEIVE);
		mContext.registerReceiver(broadcastReceiver,filter);
	}
	
	/**查勘员角色  注册一个广播*/
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(NEWTASKTRENDS);
		mContext.registerReceiver(broadcastReceiver, filter);
	}
	/**
	 * 有网络，但是不能连接到服务
	 */
	private void notConnectionReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(NOTCONNECTTION);
		mContext.registerReceiver(broadcastReceiver, filter);
	}
	
	/**同步数据广播*/
	private void snycReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(SNYCPROGRESS);
		mContext.registerReceiver(broadcastReceiver,filter);
	}
	

	/**广播接收者*/
	public BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals(NEWTASKTRENDS)){
				if(intent.getStringExtra("yaner").equals(getString(R.string.jiangxi))){
					automaticCreateCase();
				}else {
					getData();
				}
			} else if(action.equals(PROGESSBARRECEIVE)){
				HeaderViewListAdapter CurrentAdapter = (HeaderViewListAdapter)mCurrentPullDownView.mListView.getAdapter();				
				MainCaseAdapter CurrentMainAdapter = (MainCaseAdapter)CurrentAdapter.getWrappedAdapter();
				CurrentMainAdapter.notifyDataSetChanged();
				
//				HeaderViewListAdapter FinishAdapter = (HeaderViewListAdapter)mFinishPullDownView.mListView.getAdapter();				
//				MainCaseAdapter FinishMainAdapter = (MainCaseAdapter)FinishAdapter.getWrappedAdapter();
//				FinishMainAdapter.notifyDataSetChanged();
				
				HeaderViewListAdapter AllPullDownViewAdapter = (HeaderViewListAdapter)mAllPullDownView.mListView.getAdapter();				
				MainCaseAdapter AllMainAdapter = (MainCaseAdapter)AllPullDownViewAdapter.getWrappedAdapter();
				AllMainAdapter.notifyDataSetChanged();
			} else if(action.equals(NOTCONNECTTION)){
				String SuccessOrFailure = intent.getStringExtra("SuccessOrFailure");
				if(SuccessOrFailure.equals("Failure")){
					if(!mCheckConnection_layout.isShown()){
						mCheckConnection_layout.setVisibility(View.VISIBLE);	
					}
				
				}else{
					if(mCheckConnection_layout.isShown()){
						mCheckConnection_layout.setVisibility(View.GONE);	
					}
				}
			} else if(action.equals(SNYCPROGRESS)){
				String showOrDisappear = intent.getExtras().getString("showOrDisappear");
				if(showOrDisappear.equals("show")){
					mProgressDialog.show();
				} else if(showOrDisappear.equals("disappear")){
					mProgressDialog.dismiss();
				}
			}
		}
	};
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mSet_layout.getVisibility() == View.VISIBLE) {
			mSet_layout.setVisibility(View.GONE);
			//mSet_layout.startAnimation(mAnimation_hide);
		}
		BaiduLocation.instance().stop();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		PushManager.activityStoped(this);
		try{
			//注销广播		
			unregisterReceiver(broadcastReceiver);
			Log.d(TAG,"regist data stop");
			}catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, "not resister unregisterReceiver ERROR:" + e.getMessage());
			}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ((mSet_layout.getVisibility() == View.VISIBLE)&& (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
			mSet_layout.setVisibility(View.GONE);
			mSet_layout.startAnimation(mAnimation_hide);
			return false;
		}else if(mSearch_layout.getVisibility() == View.VISIBLE && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)){
			finishSearch();
			return false;
		}else if(keyCode == KeyEvent.KEYCODE_BACK){
			if(!isExit){
				isExit = true;
				G.showToast(mContext, getString(R.string.main_exit), false);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**创建任务*/
	OnClickListener createOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mSet_layout.getVisibility()==View.GONE){
			mSet_layout.setVisibility(View.VISIBLE);
			mSet_layout.startAnimation(mAnimation_show);
			}
			
		}
	};
		
	//搜索事件
	OnClickListener searchOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			titlebar.setVisibility(View.GONE);
			mSearch_layout.setVisibility(View.VISIBLE);
		}
	};
	
	/**跳转创建任务*/
	OnClickListener mMainCreateLayoutOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			Intent intent = new Intent(mContext,CreateCase.class);
////			intent.putExtras(mBundle);
//			startActivityForResult(intent, FrontState_CREATE);
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext,CreateCase2.class);
//			intent.putExtras(mBundle);
			startActivityForResult(intent, FrontState_CREATE);
			
		}
	};
	
	/**导入任务*/
	OnClickListener mMainIntoLyoutOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext,IntoCase.class);
//			intent.putExtras(mBundle);
			startActivityForResult(intent,IntoCase.INTOCASE);
		}
	};
	
	/**系统设置*/
	OnClickListener mMainSetLyoutOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext,SystemSetting.class);
//			intent.putExtras(mBundle);
			startActivityForResult(intent, EXIT);
		}
	};

	/**系统设置*/
	OnClickListener mMainPushMessageLyoutOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(mContext,PushMessageList.class);
			startActivity(intent);
		}
	};
	
	/**消失更多应用*/
	OnClickListener mSetlayoutOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mSet_layout.getVisibility() == View.VISIBLE) {
				mSet_layout.setVisibility(View.GONE);
				mSet_layout.startAnimation(mAnimation_hide);
				return;
			}
		}
	};
	
	/**取消搜索栏*/
	OnClickListener finishSearchOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finishSearch();
		}
	};
	
	/**清空搜索框中的内容*/
	OnClickListener mDeletetextButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String serchStr = mSearch_AutoCompleteTextView.getText().toString();
			if(serchStr.equals("")){
				finishSearch();
			}else {
				mSearch_AutoCompleteTextView.setText("");
			}
		}
	};
	
	/**取消搜索框*/
	private void finishSearch(){
		titlebar.setVisibility(View.VISIBLE);
		mSearch_layout.setVisibility(View.GONE);
		mSearch_AutoCompleteTextView.setText("");
		
		try{
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (inputMethodManager!=null) inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**获取数据*/
	public void getData(){
		Log.d(TAG, "data refresh" + G.getPhoneCurrentTime());
		//是查勘员时，从本地调取数据  督导员、风险调查员从服务上读取数据
		if(mUserClass.equals(SURVEY) || mUserClass.equals(OVERHAUL)){
				mArrayList_data = DBOperator.getTasks(mUserName);
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					splitData(mArrayList_data);
					
				}
			});
			
		}else if(mUserClass.equals(SUPERVISOR) || mUserClass.equals(DANGER) || mUserClass.equals(RECEPTIONIST)) {
			//每次在请求服务之前，先获取缓存在本地的上一次请求的数据
//			SharedPreferences preferencesCase = mContext.getSharedPreferences(PREFERENCES_TASK, Context.MODE_PRIVATE);
//			isNewCase = preferencesCase.getString("task", "[]");
			
			
			mDataHandler.setIsShowProgressDialog(true);
			mDataHandler.GetTaskList(mUserClass, mUserName, mAccessToken, currentHttpResponseHandler);
		}
	}
	
	HttpResponseHandler currentHttpResponseHandler = new HttpResponseHandler(){
		@Override
		public void response(boolean success, String response, Throwable error) {
			// TODO Auto-generated method stub
			if(success){
				mArrayList_data = new ArrayList<ContentValues>();
				try {
					JSONObject jsonObject  = new JSONObject(response);
					String code = jsonObject.getString("code");
					//先把从服务器取出来的数据，转换为ArrayList<ContentValues>类型
					if(code.equals("0")){
						mAllData_JsonArray = jsonObject.getJSONObject("value").getJSONArray("task_list");	
						
						JSONObject object;
						ContentValues values;
						for(int i=0;i<mAllData_JsonArray.length();i++){
							values = new ContentValues();
							object = (JSONObject) mAllData_JsonArray.get(i);
							values.put(DBModle.Task.TaskID, object.getString(DBModle.Task.TaskID));
							values.put(DBModle.Task.CaseNo, object.getString(DBModle.Task.CaseNo));
							values.put(DBModle.Task.CarMark , object.getString(DBModle.Task.CarMark ));
							values.put(DBModle.Task.CarOwner, object.getString(DBModle.Task.CarOwner));
							values.put(DBModle.Task.CarDriver , object.getString(DBModle.Task.CarDriver ));
							values.put(DBModle.Task.Telephone , object.getString(DBModle.Task.Telephone));
							values.put(DBModle.Task.Address , object.getString(DBModle.Task.Address));
							values.put(DBModle.Task.FrontPrice, object.getString(DBModle.Task.FrontPrice));
							values.put(DBModle.Task.BackPrice , object.getString(DBModle.Task.BackPrice));
							values.put(DBModle.Task.FixedPrice, object.getString(DBModle.Task.FixedPrice));
							values.put(DBModle.Task.Latitude, object.getString(DBModle.Task.Latitude));
							values.put(DBModle.Task.Longitude, object.getString(DBModle.Task.Longitude));
							values.put(DBModle.Task.TaskType, object.getString(DBModle.Task.TaskType));
							values.put(DBModle.Task.FrontOperator, object.getString(DBModle.Task.FrontOperator));
							values.put(DBModle.Task.FrontState, object.getString(DBModle.Task.FrontState));
							values.put(DBModle.Task.BackOperator, object.getString(DBModle.Task.BackOperator));
							values.put(DBModle.Task.BackState, object.getString(DBModle.Task.BackState));
							values.put(DBModle.Task.Watcher, object.getString(DBModle.Task.Watcher));
							values.put(DBModle.Task.AccidentTime, disposeDate(object.getString(DBModle.Task.AccidentTime)));
							values.put(DBModle.Task.Memo, object.getString(DBModle.Task.Memo));
							values.put(DBModle.Task.IsNew, false);
							mArrayList_data.add(values);
						}	
	
					}else{
						//网络请求失败，或者是返回数据失败
						Log.e(TAG, "response return json error" +response );
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				G.showToast(mContext, getString(R.string.network_err), true);
			}
			splitData(mArrayList_data);
		}
	};
	
	/**时间格式的处理*/
	public String disposeDate(String date){
		String month = date.substring(5,10);
		String hour = date.substring(11,16);
		String accidentTime = month +"\u0020\u0020"+hour;
		return accidentTime;
	}
	
	/**
	 * 处理服务器数据 适用督导员，风险调查员，修理厂接待员
	 * @param servicedata 服务器返回数据
	 * @param outCurrentData 当任务数据
	 * @param outFinishData 完成任务数据
	 * @param outAllData 所有任务数据
	 * */
	public void splitServiceData(ArrayList<ContentValues> servicedata,ArrayList<ContentValues> outCurrentData,ArrayList<ContentValues> outFinishData,ArrayList<ContentValues> outAllData){		
		try {
			SharedPreferences preferencesCase = mContext.getSharedPreferences(PREFERENCES_TASK, Context.MODE_PRIVATE);
			String cacheCase = preferencesCase.getString("task", "[]");			
			//适配数据之前，和上次缓存的数据进行对比，是否有更新数据
			JSONArray cacheCaseArray = new JSONArray(cacheCase);
			
			
 			
			for(int i = 0; i < servicedata.size(); i++){
				
				ContentValues contentValues = servicedata.get(i);		
				int falgIndex = -1;
				for (int j = 0; j < cacheCaseArray.length(); j++){		 
					JSONObject caseJson = cacheCaseArray.getJSONObject(j);
					
					if(contentValues.getAsString(DBModle.Task.CaseNo).equals(caseJson.getString(DBModle.Task.CaseNo)) && contentValues.getAsString(DBModle.Task.TaskType).equals(caseJson.getString(DBModle.Task.TaskType))){
						falgIndex = j;
						break;
					}	
				}
				
				if(falgIndex == -1){	
					JSONObject caseJson = new JSONObject();
					caseJson.put(DBModle.Task.CaseNo, contentValues.getAsString(DBModle.Task.CaseNo));
					caseJson.put(DBModle.Task.TaskType, contentValues.getAsString(DBModle.Task.TaskType));
					caseJson.put(DBModle.Task.IsNew, true);					
					cacheCaseArray.put(caseJson);
					contentValues.put(DBModle.Task.IsNew, true);
				}else{
					contentValues.put(DBModle.Task.IsNew, cacheCaseArray.getJSONObject(falgIndex).getBoolean(DBModle.Task.IsNew));
				}
				if(contentValues.getAsString(DBModle.Task.BackState).equals(DBModle.TaskLog.FrontState_FINISH)){
					contentValues.put(DBModle.Task.IsNew, false);
				}
				
				outAllData.add(contentValues);
				
				if(contentValues.getAsString(DBModle.Task.BackState).equals(DBModle.TaskLog.FrontState_FINISH)){
					contentValues.put("check", true);
					outFinishData.add(contentValues);
				}else {
					outCurrentData.add(contentValues);
				}
			}
		
			//每次进行从服务上查数据，都缓存到本地,用于推送消息刷新
			SharedPreferences preferences_task = mContext.getSharedPreferences(PREFERENCES_TASK, Context.MODE_PRIVATE);
			Editor editor = preferences_task.edit();
			editor.putString("task", cacheCaseArray.toString());
			editor.commit();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 处理本地数据 适用查勘员   拆检员
	 * @param servicedata 服务器返回数据
	 * @param outCurrentData 当任务数据
	 * @param outFinishData 完成任务数据
	 * @param outAllData 所有任务数据
	 * @return queryTaskList 返回需查询的案件（大案，风险案件）
	 * */
	public ArrayList<ContentValues> splitLocData(ArrayList<ContentValues> locdata,ArrayList<ContentValues> outCurrentData,ArrayList<ContentValues> outFinishData,ArrayList<ContentValues> outAllData){		
		ArrayList<ContentValues> queryTaskList = new ArrayList<ContentValues>();
		for(int i = 0; i < locdata.size(); i++){
			ContentValues contentValues = locdata.get(i);
			//数据库是否新案件标识 为int 类型 转换成bool 类型
			int isnew = 0;
			if(!isLargerDangerReturn){
				try{
					isnew = contentValues.getAsInteger(DBModle.Task.IsNew);
					if(isnew == 0 || isnew == 2){
						contentValues.put(DBModle.Task.IsNew,false);
					}else{
						contentValues.put(DBModle.Task.IsNew,true);
					}
				}catch (Exception e) {
					// TODO: handle exception
				}	
			}
			
			String taskType = contentValues.getAsString(DBModle.Task.TaskType);
			String backState = contentValues.getAsString(DBModle.Task.BackState);
			
			if((taskType.equals(getString(R.string.tasktype_danger)) || taskType.equals(getString(R.string.tasktype_large))) && backState.equals(DBModle.TaskLog.FrontState_ACCEPT) && isnew == 0){
				queryTaskList.add(contentValues);
			}else if (taskType.equals(getString(R.string.tasktype_diaodu))&& isnew == 0) {
				queryTaskList.add(contentValues);
			}
			
			outAllData.add(contentValues);
			if(contentValues.get(DBModle.Task.FrontState).equals(DBModle.TaskLog.FrontState_FINISH)||contentValues.get(DBModle.Task.FrontState).equals(DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE)||contentValues.get(DBModle.Task.FrontState).equals(DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE)){
				contentValues.put("check", true);
				outFinishData.add(contentValues);
			}else {
				outCurrentData.add(contentValues);
			}
		}
		return queryTaskList;
	}
	
	/**分发数据  并且适配页面*/
	public void splitData(ArrayList<ContentValues> listdata){
		
		//取上次缓存列表
		//和上次缓存列表对比   当前案子在上次缓存列表找不到 caseno + tasktype  找不到就标记为新案件
		//最后会得出一个新的列表， 是否是新案件已标识出来  存储。
		//把最新的列表 放到适配器里。
		//适配器点击该项发现是新案件，找出该项标识为不是新案件。
		
		mCurrentPullDownView.onRefreshComplete();			
		mFinishPullDownView.onRefreshComplete();
		mAllPullDownView.onRefreshComplete();
		if(mUserClass.equals(SURVEY)||mUserClass.equals(OVERHAUL)){
			mDeletePullDownView.onRefreshComplete();
		}
		
		mCurrentData_list = new ArrayList<ContentValues>();
		mFinishData_list  = new ArrayList<ContentValues>();
		
		ArrayList<ContentValues> allTaskData = new ArrayList<ContentValues>();
		if(mUserClass.equals(SURVEY)||mUserClass.equals(OVERHAUL)){
			ArrayList<ContentValues> queryTaskList = splitLocData(listdata, mCurrentData_list, mFinishData_list, allTaskData);
			SnycService taskSync  = new SnycService(mContext, UserManager.getInstance().getAccessToken());
			if(queryTaskList.size() != 0){
				taskSync.snycSurveyTask(queryTaskList,mUserName,mContext);
			}
			
		}else{
			splitServiceData(listdata, mCurrentData_list, mFinishData_list, allTaskData);
		}		
		
		//适配当前任务
		setCaseLayout(mCurrentPullDownView.mListView,mCurrentData_list);
		if(mCurrentData_list.size()==0){
			mCurrentNocase_Layout.setVisibility(View.VISIBLE);
		}else {
			mCurrentNocase_Layout.setVisibility(View.GONE);
		}
		//适配完成任务
		setCaseLayout(mFinishPullDownView.mListView , mFinishData_list);
		if(mFinishData_list.size()==0){
			mFinishNocase_Layout.setVisibility(View.VISIBLE);
		}else {
			mFinishNocase_Layout.setVisibility(View.GONE);
		}
		
		//适配所有任务
		setCaseLayout(mAllPullDownView.mListView, allTaskData);
		if(listdata.size()==0){
			mAllNocase_Layout.setVisibility(View.VISIBLE);
		}else {
			mAllNocase_Layout.setVisibility(View.GONE);
		}
		//删除任务
		if(mUserClass.equals(SURVEY) || mUserClass.equals(OVERHAUL)){
			setDeleteCaseLayout(mDeletePullDownView.mListView, mFinishData_list);
			if(mFinishData_list.size() == 0){
				mDeleteNocase_Layout.setVisibility(View.VISIBLE);
			}else {
				mDeleteNocase_Layout.setVisibility(View.GONE);
			}
		}		
	}
	
	/**页面适配*/
	public void setPage(){
		mArrayList = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mCurrentLayout  = inflater.inflate(R.layout.current_layout, null);
		mArrayList.add(mCurrentLayout);
		mCurrentPullDownView = (PullDownListView) mCurrentLayout.findViewById(R.id.current_pullDownListView);
		mCurrentPullDownView.setRefreshListioner(this);
		mCurrentNocase_Layout = (View)mCurrentLayout.findViewById(R.id.currentnocase_layout);
		
		mFinishLayout =inflater.inflate(R.layout.finish_layout, null);
		mArrayList.add(mFinishLayout);
		mFinishPullDownView = (PullDownListView) mFinishLayout.findViewById(R.id.finish_pullDownListView);
		mFinishPullDownView.setRefreshListioner(this);
		mFinishNocase_Layout = (View)mFinishLayout.findViewById(R.id.finishnocase_layout);
		
		mAllLayout = inflater.inflate(R.layout.all_layout, null);
		mArrayList.add(mAllLayout);
		mAllPullDownView = (PullDownListView) mAllLayout.findViewById(R.id.all_pullDownListView);
		mAllPullDownView.setRefreshListioner(this);
		mAllNocase_Layout = (View)mAllLayout.findViewById(R.id.allnocase_layout);
		
		if(mUserClass.equals(SURVEY) || mUserClass.equals(OVERHAUL)){
			mDeleteLayout = inflater.inflate(R.layout.delete_layout, null);
			mArrayList.add(mDeleteLayout);
			mDeletePullDownView = (PullDownListView)mDeleteLayout.findViewById(R.id.delete_pullDownListView);
			mDeletePullDownView.setRefreshListioner(this);
			mDeleteNocase_Layout = (View)mDeleteLayout.findViewById(R.id.deletenocase_layout);
		}
		
		MainPagerAdapter pagerAdapter = new MainPagerAdapter(mArrayList);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(currentPage);
		mViewPager.setOnPageChangeListener(this);
		getData();
	}
	
	/**适配各种任务数据*/
	public void setCaseLayout(ListView  lisView ,final ArrayList<ContentValues> listdata){
		
		mMainCaseAdapter = new MainCaseAdapter(mContext, listdata, R.layout.main_case_layout_list ,mUserClass,mArrayList_data);		
		lisView.setAdapter(mMainCaseAdapter);
		lisView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long id) {
				// TODO Auto-generated method stub
				final ContentValues contentValues = (ContentValues) listdata.get((int)id);
				String taskType = contentValues.get(DBModle.Task.TaskType).toString();
				String caseNo = contentValues.get(DBModle.Task.CaseNo).toString();
				if(mUserClass.equals(SURVEY) || mUserClass.equals(OVERHAUL)){					
					String frontState = contentValues.get(DBModle.Task.FrontState).toString();					
					String TID = contentValues.get(DBModle.Task.TID).toString();
					if(taskType.equals(mContext.getResources().getString(R.string.tasktype_large))||taskType.equals(mContext.getResources().getString(R.string.tasktype_danger))){
						Intent intent = new Intent(mContext,LargerDangerCaseDetails.class);
						intent.putExtra("values", contentValues);
						startActivityForResult(intent,LargerDangerCaseDetails.LARGERDANGER);
					}else{
						
						if(frontState.equals(DBModle.TaskLog.FrontState_NO_ASSIGN)){
							Intent intent = new Intent(mContext,CaseManager.class);
							intent.putExtra("TID", TID);
							startActivityForResult(intent, CaseManager.CASESTATE);
							
						}else{
							if(taskType.equals(mContext.getResources().getString(R.string.tasktype_recommend))){
								Intent intent = new Intent(mContext,RepairState.class);
								intent.putExtra("TID", TID);
								ContentValues values = DBOperator.getCooperationGarageCase(TID);
								if(values == null){
									values = DBOperator.getGarageCase(TID);
								}
								String cartypeID = values.getAsString(DBModle.Task.CarTypeID);
								if(cartypeID == null || cartypeID.equals("")){
									intent.putExtra("isCooperation", true);
								}else{
									intent.putExtra("isCooperation", false);
								}
								startActivityForResult(intent, CaseManager.CASESTATE);
							}else{
								String backState = contentValues.getAsString(DBModle.Task.BackState);
								if(taskType.equals("拆检")){
									if(backState.equals(DBModle.TaskLog.FrontState_START)){
										Intent intent = new Intent(mContext,CaseDetails2.class);
										intent.putExtra("TID", contentValues.get(DBModle.Task.TID).toString());
										startActivityForResult(intent, CaseManager.CASESTATE);
									}else{
//										Dialog.negativeDialog(mContext, "定损员未接该案件,请稍后操作！");
										Dialog.negativeAndPositiveDialog(mContext, "定损员未接该案件,是否继续操作！", new AlertDialog.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												Intent intent = new Intent(mContext,CaseDetails2.class);
												intent.putExtra("TID", contentValues.get(DBModle.Task.TID).toString());
												startActivityForResult(intent, CaseManager.CASESTATE);
											}
										});
									}
								}else{
									Intent intent = new Intent(mContext,CaseDetails2.class);
									intent.putExtra("TID", contentValues.get(DBModle.Task.TID).toString());
									startActivityForResult(intent, CaseManager.CASESTATE);
								}
								
							}
						}
					}
				}else if(mUserClass.equals(DANGER) || mUserClass.equals(SUPERVISOR)){					
					setCaseRead(caseNo, taskType);
					Intent intent = new Intent(mContext,LargerDangerCaseDetails.class);
					intent.putExtra("values", contentValues);
					startActivityForResult(intent, LargerDangerCaseDetails.LARGERDANGER);
					
				} else if(mUserClass.equals(RECEPTIONIST)){
					setCaseRead(caseNo, taskType);					
					Intent intent = new Intent(mContext,Receptionist.class);
					intent.putExtra("values", contentValues);
					startActivityForResult(intent,LargerDangerCaseDetails.LARGERDANGER);
				}
				
			}
		});
	}
	
	/**删除任务数据*/
	public void setDeleteCaseLayout(ListView  lisView ,final ArrayList<ContentValues> listdata){
		mDelete_Button_Date = new ArrayList<Map<String, String>>();//标识删除数据
		
		mMainDeleteCaseAdapter = new MainDeleteCaseAdapter(mContext, listdata, R.layout.main_delete_case_layout_list,mUserClass,mArrayList_data);
		lisView.setAdapter(mMainDeleteCaseAdapter);
		lisView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long id) {
				// TODO Auto-generated method stub
				
				ContentValues contentValues = (ContentValues) mMainDeleteCaseAdapter.getItem((int) id);
				boolean b = (Boolean) contentValues.get("check");
				if(b){
					if(mDelete_Button_Date.size()==0){
						mBottom_layout.setVisibility(View.GONE);
						mBottom_button_layout.setVisibility(View.VISIBLE);
					}
					contentValues.put("check", false);
					Map<String, String> map = new HashMap<String, String>();
					map.put("TID", contentValues.get(DBModle.Task.TID).toString());
					mDelete_Button_Date.add(map);
					
				}else {
					contentValues.put("check", true);
					for(int i = 0;i<mDelete_Button_Date.size();i++){
						if(mDelete_Button_Date.get(i).get("TID").equals(contentValues.get("TID").toString())){
							mDelete_Button_Date.remove(i);
						}
					}
					
					if(mDelete_Button_Date.size()==0){
						mBottom_layout.setVisibility(View.VISIBLE);
						mBottom_button_layout.setVisibility(View.GONE);
					}
				}
				mMainDeleteCaseAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**删除页面的删除BUTTON*/
	OnClickListener mDeleteButtonOnClickListener =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Dialog.negativeAndPositiveDialog(mContext, mContext.getResources().getString(R.string.main_dialog_mess), new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						int a = -1;
						
						String TIDs="";
						for(int i= 0 ; i<mDelete_Button_Date.size();i++){
							TIDs +=mDelete_Button_Date.get(i).get("TID")+",";
							a = i;
						}
						int result = DBOperator.deleteTask(TIDs.substring(0, TIDs.length()-1));
						if(result == (a+1)){
							deleteDatas();
							getData();
						}
					}
				});
		}
	};

	/**删除页面的取消已选删除BUTTON*/
	OnClickListener mCancelButtonOnClickListener =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			deleteDatas();
		}
	};
	/**隐藏两个按钮，并把数据至空*/
	public void deleteDatas(){
		mBottom_layout.setVisibility(View.VISIBLE);
		mBottom_button_layout.setVisibility(View.GONE);
		for(int i = 0;i<mFinishData_list.size();i++){
			mFinishData_list.get(i).put("check", true);
		}
		if(mUserClass.equals(SURVEY)){
			mDelete_Button_Date.removeAll(mDelete_Button_Date);
			mMainDeleteCaseAdapter.notifyDataSetChanged();
		}
	}
	
	TextWatcher mSearchAutoCompleteTextViewTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			isTextChangeFinish = false;
			mHandler.removeCallbacks(updateListRunnable);
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			isTextChangeFinish = true;
			// 延时500毫秒开始索引
			mHandler.postDelayed(updateListRunnable, 500);
		}
	};
	
	
	Runnable updateListRunnable = new Runnable() {
		public void run() {
			if (isTextChangeFinish) {
				String key = mSearch_AutoCompleteTextView.getText().toString().trim();
				ArrayList<ContentValues> caseValuesList = mCurrentData_list;
				ListView pullDownListView = mCurrentPullDownView.mListView;
				
				switch (currentPage) {
				case 0:
					caseValuesList = mCurrentData_list;
					pullDownListView = mCurrentPullDownView.mListView;
					break;

				case 1:
					caseValuesList = mFinishData_list;
					pullDownListView = mFinishPullDownView.mListView;
					break;
				case 2:
					caseValuesList = mArrayList_data;
					pullDownListView = mAllPullDownView.mListView;
					break;
				case 3:
					if(mUserClass.equals(SURVEY)||mUserClass.equals(OVERHAUL)){
							if (key.equals("")) {
//								setDeleteCaseLayout(mDeletePullDownView.mListView, mFinishData_list);
							} else {
								// List<String> newList = new ArrayList<String>();
								ArrayList<ContentValues> newList = new ArrayList<ContentValues>();
								for (ContentValues caseValues : mFinishData_list) {
									String carMark = caseValues.get(DBModle.Task.CarMark).toString();
									String caseno = caseValues.get(DBModle.Task.CaseNo).toString();
									if (carMark.contains(key)) {
										newList.add(caseValues);
										continue;
									}else if(caseno.contains(key)){
										newList.add(caseValues);
										continue;
								}
								setDeleteCaseLayout(mDeletePullDownView.mListView,newList);
							}
						}
					}
					break;
				default:
					break;
				}
				
				if (key.equals("")) {
					setCaseLayout(pullDownListView,caseValuesList);
				} else {
					// List<String> newList = new ArrayList<String>();
					ArrayList<ContentValues> newList = new ArrayList<ContentValues>();
					for (ContentValues caseValues : caseValuesList) {
						String carMark = caseValues.get(DBModle.Task.CarMark).toString();
						String caseno = caseValues.get(DBModle.Task.CaseNo).toString();
						if (carMark.contains(key)) {
							newList.add(caseValues);
							continue;
						}else if(caseno.contains(key)){
							newList.add(caseValues);
							continue;
						}
					}
					setCaseLayout(pullDownListView,newList);
				}

				Log.d(TAG, "isTextChangeFinish:" + key);
			}
		}
	};
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
//		Log.i("AAA", String.valueOf(arg0));
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		Log.i("BBBB", String.valueOf(arg0));
	}

	@Override
	public void onPageSelected(int arg0) {

		finishSearch();
		
		
		
		int single = mBitmap.getWidth() + offset * 2;
		TranslateAnimation ta = new TranslateAnimation(currentPage * single,single * arg0, 0, 0);
		ta.setFillAfter(true);
		ta.setDuration(200);
		//mIageView.startAnimation(ta);
		currentPage = arg0;
		
		//ColorStateList orange = (ColorStateList) mContext.getResources().getColorStateList(R.color.orange);
		
		switch (arg0) {
		case CURRENT:
//			onPageTitleChange(0);
//			mCurrent_layout.setBackgroundDrawable(bgDrawable);
//			mCurrent_image.setImageResource(R.drawable.main_current_back);
			current_radioButton.setChecked(true);
			//适配当前任务
			mSearch_AutoCompleteTextView.setText("");
//			setCaseLayout(mCurrentPullDownView.mListView,mCurrentData_list);
			
			break;
		case FINISH:
//			onPageTitleChange(1);
//			mFinish_layout.setBackgroundDrawable(bgDrawable);
//			mFinish_image.setImageResource(R.drawable.main_finish_back);
			//mFinish_text.setTextColor(orange);
			finish_radioButton.setChecked(true);
			//适配完成任务
			mSearch_AutoCompleteTextView.setText("");
//			setCaseLayout(mFinishPullDownView.mListView , mFinishData_list);
		
			break;
		case ALL:
//			onPageTitleChange(2);
//			mAll_layout.setBackgroundDrawable(bgDrawable);
//			mAll_image.setImageResource(R.drawable.main_all_back);
			//mAll_text.setTextColor(orange);
			all_radioButton.setChecked(true);
			//适配所有任务
			mSearch_AutoCompleteTextView.setText("");
//			setCaseLayout(mAllPullDownView.mListView, mArrayList_data);
			
			break;
		case DELETE:
			deleteDatas();
//			onPageTitleChange(3);
//			mDelete_layout.setBackgroundDrawable(bgDrawable);
//			mDelete_image.setImageResource(R.drawable.main_delete_back);
			//mDelete_text.setTextColor(orange);
			delete_radioButton.setChecked(true);
			//删除任务
			mSearch_AutoCompleteTextView.setText("");
//			if(mUserClass.equals(SURVEY)){
//				setDeleteCaseLayout(mDeletePullDownView.mListView, mFinishData_list);
//			}
			break;
		default:
			break;
		}
	}
	
	private void onPageTitleChange(int index){
		casePage = index;
		
//		mCurrent_layout.setBackgroundDrawable(null);
//		mFinish_layout.setBackgroundDrawable(null);
//		mAll_layout.setBackgroundDrawable(null);
//		mDelete_layout.setBackgroundDrawable(null);
		
//		mCurrent_image.setImageResource(R.drawable.main_current_front);
//		mFinish_image.setImageResource(R.drawable.main_finish_front);
//		mAll_image.setImageResource(R.drawable.main_all_front);
//		mDelete_image.setImageResource(R.drawable.main_delete_front);	
		/*ColorStateList white = (ColorStateList) mContext.getResources().getColorStateList(R.color.white);
		mCurrent_text.setTextColor(white);
		mFinish_text.setTextColor(white);
		mAll_text.setTextColor(white);
		mDelete_text.setTextColor(white);		*/
	}

	@Override
	public void onClick(View v) {
		int single = mBitmap.getWidth() + offset * 2;
		if (v.getId() == R.id.current_RadioButton) {
			mViewPager.setCurrentItem(0);
			if (currentPage != 0) {
				TranslateAnimation ta = new TranslateAnimation(currentPage * single, 0, 0, 0);
				ta.setFillAfter(true);
				ta.setDuration(200);
				//mIageView.startAnimation(ta);
			}
			currentPage = 0;
		}

		if (v.getId() == R.id.finish_RadioButton) {
			mViewPager.setCurrentItem(1);
			if (currentPage != 1) {
				TranslateAnimation ta = new TranslateAnimation(currentPage * single, single, 0, 0);
				ta.setFillAfter(true);
				ta.setDuration(200);
				//mIageView.startAnimation(ta);
			}
			currentPage = 1;
		}

		if (v.getId() == R.id.all_RadioButton) {
			mViewPager.setCurrentItem(2);
			if (currentPage != 2) {
				TranslateAnimation ta = new TranslateAnimation(currentPage * single, single * 2, 0, 0);
				ta.setFillAfter(true);
				ta.setDuration(200);
				//mIageView.startAnimation(ta);
			}
			currentPage = 2;
		}
		
		if (v.getId() == R.id.delete_RadioButton) {
			mViewPager.setCurrentItem(3);
			if (currentPage != 3) {
				TranslateAnimation ta = new TranslateAnimation(currentPage * single, single * 3, 0, 0);
				ta.setFillAfter(true);
				ta.setDuration(200);
				//mIageView.startAnimation(ta);
			}
			currentPage = 3;
		}
	}
	
	/** 设置案件已读 
	 * @return */
	private boolean setCaseRead(String CaseNo,String TaskType){
		SharedPreferences preferences_task = mContext.getSharedPreferences(PREFERENCES_TASK, Context.MODE_PRIVATE);
		try{
			JSONArray array = new JSONArray(preferences_task.getString("task", "[]"));
			for (int i = 0; i < array.length(); i++) {
				JSONObject caseJson = array.getJSONObject(i);
				if(caseJson.get(DBModle.Task.CaseNo).equals(CaseNo) && caseJson.get(DBModle.Task.TaskType).equals(TaskType)){
					caseJson.put("isnew", false);
				}				
			}
			Editor edit = preferences_task.edit();
			edit.putString("task", array.toString());
			return edit.commit();
		}catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		mSearch_AutoCompleteTextView.setText("");
		getData();
	}
	
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
			switch (resultCode) {
//			case EXIT:				
//				if(data.getStringExtra("logout").equals(Main.LOGOUT)){
//					Intent intent = new Intent(mContext,Login.class);
//					startActivity(intent);
//					finish();
//					((MobileCheckApplocation)getApplication()).logout();
//				}else{					
//					if(data!=null){						
//						finish();
//					}
//					((MobileCheckApplocation)getApplication()).exit();
//				}
//				break;
				
			case FrontState_CREATE:
				getData();
				break;
			
			case CaseManager.CASESTATE:
				getData();
				break;
			case LargerDangerCaseDetails.LARGERDANGER:
				if(mUserClass.equals(SURVEY)){
					getData();
				}else if(mUserClass.equals(DANGER)||mUserClass.equals(SUPERVISOR)){
					isLargerDangerReturn = true;
					if(data.getBooleanExtra("isNewData", false)){
						getData();
					}else {
						splitData(mArrayList_data);
					}
				}
				break;
			case IntoCase.INTOCASE:
				getData();
				break;
				
			case Receptionist.RECEPTIONIST:
				getFactory();
				getData();
				
				break;
		default:
			break;
		}
	}
}
