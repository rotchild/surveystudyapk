package com.whhcxw.MobileCheck;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.mail.EmailManager;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.global.G;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

public class Report extends BaseActivity {

    private final String TAG = this.getClass().getName();
	private Context mContext;
    private Titlebar2 titlebar;
    private Button mCouncilor_button;
    private Button mCouncilor_decrease_button;
    /**添加区域督导员 姓名列表*/
    private String mCouncilors="";
    /**抄送姓名列表*/
    private String mCopys="";
    private Button mCopy_button;
    private Button mCopy_decrease_button;
    private TextView mCouncilor_TextView;
    
    private TextView mCouncilor_text;
    
    private TextView mCopy_TextView;
    /**督导*/
    public static final int COUNCILOR = 110;
    /**抄送*/
    public static final int COPY = 111;
    
    public static final int MAIL = 888;
    private String mLargeOrDanger;
    private String mTID;
    private String mCaseNo;
    private String mTaskType;
    private ArrayList<Map<String, Object>> mCouncilorData_list;
    private ArrayList<Map<String, Object>> mCopyData_list;
    
    
//    private EditText mReport_text;
    private ContentValues mContentValues;
    
    private String mLeader;
    private String mOther="";
    
    private Button mSend_Button;
    /**
     * 添加内文本内容
     */
    private Button addContent_button;
    
    private TextView previewText;
    
    private EmailManager manager;
    

    private String mEmailContent = "";
    
    @SuppressLint("UseSparseArrays")
	private HashMap<Integer, String> contentMap = new HashMap<Integer, String>();
    private String mailContent = ""; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag==0) finish();
		setContentView(R.layout.report);
		mContext = this;
		initTitle();
		initView();
		
		manager = new EmailManager(this);
	}
	
	public void initTitle() {
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);
//		titlebar.showRight(titleRightOnClickListener);
//		titlebar.setRightBackground(mContext.getResources().getDrawable(R.drawable.ui_titlebar_bg_selector));
//		titlebar.setRightText(R.string.title_send);
	}
	
	public void initView(){
		Intent intent = getIntent();
		mTID = intent.getExtras().getString("mTID");
		mLargeOrDanger = intent.getExtras().getString("TaskType");
		mContentValues = DBOperator.getTask(mTID);
		mCaseNo = mContentValues.getAsString(DBModle.Task.CaseNo);
		mTaskType = mContentValues.getAsString(DBModle.Task.TaskType);
		mCouncilor_text = (TextView)this.findViewById(R.id.councilor_text);
		if(mLargeOrDanger.equals(CaseDetails.LARGE)){
			mCouncilor_text.setText(R.string.report_councilor);
		}else {
			mCouncilor_text.setText(R.string.report_danger);
		}
		
		mCouncilor_TextView = (TextView)this.findViewById(R.id.councilor);
		mCopy_TextView = (TextView)this.findViewById(R.id.copy);
		
		mCouncilor_button = (Button)this.findViewById(R.id.councilor_button);
		mCouncilor_button.setOnClickListener(mCouncilorbuttonOnClickListener);
		mCouncilor_decrease_button = (Button)this.findViewById(R.id.councilor_decrease_button);
		mCouncilor_decrease_button.setOnClickListener(mCouncilorDecreaseButtonOnClickListener);
		
		mCopy_button = (Button)this.findViewById(R.id.copy_button);
		mCopy_button.setOnClickListener(mCopybuttonOnClickListener);
		mCopy_decrease_button = (Button)this.findViewById(R.id.copy_decrease_button);
		mCopy_decrease_button.setOnClickListener(mCopyDecreaseButtonOnClickListener);
		
//		mReport_text = (EditText)this.findViewById(R.id.edittext);
		
		mSend_Button = (Button)this.findViewById(R.id.sent);
		mSend_Button.setOnClickListener(titleRightOnClickListener);
		
		addContent_button = (Button) findViewById(R.id.report_mail_content_btn);
		addContent_button.setOnClickListener(mOnAddContentClick);
		
		previewText = (TextView) findViewById(R.id.report_mail_preview);
		previewText.setMovementMethod(ScrollingMovementMethod.getInstance());  
		previewText.setVisibility(View.GONE);
		
	}
	
	
	/**titlebar发送*/
	OnClickListener titleRightOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			String Memo = mReport_text.getText().toString();
