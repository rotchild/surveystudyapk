package com.whhcxw.MobileCheck;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.adapter.LargerDangerCaseDetailsGridViewAdapter;
import com.whhcxw.camera.picture.PictureManager;
import com.whhcxw.global.G;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

public class LargerDangerCaseDetails extends BaseActivity {

	private final String TAG = "LargerDangerCaseDetails";
	
	private String mPath_str;
	private String mCaseNo;
	private String mTasktype;
	private String mTaskID;
	private String mAccessToken;
	// private ArrayList<String> mPaths_list;
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

	private TextView mMemo_TextView;

	private ContentValues mContentValues;

	private View mEdittext_layout;
	private String mUserClass;
	private String mUserName;

	private EditText mEdittext;
	private Button mSend_Button;
	
	
	private GridView mGridView;
	private LargerDangerCaseDetailsGridViewAdapter mGridViewAdapterr;
	
	public static final int LARGERDANGER = 55;

	private JSONArray mReplyJsonArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.main_largerdanger_casedetails);
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
				intent.putExtra("isNewData", false);
				setResult(LARGERDANGER, intent);
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
		}
		
		
	}

	private void initview() {
		
		Intent intent = getIntent();
		mContentValues = intent.getParcelableExtra("values");
		mCaseNo = mContentValues.get(DBModle.Task.CaseNo).toString();
		mTaskID = mContentValues.getAsString(DBModle.Task.TaskID);
		mTasktype = mContentValues.get(DBModle.Task.TaskType).toString();

		mUserName = UserManager.getInstance().getUserName();
		mUserClass = UserManager.getInstance().getUserClass();
		mAccessToken = UserManager.getInstance().getAccessToken();
		

		mCar_TextView = (TextView) this.findViewById(R.id.car);
		mCar_TextView.setText(mContentValues.get(DBModle.Task.CarMark).toString());
		mCasenumber_TextView = (TextView) this.findViewById(R.id.casenumber);
		mCasenumber_TextView.setText(mContentValues.get(DBModle.Task.CaseNo).toString());
		mCarmaster_TextView = (TextView) this.findViewById(R.id.carmaster);
		mCarmaster_TextView.setText(mContentValues.get(DBModle.Task.CarOwner).toString());
		mPhone_TextView = (TextView) this.findViewById(R.id.phone);
		mPhone_TextView.setText(mContentValues.get(DBModle.Task.Telephone).toString());
		mTime_TextView = (TextView) this.findViewById(R.id.time);
		mTime_TextView.setText(mContentValues.get(DBModle.Task.AccidentTime).toString());
		mAddress_TextView = (TextView) this.findViewById(R.id.address);
		mAddress_TextView.setText(mContentValues.get(DBModle.Task.Address).toString());
		mMemo_TextView = (TextView) this.findViewById(R.id.memo);
		
		mEdittext = (EditText)this.findViewById(R.id.edittext);
		mSend_Button = (Button)this.findViewById(R.id.button);
		mSend_Button.setOnClickListener(mSendButtonOnClickListener);
		
		if(mContentValues.getAsString(DBModle.Task.BackState).equals(DBModle.TaskLog.FrontState_FINISH)){
			mSend_Button.setEnabled(false);
		}

		
		mEdittext_layout = (View) this.findViewById(R.id.edittext_layout);
		mGridView = (GridView) this.findViewById(R.id.gridview);

		if(mUserClass.equals(Main.SURVEY)){
			mEdittext_layout.setVisibility(View.GONE);
		}
		
		View reply_layout = (View)this.findViewById(R.id.reply_layout);
		TextView reply_TextView = (TextView)this.findViewById(R.id.reply);
		try {
			String a = mContentValues.get(DBModle.Task.Memo).toString();
			mReplyJsonArray = new JSONArray(a);
			for(int i = 0;i<mReplyJsonArray.length();i++){
				JSONObject	mReplyJSObject = mReplyJsonArray.getJSONObject(i);
				String username = mReplyJSObject.getString("username");
				if(mUserClass.equals(Main.SURVEY)){
					if(username.equals(mUserName)){
						mMemo_TextView.setText(mReplyJSObject.getString("message"));
					}else{
						reply_layout.setVisibility(View.VISIBLE);
						reply_TextView.setText(mReplyJSObject.getString("message"));
					}
				}else if(mUserClass.equals(Main.SUPERVISOR) || mUserClass.equals(Main.DANGER)){
					if(username.equals(mUserName)){
						reply_layout.setVisibility(View.VISIBLE);
						String replay=mReplyJSObject.getString("message");
						reply_TextView.setText(replay);
					}else{
						mMemo_TextView.setText(mReplyJSObject.getString("message"));
					}
				}
			}
			
			
			if(mReplyJsonArray.length() == 2){
				ContentValues values = new ContentValues();
				values.put(DBModle.Task.IsNew, "2");
				values.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_FINISH);
				//修改isnew的状态
				if(mUserClass.equals(Main.SURVEY)){
					DBOperator.updateTask(mCaseNo,mTasktype,values);
				}
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Memo error" +e.getMessage());
		} 
	
		if (mUserClass.equals(Main.SURVEY)) {
			mPath_str = mContentValues.getAsString(DBModle.Task.ImgPath);
			if(mPath_str.contains(mContext.getResources().getString(R.string.tasktype_danger))||mPath_str.contains(mContext.getResources().getString(R.string.tasktype_large))){
				mPath_str = G.STORAGEPATH + mContentValues.getAsString(DBModle.Task.CaseNo)+"/"+mContext.getResources().getString(R.string.tasktype_survey)+"/";
			}
			loadSurveyData(mPath_str);
		} else if (mUserClass.equals(Main.DANGER)|| mUserClass.equals(Main.SUPERVISOR)) {
			mPath_str = G.STORAGEPATH + mCaseNo.split("_")[0] + "/" + mTasktype + "/";
			downloadPictures();
		}
		judgeNewCase();
	}
	
	/**判断是否是新案件   并删除Preferences中的记录*/
	private void judgeNewCase(){
		try {
			SharedPreferences preferences = mContext.getSharedPreferences(PushManager.PREFERENCES_PUSH, Context.MODE_PRIVATE);
			JSONArray notifyListArray = new JSONArray(preferences.getString("notifyList", "[]"));
			JSONArray array = new JSONArray();
			for (int i = 0; i < notifyListArray.length(); i++) {
				JSONObject object = notifyListArray.getJSONObject(i);
				if(object.getString("caseno").equals(mCaseNo) && object.getString("tasktype").equals(mTasktype) && object.getBoolean("isread") == false){
					Log.d(TAG, "The first read so delete the pushmanager record");
					if(notifyListArray.length() == 1){
						Editor editor = preferences.edit(); 
						editor.putString("notifyList", array.toString()).commit();
						
					}
				}else {
					Editor editor = preferences.edit(); 
					array.put(object);
					editor.putString("notifyList", array.toString()).commit();
					
				}
			}
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("MainCaseAdapter", "PushManager.PREFERENCES_PUSH get value fail ："+e.getMessage());
		}
	}
	
	
	/**督导发送案件描述*/
	OnClickListener mSendButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String text = mEdittext.getText().toString();
			if(text.equals("")){
				Dialog.negativeDialog(mContext, getString(R.string.largerdangercasedetails_nomess));
				return;
			}
			
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("date", G.getPhoneCurrentTime());
				jsonObject.put("message", Report.editDispose(text));
				jsonObject.put("username", mUserName);
				mReplyJsonArray.put(jsonObject);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG, "Memo 组装成json格式失败:" + e);
			}
			
