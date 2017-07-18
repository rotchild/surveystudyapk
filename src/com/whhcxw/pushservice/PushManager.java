package com.whhcxw.pushservice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBModle.TaskLog;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.data.DataHandler;
import com.whhcxw.MobileCheck.net.HttpResponseHandler;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.global.G;

public class PushManager extends com.baidu.android.pushservice.PushManager{

	private static final String TAG = PushManager.class.getSimpleName();
	
	public static final String PREFERENCES_PUSH = "preferences_push";
	
	/** 重复绑定次数上限 3次 */
	private static final int mRepeatBindLimitCount = 3;
	/** 已重复绑定几次 */
	private static int mRepeatBindCount = 0;
	
	/**
	 * 绑定用户
	 * @param ctx 上下文对象
	 * @param UserName 当前登录对象
	 * @param AccessToken 访问信令
	 * @param content 绑定返回结果
	 * */
	public static void BindUsers(final Context ctx,final String UserName,String AccessToken,final String content){
		String PushID = "";
		JSONObject contentjson = null;
		try {
			contentjson = new JSONObject(content);			
			PushID = contentjson.getJSONObject("response_params").getString("user_id");
			PushID += "_android";
			contentjson.getJSONObject("response_params").put("user_id", PushID);			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e(TAG, "parse json:" + content );
			return ;
		}
		final String contentJsonStr = contentjson.toString();
		final String user_id = PushID;
		DataHandler dataHandler = new DataHandler(ctx);
		dataHandler.BindPushUser(UserName, PushID, AccessToken, new HttpResponseHandler(){
			@Override
			public void response(boolean success, String response,Throwable error) {
				// TODO Auto-generated method stub
				super.response(success, response, error);
				if(success){
					try {
						JSONObject resultJson = new JSONObject(response);
						String code = resultJson.getString("code");
						if(code.equals("0")){
							savePushPreferences(ctx,contentJsonStr,UserName);
							Log.d(TAG, "bind succ UserName: "+UserName + " pushid:" + user_id);
						}else{
							//startWork(arg0, arg1, arg2)
							Log.d(TAG, "bind fail error re bind");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block						
						e.printStackTrace();
						Log.e(TAG, "Parse bind json infos error: " + e);
					}
				}else{
					Log.e("TAG", "bind fail error :" + error.getMessage());
				}
			}			
		});
	}

	/**
	 * 启动绑定服务
	 * @param ctx
	 * @param loginTypeApiKey
	 * @param ApiKey
	 * */
	public static void startBindWork(Context ctx,int loginTypeApiKey,String ApiKey,String UserName){
		if(!getBindUser(ctx).equals(UserName)){
			//如果获取已绑定的UserName 不为当前登录用户  说明用户切换设备 ，发起请求像百度绑定
			startWork(ctx,loginTypeApiKey, ApiKey);
			Log.d(TAG, " start bind username:" + UserName);
		}else{
			Log.d(TAG, " bind once username:" + UserName);
			PushConstants.restartPushService(ctx);
		}
		mRepeatBindCount++;
	}
	
	/**
	 * 重新调用3次
	 * */
	public static void reBind(Context ctx,int loginType,String ApiKey,String UserName){
		if(mRepeatBindCount == mRepeatBindLimitCount){
			Log.e(TAG, "rebind 3 count fail stop bind");
			return;
		}
		Log.e(TAG, "rebind  count:" + mRepeatBindCount);
		startBindWork(ctx,loginType,ApiKey,UserName);
	}
	
	
	/**
	 * 保存百度绑定信息
	 * */
	public static boolean savePushPreferences(Context ctx,String content,String UserName){
		SharedPreferences preferences = ctx.getSharedPreferences(PREFERENCES_PUSH, Context.MODE_PRIVATE);
		//content = {"response_params":{"appid":"604811","channel_id":"3980400113410407003","user_id":"1122655928626736813"},"request_id":2650910872}
		String bind_content = content;
		String request_id = "";
		String appid = "";
		String channel_id = "";
		String user_id = "";
		try {
			JSONObject json = new JSONObject(content);
			request_id = json.getString("request_id");
			JSONObject response_params = json.getJSONObject("response_params");
			appid = response_params.getString("appid");
			channel_id = response_params.getString("channel_id");
			user_id = response_params.getString("user_id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Editor edit = preferences.edit();
		edit.putString("bind_content", bind_content);
		edit.putString("request_id", request_id);
		edit.putString("appid", appid);
		edit.putString("channel_id", channel_id);
		edit.putString("user_id", user_id);
		edit.putString("UserName", UserName);
		return edit.commit();
	}
	
	
	/**
	 * 清空推送绑信息
	 * @param Context
	 */
	public static boolean clearPushPreferences(Context ctx){
		SharedPreferences preferences = ctx.getSharedPreferences(PREFERENCES_PUSH, Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putString("bind_content", "");
		edit.putString("request_id", "");
		edit.putString("appid", "");
		edit.putString("channel_id", "");
		edit.putString("user_id", "");
		edit.putString("UserName", "");
		return edit.commit();
	}
	
	
	/** 
	 * 获取百度绑定的UserName
	 * @return user_id 返回"" 就是没有
	 *  */
	public static String getBindUser(Context ctx){
		return ctx.getSharedPreferences(PREFERENCES_PUSH, Context.MODE_PRIVATE).getString("UserName", "");
	}

	/** 
	 * 获取百度绑定的userid
	 * @return user_id 返回"" 就是没有
	 *  */
	public static String getBindID(Context ctx){
		return ctx.getSharedPreferences(PREFERENCES_PUSH, Context.MODE_PRIVATE).getString("user_id", "");
	}
	
	
	/**
	 * 保存推送消息    JSONArray中  0：caseno  1：tsktype  2：alert返回的案件信息  3:platform 平台   4:boolean 是否已读 false否 true 是
	 * @param cxt
	 * @param message
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean addPushTask(Context cxt,String message){
		boolean b = false;
		try {
			JSONObject jsonObject = new JSONObject(message);
			String alert =  jsonObject.getJSONObject("aps").getString("alert");
			String  platform = jsonObject.getJSONObject("aps").getString("platform");
			JSONObject data = jsonObject.getJSONObject("aps").getJSONObject("data");
			String caseno = data.getString("CaseNo");
			String tasktype = data.getString("tasktype");
			SharedPreferences preferences = cxt.getSharedPreferences(PushManager.PREFERENCES_PUSH, Context.MODE_PRIVATE);
			JSONArray notifyListArray = new JSONArray(preferences.getString("notifyList", "[]"));
			
			if(notifyListArray.length()==0){
				JSONObject object = new JSONObject();
				object.put("caseno", caseno);
				object.put("tasktype", tasktype);
				object.put("alert", alert);
				object.put("platform", platform);
				object.put("isread",false);
				notifyListArray.put(object);
			}
			
			boolean isExist = false;
			JSONObject msgJsonObject = null;
			for (int i = 0; i < notifyListArray.length(); i++) {
				JSONObject listJsonObject = notifyListArray.getJSONObject(i);
				String casenolist = listJsonObject.get("caseno").toString();
				String tsktypelist = listJsonObject.get("tasktype").toString();
				
				if(caseno.equals(casenolist) && tasktype.equals(tsktypelist)){
					msgJsonObject = listJsonObject;
				}
			}
			
			if(msgJsonObject == null){
				msgJsonObject = new JSONObject();
				notifyListArray.put(msgJsonObject);
			}
			
			msgJsonObject.put("caseno", caseno);
			msgJsonObject.put("tasktype", tasktype);
			msgJsonObject.put("alert", alert);
			msgJsonObject.put("platform", platform);
			msgJsonObject.put("isread",false);
						
			Editor editor = preferences.edit();
			editor.putString("notifyList", notifyListArray.toString());
			b = editor.commit();
			
			boolean createTaskSuc = createTask(compileJsonData(cxt,data), cxt);
			if (createTaskSuc) {
				
				Log.d(TAG, "create task from push service success");
			}else {
				Log.e(TAG, "create task from push service failer");
			}
		
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "PushManager addPushTask json fail");
		}
		return b;
	}
	
	
	/**
	 * 解析json中的data TASK 数据 
	 * @param data
	 */
	private static ContentValues compileJsonData(Context ctx,JSONObject data){
		ContentValues cv = new ContentValues();
		try {
			cv.put(DBModle.Task.CaseNo, filtData(data.getString("CaseNo")));
			cv.put(DBModle.Task.TaskType, ctx.getResources().getString(R.string.tasktype_survey));
			cv.put(DBModle.Task.CarMark, filtData(data.getString("CarMark")));
			String carowner =  filtData(data.getString("CarOwner"));
			if(carowner.length() >50){
				carowner = carowner.substring(0, 49);
			}
			cv.put(DBModle.Task.CarOwner,carowner);
			cv.put(DBModle.Task.Telephone, filtData(data.getString("Telephone")));
			cv.put(DBModle.Task.Address, filtData(data.getString("Address")));
			cv.put(DBModle.Task.AccidentTime, getCurrentTime(filtData(data.getString("AccidentTime"))));
//			cv.put(DBModle.Task.ImgPath, filtData(data.getString("ImgPath")));
			cv.put(DBModle.Task.ImgPath,"");
			cv.put(DBModle.Task.FixedPrice, filtData(data.getString("FixedPrice")));
			//经纬度
//			String latitude= BaiduLocation.instance().LOCAITON_LATITUDE;
//			String longitude = BaiduLocation.instance().LOCAITON_LONITUDE;
//			cv.put(DBModle.Task.Latitude, latitude);
//			cv.put(DBModle.Task.Longitude, longitude);
			cv.put(DBModle.Task.Latitude, "0");
			cv.put(DBModle.Task.Longitude, "0");
			cv.put(DBModle.Task.StateUpdateTime, getCurrentTime());
			cv.put(DBModle.Task.Memo, filtData(data.getString("Memo")));
			cv.put(DBModle.Task.CaseType, filtData(data.getString("CaseType")));
			cv.put(DBModle.Task.AutoGenerationNO, filtData(data.getString("AutoGenerationNo")));
			cv.put(DBModle.Task.CarType, filtData(data.getString("CarType")));
			cv.put(DBModle.Task.AccidentType, filtData(data.getString("AccidentType")));
			cv.put(DBModle.Task.InsuranceType, filtData(data.getString("InsuranceType")));
			cv.put(DBModle.Task.InsuranceID, filtData(data.getString("InsuranceID")));
			cv.put(DBModle.Task.InsuranceContactPeople, filtData(data.getString("InsuranceContactPeople")));
			cv.put(DBModle.Task.InsuranceContactTelephone, filtData(data.getString("InsuranceContactTelephone")));
			cv.put(DBModle.Task.PaymentAmount, filtData(data.getString("PaymentAmount")));
			cv.put(DBModle.Task.PaymentMethod, filtData(data.getString("PaymentMethod")));
			cv.put(DBModle.Task.IsRisk, filtData(data.getString("IsRisk"),"0"));
			cv.put(DBModle.Task.AccidentDescribe, filtData(data.getString("AccidentDescribe")));
			cv.put(DBModle.Task.FrontOperator, UserManager.getInstance().getUserName());
			cv.put(DBModle.Task.FrontState, TaskLog.FrontState_NO_ASSIGN);//统一定为默认未分配状态
			cv.put(DBModle.Task.BackOperator, "");
			cv.put(DBModle.Task.BackState,  DBModle.TaskLog.FrontState_NO_ASSIGN);
			cv.put(DBModle.Task.IsNew, 1);
			cv.put(DBModle.Task.CarDriver, filtData(data.getString("CarDriver")));
			cv.put(DBModle.Task.BackPrice , "0");
			cv.put(DBModle.Task.FrontPrice , "0");
			cv.put(DBModle.Task.Watcher , "");
			cv.put(DBModle.Task.LinkCaseNo ,  filtData(data.getString("CaseNo")));
			
			cv.put(DBModle.Task.CarTypeID, "-1");
			cv.put(DBModle.Task.GarageID, "-1");
			cv.put(DBModle.Task.TaskID, "-1");
			cv.put(DBModle.Task.IsCooperation, DBModle.TaskLog.COOPERATION);
			
			cv.put(DBModle.Task.ThirdCar, filtData(data.getString("ThirdCar")));
			cv.put(DBModle.Task.AreaID, filtData(data.getString("AreaID")));
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "compile the json data error");
		}
		
		return cv;
	}
	
	/**
	 * 过滤数据
	 * @param data
	 * @return
	 */
	private static String filtData(String data,String defaultText){
		if (data!=null) {
			return data;
		}else {
			return defaultText;
		}
	}
	/**
	 * 过滤数据
	 * @param data
	 * @return
	 */
	private static String filtData(String data){
		if (data!=null) {
			return data;
		}else {
			return "";
		}
	}
	
	/**
	 * 创建调度任务
	 * @param cv
	 * @param cxt
	 */
	private static boolean createTask(ContentValues cv,Context cxt){
		boolean bSuc = false;
		if (isCompleted(cv)) {
			if(bSuc = DBOperator.createTask(cv)){
			}else {
				bSuc = false;
				Toast.makeText(cxt, cxt.getResources().getString(R.string.createcase_fail), Toast.LENGTH_LONG).show();
				ContentValues values = DBOperator.getTask(cv.getAsString(DBModle.Task.CaseNo), cv.getAsString(DBModle.Task.TaskType));
				String TID = values.getAsString(DBModle.Task.TID);
				String currentState = values.getAsString(DBModle.Task.FrontState);
//				if(DBOperator.selectTaskLog(TID, currentState).size() != 0){
					ContentValues valuesTaskLog = new ContentValues();
					valuesTaskLog.put(DBModle.TaskLog.StateUpdateTime, G.getPhoneCurrentTime());
					if( DBOperator.updateTaskLog(TID, valuesTaskLog , currentState)){
						if(DBOperator.uploadCaseService(TID, values.getAsString(DBModle.Task.FrontState))){
							UploadWork.sendDataChangeBroadcast(cxt);	
						}
					}
//				}
			}
		}
		return bSuc;
	}
	/**
	 * 判断信息是否完整
	 * @param cv
	 * @return
	 */
	private static boolean isCompleted(ContentValues cv){
		boolean isCompleted = true;
		if (cv.get(DBModle.Task.CaseNo)==null||"".equals(cv.getAsString(DBModle.Task.CaseNo).trim())) {
			isCompleted = false;
		}
		if (cv.get(DBModle.Task.CarMark)==null||"".equals(cv.getAsString(DBModle.Task.CarMark).trim())) {
			isCompleted = false;
		}
//		if (cv.get(DBModle.Task.InsuranceID)==null||"".equals(cv.getAsString(DBModle.Task.InsuranceID).trim())) {
//			isCompleted = false;
//		}
		return isCompleted;
	}
	
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
	 * 处理平台发送过来时间格式不同
	 * @param time
	 * @return
	 */
	public static String getCurrentTime(String time){
		String month = time.substring(5,10);
		String hour = time.substring(11,16);
		String accidentTime = month +"\u0020\u0020"+hour;
		return accidentTime;
	}
	
	
}
