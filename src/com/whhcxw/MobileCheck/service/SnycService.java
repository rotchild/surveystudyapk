package com.whhcxw.MobileCheck.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.R;

/**
 * 同步服务
 * */
public class SnycService extends Thread{

	private final String TAG = this.getClass().getSimpleName();
	private DataHandler mDataHandler;
	private Context mContext;
	private String AccessToken;
	public Handler mHandler;
	private boolean isSnyc = false;
	/** 4小时刷新一次 */
	private int mRefreshTime = 1000 * 60*60*8;
	
	/**同步数据进度条显示*/
	private final String SHOW="show";
	/**同步数据进度条消失*/
	private final String DISAPPEAR="disappear";

	public SnycService(Context ctx, String _accessToken) {
		mContext = ctx;
		AccessToken = _accessToken;
		mDataHandler = new DataHandler(mContext);
		// mRefreshTime = 1000 * 5;
	}

//	public void start() {
////		isSnyc = true;
//		mHandler.post(mRefreshRunnable);
//	}

//	public void stop() {
//		mHandler.getLooper().quit();
//		Log.d(TAG, "sync service stop");
//		isSnyc = false;
//		mHandler.removeCallbacks(mRefreshRunnable);
//	}

	private Runnable mRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG, "sync mRefreshRunnable  mRefreshTime:" + mRefreshTime);
			if (isSnyc) {
				snyc();
				mHandler.postDelayed(mRefreshRunnable, mRefreshTime);
			}
		}
	};


	public void run() {
		Looper.prepare();
		mHandler = new Handler();
		isSnyc = true;
		mHandler.post(mRefreshRunnable);
		Looper.loop();
		isSnyc = false;
		mHandler.removeCallbacks(mRefreshRunnable);
	}
	ArrayList<String> result;
	// 开始同步
	private void snyc() {
		mDataHandler.setIsShowProgressDialog(false);
		mDataHandler.setIsShowError(false);
		result = new ArrayList<String>();
		try {
			showOrDisappearProgress(SHOW);
			// 同步车牌信息
			mDataHandler.GetCarTypeList(AccessToken, new HttpResponseHandler() {
				@Override
				public void response(boolean success, String response,
						Throwable error) {
					try {
						if (!success) {
							Log.e(TAG, "sync CarType get data error ");
						} else{
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray car_type_list = data.getJSONObject("value").getJSONArray("car_type_list");
								if(car_type_list.length() != 0){
									JSONObject json = car_type_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.CARTYPE);	
									if(localParams == null){
										if( DBOperator.syncCarType(car_type_list) != 0){
											DBOperator.addParams("", DBModle.CARTYPE, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if( DBOperator.syncCarType(car_type_list) != 0){
												DBOperator.updateParams(DBModle.CARTYPE, serveUpdateTime);
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					result.add("GetCarTypeList");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
					
				}
			});

			// 同步修理厂信息
			mDataHandler.GetGarageList(AccessToken, new HttpResponseHandler() {
				public void response(boolean success, String response,
						Throwable error) {
					if (!success) {
						Log.e(TAG, "sync Repair get data error ");
					}else{
						try {
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray repair_list = data.getJSONObject("value").getJSONArray("repair_list");
								if(repair_list.length() != 0){
									JSONObject json = repair_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.GARAGE);	
									if(localParams == null){
										if(DBOperator.syncGarage(repair_list) != 0){
											DBOperator.addParams("", DBModle.GARAGE, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if( DBOperator.syncGarage(repair_list) != 0){
												DBOperator.updateParams(DBModle.GARAGE, serveUpdateTime);
											}
										}
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					result.add("GetGarageList");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
				}
			});
		
			// 同步用户信息
			mDataHandler.GetUserList(AccessToken, new HttpResponseHandler() {
				@Override
				public void response(boolean success, String response,
						Throwable error) {
					if (!success) {
						Log.e(TAG, "sync User get data error ");
					}else{
						try {
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray user_list = data.getJSONObject("value").getJSONArray("user_list");
								if(user_list.length() != 0){
									JSONObject json = user_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.USERS);	
									if(localParams == null){
										if( DBOperator.syncUsers(user_list, "") != 0){
											DBOperator.addParams("", DBModle.USERS, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if( DBOperator.syncUsers(user_list, "") != 0){
												DBOperator.updateParams(DBModle.USERS, serveUpdateTime);
											}
										}
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					result.add("GetUserList");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
				}
			});
			
			// 同步区域信息
			mDataHandler.GetAreaList(AccessToken, new HttpResponseHandler() {
				@Override
				public void response(boolean success, String response,
						Throwable error) {
					try {
						if (!success) {
							Log.e(TAG, "sync Area get data error ");
						}else{
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray area_list = data.getJSONObject("value").getJSONArray("area_list");
								if(area_list.length() != 0){
									JSONObject json = area_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.AREAS);	
									if(localParams == null){
										if( DBOperator.syncArea(area_list) != 0){
											DBOperator.addParams("", DBModle.AREAS, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if( DBOperator.syncArea(area_list) != 0){
												DBOperator.updateParams(DBModle.AREAS, serveUpdateTime);
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					result.add("GetAreaList");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
				}
			});
			
			// 同步保险公司信息
			mDataHandler.GetInsuranceList(AccessToken, new HttpResponseHandler() {
				@Override
				public void response(boolean success, String response,
						Throwable error) {
					try {
						if (!success) {
							Log.e(TAG, "sync Insurance get data error ");
						}else{
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray insurance_list = data.getJSONObject("value").getJSONArray("insurance_list");
								if(insurance_list.length() != 0){
									JSONObject json = insurance_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.INSURANCE);	
									if(localParams == null){
										if( DBOperator.syncInsurance(insurance_list) != 0){
											DBOperator.addParams("", DBModle.INSURANCE, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if(DBOperator.syncInsurance(insurance_list) != 0){
												DBOperator.updateParams(DBModle.INSURANCE, serveUpdateTime);
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					result.add("GetInsuranceList");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
					
				}
			});
			
			// 同步Dict表
			mDataHandler.GetDicts(AccessToken, new HttpResponseHandler() {
				@Override
				public void response(boolean success, String response,
						Throwable error) {
					try {
						if (!success) {
							Log.e(TAG, "sync Dict get data error ");
						}else{
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray dict_list = data.getJSONObject("value").getJSONArray("dict_list");
								if(dict_list.length() != 0){
									JSONObject json = dict_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.DICT);	
									if(localParams == null){
										if(DBOperator.syncDict(dict_list) != 0){
											DBOperator.addParams("", DBModle.DICT, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if(DBOperator.syncDict(dict_list) != 0){
												DBOperator.updateParams(DBModle.DICT, serveUpdateTime);
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					result.add("GetDicts");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
					
				}
			});
			
			// 同步RoleDicts表
			mDataHandler.GetRoleDicts(AccessToken, new HttpResponseHandler() {
				@Override
				public void response(boolean success, String response,
						Throwable error) {
					try {
						if (!success) {
							Log.e(TAG, "sync RoleDicts get data error ");
						}else{
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {
								JSONArray role_dict_list = data.getJSONObject("value").getJSONArray("role_dict_list");
								if(role_dict_list.length() != 0){
									JSONObject json = role_dict_list.getJSONObject(0);				
									String serveUpdateTime = json.getString(DBModle.Params.UpdateTime);
									ContentValues localParams = DBOperator.getParams(DBModle.ROLEDICTS);	
									if(localParams == null){
										if(DBOperator.syncRoleDicts(role_dict_list) != 0){
											DBOperator.addParams("", DBModle.ROLEDICTS, "", "", mContext.getString(R.string.params_update), serveUpdateTime);
										}
									}else{
										Date serveDate = setSimpleDateFormat(serveUpdateTime);
										Date localDate = setSimpleDateFormat(localParams.getAsString(DBModle.Params.UpdateTime));
										if(serveDate.getTime()>localDate.getTime()){
											if(DBOperator.syncRoleDicts(role_dict_list) != 0){
												DBOperator.updateParams(DBModle.ROLEDICTS, serveUpdateTime);
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					result.add("GetRoleDicts");
					if(result.size() == 7){
						showOrDisappearProgress(DISAPPEAR);
					}
				}
			});
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "sync err" + e.getMessage());
		}
	}


	/**
	 * 同步查勘任务状态
	 * */
	public void snycSurveyTask(ArrayList<ContentValues> queryTaskList,String username,final Context context) {
		// 同步区域信息
		mDataHandler.QueryTasks(queryTaskList, AccessToken,username,new HttpResponseHandler() {
					@Override
					public void response(boolean success, String response,
							Throwable error) {
						try {
							if (!success) {
								Log.e(TAG,"sync snycSurveyTask get data error ");
								return;
							}
							JSONObject data = new JSONObject(response);
							String code = data.getString("code");
							if (code.equals("0")) {

								int modifyCount = 0;
								JSONArray task_list = data.getJSONObject("value").getJSONArray("task_list");
								for (int i = 0; i < task_list.length(); i++) {
									JSONObject task = task_list.getJSONObject(i);
									String backState = task.getString(DBModle.Task.BackState);
									String caseno = task.getString(DBModle.Task.CaseNo);
									String tasktype = task.getString(DBModle.Task.TaskType);
									String memo = task.getString(DBModle.Task.Memo);

									ContentValues values = new ContentValues();
									values.put(DBModle.Task.Memo, memo);
									values.put(DBModle.Task.BackState,backState);
									values.put(DBModle.Task.IsNew, "1");

									if (backState.equals(DBModle.TaskLog.FrontState_FINISH)) {
										// 修改案件
										if (DBOperator.updateTask(caseno,tasktype, values)) {
											// 修改成功++
											modifyCount++;
										}
									}
								}
								// 有案件状态更新，发送广播刷新列表
								if (modifyCount > 0) {
									Intent mIntent = new Intent(Main.NEWTASKTRENDS);
									mIntent.putExtra("yaner", context.getString(R.string.yuandong));
									context.sendBroadcast(mIntent);
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	}
	
	/**
	 * yyyy-MM-dd HH:mm:ss  String转成Date
	 * @param date
	 * @return
	 */
	private Date setSimpleDateFormat(String date){
		Date dateFormat;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			dateFormat = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dateFormat = null;
		}
		return dateFormat;
	}

	/**
	 * 同步数据进度条，显示  show  消失disappear
	 * @param showOrDisappear
	 */
	private void showOrDisappearProgress(String showOrDisappear){
		Intent showIntent = new Intent(Main.SNYCPROGRESS); 
		showIntent.putExtra("showOrDisappear", showOrDisappear); 
        mContext.sendBroadcast(showIntent);
	}
	
}