//			mReplyJsonArray.put(mReplyJSObject);
			
			DataHandler dataHandler = new DataHandler(mContext);
			String UpdateFileds =  DBModle.Task.Memo+"|"+DBModle.Task.BackState;
			String UpdateValues = mReplyJsonArray.toString()+"|"+DBModle.TaskLog.FrontState_FINISH;
			dataHandler.ReplyTask(mCaseNo, mTaskID, mTasktype, mUserName,UpdateFileds, UpdateValues, mAccessToken,mContentValues.getAsString(DBModle.Task.FrontOperator),responseHandler);
			
			
			if(mContentValues.getAsBoolean("isnew")){
				JSONArray newJsonArray = new JSONArray();
				try {
					SharedPreferences preferencesCase = mContext.getSharedPreferences(Main.PREFERENCES_TASK, Context.MODE_PRIVATE);
					String isNewCase = preferencesCase.getString("task", "");
					JSONArray isNewJsonArray = new JSONArray(isNewCase);
					for (int i = 0; i < isNewJsonArray.length(); i++) {
						JSONObject object = (JSONObject) isNewJsonArray.get(i);
						String jsonCaseNo = object.getString(DBModle.Task.CaseNo);
						String jsonCaseType = object.getString(DBModle.Task.TaskType);
						if(jsonCaseNo.equals(mCaseNo)&&jsonCaseType.equals(mTasktype)){
							object.put("isnew", false);
						}
						newJsonArray.put(object);
					}
					Editor editor = preferencesCase.edit();
					editor.putString("task", newJsonArray.toString());
					editor.commit();
					
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			Intent intent = new Intent();
			intent.putExtra("isNewData", true);
			setResult(LARGERDANGER, intent);
					
		}
	};
	
	HttpResponseHandler responseHandler = new HttpResponseHandler() {

		@Override
		public void response(boolean success, String response, Throwable error) {
			// TODO Auto-generated method stub
			
			if(success){
				try {
					JSONObject jsonObject = new JSONObject(response);
					String code = jsonObject.getString("code");
					if(!code.equals("0")){
						String cause = jsonObject.getString("cause");
						Log.e(TAG, "responseHandler back error"+cause);
						Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_largedangerfail));
					}else {
						finish();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_largedangerfail));
					return;
				}
				
			}else {
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.casedetails_largedangerfail));
				
			}
		}
	};
	
	
	private void loadSurveyData(String path){
		List<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
		File[] files = new File(path).listFiles();
		if(files != null && files.length > 0){			
			for (File file : files) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("survey_path", file.getAbsolutePath());
				map.put("server_path", "");
				data.add(map);
			}			
		}else{
			Log.d(TAG, "file not item path:"+path);
		}
		setGridViewAdapter(data);
	}

	/** 督导、风险进入时 从服务器上下载图片 */
	public void downloadPictures() {
		DataHandler dataHandler = new DataHandler(mContext);
		dataHandler.GetPictures(mCaseNo, mAccessToken,
				new HttpResponseHandler() {
					@Override
					public void response(boolean success, String response,Throwable error) {
						// TODO Auto-generated method stub
						List<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
						if (success) {							
							try {
								JSONObject jsonObject = new JSONObject(response);
								String code = jsonObject.getString("code");
								if (!code.equals("0")) {
									String cause = jsonObject.getString("cause");
									G.showToast(mContext, cause, true);
									return;
								}
								JSONArray piclistObject = (JSONArray) jsonObject.getJSONObject("value").getJSONArray("pic_list");
								Log.i(TAG, piclistObject.toString());								
								for (int i = 0; i < piclistObject.length(); i++) {
									JSONObject item = piclistObject.getJSONObject(i);
									HashMap<String, String> map = new HashMap<String, String>();
									map.put("survey_path", "");
									map.put("server_path", item.getString("ImagePath"));
									data.add(map);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.e(TAG, "downloadPictures  error:" + String.valueOf(e));
								return;
							}
						}
						setGridViewAdapter(data);
					}
				});
	}

	/** 获取照片路径 */
	public ArrayList<String> getPictures(String mPath_str) {
		ArrayList<String> mPaths_list = new ArrayList<String>();
		File file = new File(mPath_str);
		if (!file.exists()) {
			file.mkdir();
		}

		File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				mPaths_list.add(files[i].getAbsolutePath());
			}
		}
		return mPaths_list;
	}

	/** 设置照片的加载 */
	public void setGridViewAdapter(List<HashMap<String, String>> data) {
		if(mGridViewAdapterr == null){
			mGridViewAdapterr = new LargerDangerCaseDetailsGridViewAdapter(mContext, data, mUserClass, mPath_str);
			mGridView.setOnItemClickListener(browseOnClickListener);
			mGridView.setAdapter(mGridViewAdapterr);
			mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		}else{
			mGridViewAdapterr.nitifyData(data);
		}
		
	}

	/** 预览照片 */
	OnItemClickListener browseOnClickListener = new OnItemClickListener() {

		@SuppressWarnings("static-access")
		@Override
		public void onItemClick(AdapterView<?> view, View arg1, int postion,long arg3) {
			if(mUserClass.equals(Main.SURVEY)){
				PictureManager.initInstance(mContext).showPicture(getPictures(mPath_str), mContext, postion);
			}else{
				LargerDangerCaseDetailsGridViewAdapter adapter = (LargerDangerCaseDetailsGridViewAdapter) view.getAdapter();
				@SuppressWarnings("unchecked")
				HashMap<String, String> map = (HashMap<String, String>) adapter.getItem(postion);
				
				String picPath = map.get("survey_path");
				PictureManager.initInstance(mContext).showPicture(getPictures(mPath_str), mContext, picPath,postion);
			}

//			 PictureManager.initInstance(mContext).showPicture(path,mContext);
		}

	};

	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			intent.putExtra("isNewData", false);
			setResult(LARGERDANGER, intent);
			finish();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
