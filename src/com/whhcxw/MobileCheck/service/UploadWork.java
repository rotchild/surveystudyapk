package com.whhcxw.MobileCheck.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.pushservice.PushConstants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.whhcxw.MobileCheck.MD5;
import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.data.SQLiteManager;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.crashreport.CrashHandler;
import com.whhcxw.global.G;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.pushservice.PushMessageReceiver;
import com.whhcxw.utils.CatchException;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class UploadWork {

	private final String TAG = this.getClass().getSimpleName();	
	
	private List<ContentValues> mData = null;
	private ContentValues mCurrentItem = null;
	private AsyncHttpClient mAsyncHttpClient = null;

	/** 是否开始 */
	private Context mContext;
	private Handler mHandler = new Handler();
	/** 定时刷新时间4分钟 */
	private int mConnectTime = 1000 * 30 ;
	private int mSoTime = 1000 * 60 * 3;
	private int taskTimeout= mConnectTime+mSoTime;
	private int mRefreshTime = taskTimeout+ 1000 * 30;
	
	private DataHandler mDataHandler;
	
	public UploadWork(Context ctx){
		super();
		mContext = ctx;		
		mAsyncHttpClient = new AsyncHttpClient();
		mAsyncHttpClient.addHeader("Charset", "UTF-8");
		mAsyncHttpClient.setTimeout(mConnectTime);//设置连接时间都为15秒
		mAsyncHttpClient.setSoTimeout(mSoTime);//设置传送时间都为90秒
		mDataHandler = new DataHandler(mContext);
		mData=new ArrayList<ContentValues>();
	}
	

	public static NetworkInfo getActiveNetwork(Context context){
	    if (context == null)
	        return null;
	    ConnectivityManager mConnMgr = (ConnectivityManager) context
	            .getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (mConnMgr == null) return null;
	    NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo(); // 获取活动网络连接信息
	    return aActiveInfo;
	}

	public static String UPLOADWORK_DATACHANGE = "uploadwork.receiver.datachange";
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(UPLOADWORK_DATACHANGE) || action.equals(CONNECTIVITY_CHANGE_ACTION) ) {
				if (getActiveNetwork(mContext)!=null){
					mCurrentItem=null;
					notifyDataChange();
				}
			}
			if(action.equals(CONNECTIVITY_CHANGE_ACTION)){
				if((getActiveNetwork(mContext)!=null)){
					mDataHandler.Connection(UserManager.getInstance().getAccessToken(), new HttpResponseHandler(){
						@Override
						public void response(boolean success, String response, Throwable error) {
							// TODO Auto-generated method stub
							try {
								Intent intent = new Intent(Main.NOTCONNECTTION);
								if(success){
									JSONObject jsonObject = new JSONObject(response);
									String code = jsonObject.getString("code");
									if(code.equals("0")){
										UserManager.getInstance().IsConnection(true);
										intent.putExtra("SuccessOrFailure", "Success");
									}else {
										UserManager.getInstance().IsConnection(false);
										intent.putExtra("SuccessOrFailure", "Failure");
									}
								}else{
									UserManager.getInstance().IsConnection(false);
									intent.putExtra("SuccessOrFailure", "Failure");
								}
								mContext.sendBroadcast(intent);
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
					});
				}else {
					UserManager.getInstance().IsConnection(false);
					Intent intent2 = new Intent(Main.NOTCONNECTTION);
					intent2.putExtra("SuccessOrFailure", "Failure");
					mContext.sendBroadcast(intent2);
				}
			}
		}
	};
	
	/**
	 * 发送广播通知有新的任务
	 * */
	public static void sendDataChangeBroadcast(Context ctx){
		 Intent intent = new Intent(UPLOADWORK_DATACHANGE);
		 ctx.sendBroadcast(intent);
	}
	
	/** 注册广播*/
	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	public void registerReceiver(){
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(UPLOADWORK_DATACHANGE);
		mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);		
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		filter.setPriority(1000);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}
	
	/** 解除广播 */
	public void unRegisterReceiver(){
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
	
	/** 开始工作 */
	public void start(){	
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("UploadWork  start()  start time", G.getPhoneCurrentTime());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CatchException.saveException(mContext, jsonObject);
		
		loadData();
		schedulTask();
		mHandler.postDelayed(mReloadRunnable,mRefreshTime);
		registerReceiver();
	}
	
	/** 停止工作 */
	public void stop(){
		mHandler.removeCallbacks(mReloadRunnable);
		unRegisterReceiver();
		Log.d(TAG, "uploadwork service stop");
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("UploadWork stop", "uploadwork service stop");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CatchException.saveException(mContext, jsonObject);
	}
	
	private void loadData(){
		//加入新数据
		mData.clear();
		String selection = DBModle.TaskStack.IsRequest + " in (" + DBModle.TaskStack.IsRequest_Y +" , "+DBModle.TaskStack.IsRequest_F+")";
		ArrayList<ContentValues> cvList=SQLiteManager.getInstance().Select(DBModle.TASKSTACK, new String[] { "*" }, selection, new String[] {}, null,null, "Level asc,StackID asc");
		for (ContentValues cv:cvList){
			if (mCurrentItem!=null && mCurrentItem.getAsInteger("StackID")==cv.getAsInteger("StackID")){
				continue; 
			}
			mData.add(cv);
		}
	}	
	
	/** 通知数据改变 */
	public void notifyDataChange(){
		loadData();
		schedulTask();
	}
	
	public void schedulTask(){
		//从已有的mdata中调度一个待发送的命令，可以多次调用，但是保证只有一个连接运行
		long lastTime=System.currentTimeMillis();
		if (mCurrentItem!=null){lastTime=mCurrentItem.getAsLong("SchedulTime");};//
		/////////////////////////////测试代码需要重新整理
		if (mCurrentItem!=null && (System.currentTimeMillis()-lastTime)>taskTimeout*2){
			mCurrentItem=null;
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("notifyDataChange:强行清空","");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CatchException.saveException(mContext, jsonObject);
		}
		/////////////////////////////////////////////////////////

		if (mCurrentItem!=null && (System.currentTimeMillis()-lastTime)<taskTimeout) {
			Log.d(TAG, "null schedulTask");
			return;//当前任务未完成
		}
		if(mData.size() > 0){
			mCurrentItem = mData.get(0);
			mCurrentItem.put("SchedulTime",System.currentTimeMillis());
			mData.remove(0);
			Log.d(TAG, "schedulTask");
			executeTask(mCurrentItem);
		}
	}
	
	Runnable mReloadRunnable = new Runnable() {		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(PushManager.getBindUser(mContext).equals("")){
				PushManager.startBindWork(mContext, PushConstants.LOGIN_TYPE_API_KEY, PushMessageReceiver.API_KEY, UserManager.getInstance().getUserName());PushManager.startBindWork(mContext, PushConstants.LOGIN_TYPE_API_KEY, PushMessageReceiver.API_KEY, UserManager.getInstance().getUserName());
			}
			
			Log.d(TAG, "后台上传文件 定时刷新 mRefreshTime:" + G.getPhoneCurrentTime()+" queueSize= " +mData.size()+" currentItem = " +mCurrentItem);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("后台上传文件 定时刷新 mRefreshTime:", G.getPhoneCurrentTime());
				jsonObject.put("queueSize", mData.size());
				jsonObject.put("currentItem", mCurrentItem);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CatchException.saveException(mContext, jsonObject);
			//从新加载数据
			notifyDataChange();
			//定时刷新
			mHandler.postDelayed(mReloadRunnable,mRefreshTime);
		}
	};
	
	void executeTask(ContentValues data) {
		// TODO Auto-generated method stub
		Log.d(TAG, "execute data:" +data.toString());
		
		TaskStackMode taskStackMode = new TaskStackMode((ContentValues) data);
		if(taskStackMode._Action.equals(DBModle.TaskStack.Action_Http)) {
			PostRequest(taskStackMode);
		}else if(taskStackMode._Action.equals(DBModle.TaskStack.Action_Upload)) {
			UpLoadFile(taskStackMode);
		}
	}

	private void PostRequest(TaskStackMode tsm) {			
		final String url = tsm._RequestURL;	
		final String StackID = tsm._StackID;
		String[] keys = tsm._Keys.split("\\|");
		String[] values = tsm._Vals.split("\\|");
		
		RequestParams params = new RequestParams();
		for(int i = 0; i < keys.length;i++){
			params.put(keys[i], values[i]);
		}			
		if(url.equals(HttpParams.UPDATETASK)){
			try {
				JSONObject json = new JSONObject(tsm._Data);
				Iterator<?> it = json.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					String value = json.getString(key);
					params.put(key, value);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "PostRequest json error:" + e.getMessage());
				return;
			}
		}
		post(url,StackID, params , "");
	}
	
	private void UpLoadFile(TaskStackMode tsm) {
		final String url = tsm._RequestURL;	
		final String StackID = tsm._StackID;
		String[] keys = tsm._Keys. split("\\|");
		String[] values = tsm._Vals.split("\\|");
		String filePath = "";
		RequestParams params = new RequestParams();
		for(int i = 0; i < keys.length;i++){
			if(DBModle.TaskStack.Keys_File.equals(keys[i])){
				filePath = values[i];
				try {
					params.put(keys[i], new File(filePath));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(TAG, "UpLoadFile dFileNotFoundException filePath:" + filePath);
					mCurrentItem=null;
					//文件没有找到，把这条记录改为IsRequest_F，并进行下一个
					ContentValues values2 = new ContentValues();
					values2.put(DBModle.TaskStack.IsRequest , DBModle.TaskStack.IsRequest_F);
					boolean rt = DBOperator.updateTaskStack(StackID, values2);
					if(rt){
						JSONObject jsonObject = new JSONObject();
						try {
							jsonObject.put("UploadWork  updateTaskStack()", "文件没有找到，把这条记录改为IsRequest_F，并进行下一个 ");
							jsonObject.put("url", url);
							jsonObject.put("params", params);
							jsonObject.put("filePath", filePath);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						CatchException.saveException(mContext, jsonObject);
						Log.d(TAG, "updateTask "+  DBModle.TaskStack.IsRequest_F +"    StackID:" + StackID);
					}
					
					new Handler().post(IsRequest_FRunnable);
					
					return;
				}
			}else{
				params.put(keys[i], values[i]);
			}
		}	
		
		String path = values[0];
		MD5 md5 = new MD5();
		String fileMa5 = "";
		try {
			fileMa5 = md5.getFileMD5String(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "文件找不到 :"+e);
			
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("UploadWork", "UpLoadFile()");
				jsonObject.put("url", url);
				jsonObject.put("params", params);
				jsonObject.put("filePath", filePath);
				jsonObject.put("MD5", "MD5  getFileMD5String(new File(path))  error!!!");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			CatchException.saveException(mContext, jsonObject);
			
			return;
		}
		params.put("MD5", fileMa5);
		post(url,StackID, params , filePath);		
	}
	
	Runnable IsRequest_FRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			schedulTask();
		}
	};
	
	private void post(final String url,final String StackID,final RequestParams params , final String filePath){
		mAsyncHttpClient.post(url, params,new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String content) {
				// TODO Auto-generated method stub
				mCurrentItem=null;
				super.onSuccess(content);
				
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("UploadWork", "post onSuccess()");
					jsonObject.put("content", content);
					jsonObject.put("url", url);
					jsonObject.put("params", params);
					jsonObject.put("filePath", filePath);
					if(CrashHandler.getInstance().getActiveNetwork(mContext) == null){
						jsonObject.put("ActiveNetwork", "null");
					}else{
						jsonObject.put("ActiveNetwork", CrashHandler.getInstance().getActiveNetwork(mContext).toString()+"");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				CatchException.saveException(mContext, jsonObject);
				
				
				try {
					JSONObject result = new JSONObject(content);					
					if(result.getString("code").equals("0")){	
						Log.d(TAG, "PostRequest succ url:" + url + " content:" + content);
						//修改为当上传遍成功不再风险记录
						ContentValues values = new ContentValues();
						values.put(DBModle.TaskStack.IsRequest , DBModle.TaskStack.IsRequest_S);
						boolean rt = DBOperator.updateTaskStack(StackID, values);
						if(rt){
							Log.d(TAG, "updateTaskStack "+ DBModle.TaskStack.IsRequest_S +" StackID:" + StackID);
							Intent intent = new Intent(Main.PROGESSBARRECEIVE);
							mContext.sendBroadcast(intent);
						}						
					}else{
						Log.e(TAG, "PostRequest fail url:" + url + " content:" + content);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				schedulTask();
				
				UserManager.getInstance().IsConnection(true);
				Intent intent = new Intent(Main.NOTCONNECTTION);
				intent.putExtra("SuccessOrFailure", "Success");
				mContext.sendBroadcast(intent);
			}

			@Override
			public void onFailure(Throwable error, String content) {
				// TODO Auto-generated method stub
				super.onFailure(error, content);
				mCurrentItem=null;
				Log.e(TAG, "PostRequest fail url:" + url);
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("UploadWork", "post onFailure()");
					jsonObject.put("error", error);
					jsonObject.put("url", url);
					jsonObject.put("params", params);
					jsonObject.put("filePath", filePath);
					if(CrashHandler.getInstance().getActiveNetwork(mContext) == null){
						jsonObject.put("ActiveNetwork", "null");
					}else{
						jsonObject.put("ActiveNetwork", CrashHandler.getInstance().getActiveNetwork(mContext).toString()+"");
					}
					
					jsonObject.put("content", content);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				CatchException.saveException(mContext, jsonObject);
				schedulTask();
				
				if(getActiveNetwork(mContext) !=null ){
					mDataHandler.Connection(UserManager.getInstance().getAccessToken(), new HttpResponseHandler(){

						@Override
						public void response(boolean success, String response, Throwable error) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(Main.NOTCONNECTTION);
							if(success){
								UserManager.getInstance().IsConnection(true);
								intent.putExtra("SuccessOrFailure", "Success");
							}else{
								UserManager.getInstance().IsConnection(false);
								intent.putExtra("SuccessOrFailure", "Failure");
							}
							mContext.sendBroadcast(intent);
						}
					});
				}
			}
		});
	}

	private int deleteTaskStack(String StackID){
		String selection = DBModle.TaskStack.StackID + " = ? ";
		return SQLiteManager.getInstance().Delete(DBModle.TASKSTACK, selection, new String[]{StackID});
	}
	
	@SuppressWarnings("unused")
	private class TaskStackMode {

		String _StackID;
		String _TID;
		String _Action;		
		String _StackType;
		String _RequestURL;
		String _Keys;
		String _Vals;
		String _Level;
		String _IsRequest;
		String _Description;
		String _Data;

		public TaskStackMode(ContentValues cv) {
			if (cv != null) {
				_StackID = cv.getAsString(DBModle.TaskStack.StackID);
				_TID = cv.getAsString(DBModle.TaskStack.TID);
				_Action = cv.getAsString(DBModle.TaskStack.Action);
				_StackType = cv.getAsString(DBModle.TaskStack.StackType);
				_RequestURL = cv.getAsString(DBModle.TaskStack.RequestURL);
				_Keys = cv.getAsString(DBModle.TaskStack.Keys);
				_Vals = cv.getAsString(DBModle.TaskStack.Vals);
				_Level = cv.getAsString("Level");
				_IsRequest = cv.getAsString(DBModle.TaskStack.IsRequest);
				_Description = cv.getAsString(DBModle.TaskStack.Description);
				_Data = cv.getAsString(DBModle.TaskStack.Data);
			}
		}
	}
}