//			String Memo = "";
//			String memoDispose =  editDispose(Memo);  || memoDispose.equals("")
			
			
			if(mLeader == null || mEmailContent.equals("")){
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.report_checkoutcouncilor));
				return;	
			}
			String largerDanger = "";
			if(mLargeOrDanger.equals(CaseDetails.LARGE)){
				largerDanger = mContext.getResources().getString(R.string.tasktype_large);
			}else if(mLargeOrDanger.equals(CaseDetails.DANGER)){
				largerDanger = mContext.getResources().getString(R.string.tasktype_danger);
			}
//			关于案件92200002700017938779大案上报
			manager.addSubject(getString(R.string.report_report)+mCaseNo+largerDanger+getString(R.string.report_report_send));
			
			//发送邮件
			manager.sendEmail();
			
			//[{message:"车子有问题，风险案件。",reply:"继续做",date:” yyyy-MM-dd HH:mm:ss”}]
			JSONArray memoJson = new JSONArray();			
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("date",G.getPhoneCurrentTime());
				jsonObject.put("message", "");
				jsonObject.put("username",UserManager.getInstance().getUserName());
				memoJson.put(jsonObject);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG, "Memo 组装成json格式失败:" + e);
			}
			mContentValues.put(DBModle.Task.Memo, memoJson.toString());
			mContentValues.remove(DBModle.Task.TID);
			mContentValues.put(DBModle.Task.BackOperator, mLeader);
			mContentValues.put(DBModle.Task.BackState, DBModle.TaskLog.FrontState_FINISH);
			mContentValues.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_FINISH);
			mContentValues.put(DBModle.Task.Latitude, BaiduLocation.instance().LOCAITON_LATITUDE);
			mContentValues.put(DBModle.Task.Longitude, BaiduLocation.instance().LOCAITON_LONITUDE);
			mContentValues.put(DBModle.Task.Watcher, mOther);
			
			String imgpath = G.STORAGEPATH + mCaseNo +"/"+mContentValues.getAsString(DBModle.Task.TaskType)+"/";
			mContentValues.put(DBModle.Task.ImgPath, imgpath);
			if(mLargeOrDanger.equals(CaseDetails.LARGE)){
				mContentValues.put(DBModle.Task.TaskType, mContext.getResources().getString(R.string.tasktype_large));
			}else if(mLargeOrDanger.equals(CaseDetails.DANGER)){
				mContentValues.put(DBModle.Task.TaskType, mContext.getResources().getString(R.string.tasktype_danger));
			}
			
			boolean b = DBOperator.repostTask(mContentValues, mLeader, mOther, "b");
			if(b){
				UploadWork.sendDataChangeBroadcast(mContext);
				
				finish();
			}else{
				Dialog.negativeDialog(mContext, mContext.getResources().getString(R.string.report_dialog_mess));
			}
		}
	};
	

	
	/**添加抄送*/
	OnClickListener mCopybuttonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putSerializable("datalist", mCopyData_list);
			
			Intent intent = new Intent(mContext,ReportCopy.class);
			intent.putExtra("bundle", bundle);
			startActivityForResult(intent, 0);
		}
	};
	
	
	/**减少抄送*/
	OnClickListener mCopyDecreaseButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putSerializable("datalist", mCopyData_list);
			
			Intent intent = new Intent(mContext,ReportCopy.class);
			intent.putExtra("bundle", bundle);
			startActivityForResult(intent, 0);
		}
	};
	

	/**添加区域督导    风险调查员*/
	OnClickListener mCouncilorbuttonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putSerializable("datalist", mCouncilorData_list);
			bundle.putString("LargeOrDanger", mLargeOrDanger);
			
			Intent intent = new Intent(mContext,ReportCouncilor.class);
			intent.putExtra("bundle", bundle);
			startActivityForResult(intent, 0);
			
		}
	};
	
	/**减少区域督导    风险调查员*/
	OnClickListener mCouncilorDecreaseButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Bundle bundle = new Bundle();
			bundle.putSerializable("datalist", mCouncilorData_list);
			bundle.putString("LargeOrDanger", mLargeOrDanger);
			
			Intent intent = new Intent(mContext,ReportCouncilor.class);
			intent.putExtra("bundle", bundle);
			startActivityForResult(intent, 0);
		}
	};
	/**添加文本内容*/
	OnClickListener mOnAddContentClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(Report.this, ReportMail.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("map", contentMap);
			bundle.putString("text", mailContent);
			bundle.putString("filePath",  G.STORAGEPATH + mCaseNo + "/" + mTaskType +"/");
			intent.putExtras(bundle);
			startActivityForResult(intent,MAIL);
			
		}
	};
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mCouncilors="";
		mCopys = "";
		super.onResume();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		switch (resultCode) {
		case COUNCILOR:
			String leader="";
			Bundle bundle = data.getExtras().getBundle("bundle");
			ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) bundle.getSerializable("datalist");
			String[] mLeaderEmail = new String[1];
			mCouncilorData_list=list;
			for(int i = 0;i<list.size();i++){
				if((Boolean) list.get(i).get("check")){
					mCouncilors += list.get(i).get(DBModle.Users.RealName)+",";
					leader +=list.get(i).get(DBModle.Users.UserName)+"|";
					mLeaderEmail[i] = list.get(i).get(DBModle.Users.Email).toString();
				}
			}
				mCouncilor_TextView.setText(mCouncilors.substring(0, mCouncilors.length() - 1));
				mLeader = leader.substring(0, leader.length() - 1);
				//设置Button的编辑状态
				mCouncilor_button.setVisibility(View.GONE);
				mCouncilor_decrease_button.setVisibility(View.VISIBLE);
				
				//添加督导或者风险Email
				manager.addEmailTO(mLeaderEmail);
			break;

		case COPY:
			String other="";
			
			Bundle bundle2 = data.getExtras().getBundle("bundle");
			
			ArrayList<Map<String, Object>> list2 = (ArrayList<Map<String, Object>>) bundle2.getSerializable("datalist");
			mCopyData_list=list2;
			String emails = "";
			for(int i = 0;i<list2.size();i++){
				if((Boolean) list2.get(i).get("check")){
					mCopys += list2.get(i).get(DBModle.Users.RealName)+",";
					other += list2.get(i).get(DBModle.Users.UserName)+"|";
					emails += list2.get(i).get(DBModle.Users.Email)+",";
					
//					copyEmail[i] = list2.get(i).get(DBModle.Users.Email).toString();
				}
			}
			
				String email = emails.substring(0, emails.length() - 1);
				String[] copyEmail = email.split(",");
				
				mCopy_TextView.setText(mCopys.substring(0, mCopys.length() - 1));
				mOther = other.substring(0, other.length() - 1);
				//设置Button的编辑状态
				mCopy_button.setVisibility(View.GONE);
				mCopy_decrease_button.setVisibility(View.VISIBLE);
				
				//添加抄送
				manager.addEmailCC(copyEmail);
				
			break;
		case MAIL:
			Bundle b = data.getExtras();
			mailContent = b.getString("text");
			ArrayList<File> files = (ArrayList<File>) b.getSerializable("files");
			mEmailContent = mailContent;
			contentMap = (HashMap<Integer, String>) b.getSerializable("map");
			if(contentMap.isEmpty()){
				previewText.setVisibility(View.GONE);
			}else {
				previewText.setVisibility(View.VISIBLE);
			}
			previewText.setText(mailContent+"\n\n"+"附件："+files.size()+"张照片");
			manager.addEmailContent(mailContent);
			manager.addEmailPhotos(manager.convertFilesToUris(files));
			break;
		default:
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	/**
	 * 多行输入框 内容处理\n,\r,\f,\t 
	 * @param str
	 * @return 处理后的Str
	 */
	public static String editDispose(String str){
		return str.replaceAll("\t|\f|\r|\n", "");
	}
	
	
}
