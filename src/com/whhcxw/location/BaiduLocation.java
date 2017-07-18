package com.whhcxw.location;

import java.text.DecimalFormat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BaiduLocation {

	private final String TAG = this.getClass().getSimpleName();
	Context mContext;
	LocationClient mLocationClient;
	MyLocationListenner mLocationListenner = new MyLocationListenner();
	BDLocationListener mBdLocationListener = null;

	/** 位置信息 定位是否成功 */
	public static boolean LOCAITON_SUCCESS = false;

	/** 位置信息 经度 */
	public String LOCAITON_LONITUDE = "0.0";

	/** 位置信息 纬度 */
	public String LOCAITON_LATITUDE = "0.0";

	/** 位置信息 地址 */
	public String LOCAITON_ADDRESS = "";
	
	/** 位置信息 城市 */
	public String LOCAITON_CITY = "";
	
	/** 位置信息 城市 */
	public BDLocation CURRENT_BDLOCATION = null;

	private static BaiduLocation _instance;
	
	/** 尝试定位次数 */
	private int mRequestLocationCount = -1;
	
	/** 重复尝试次数 */
	private int mmRequestLocationLimitCount = 2;
	
	private Handler mHandler = new Handler();

	private BaiduLocation() {
	}

	public static BaiduLocation instance() {
		if (_instance == null) {
			_instance = new BaiduLocation();			
		}
		return _instance;
	}

	public void init(Context c, LocationClientOption o) {
		if (mLocationClient != null)
			return;
		mContext = c.getApplicationContext();
		mLocationClient = new LocationClient(mContext);
		if (o != null)
			mLocationClient.setLocOption(o);
		Log.d(TAG, "BaiduLocation init()");
	}

	public void start() {
		if (mLocationClient != null) {
			mLocationClient.registerLocationListener(mLocationListenner);
			mLocationClient.start();
			mLocationClient.requestLocation();
		}
		Log.d(TAG, "LocationClient start");
	}

	public void stop() {
		if (mLocationClient != null)
			mLocationClient.unRegisterLocationListener(mLocationListenner);
		mLocationClient.stop();
		Log.d(TAG, "LocationClient stop");
	}
	
	public void requestLocation(){
		mLocationClient.requestLocation();
		Log.d(TAG, "requestLocation");
	}

	// 设置相关参数
	public void setLocationOption(LocationClientOption o) {
		stop();
		mLocationClient.setLocOption(o);
		start();
	}
	
	/**
	 * 返回默认 LocationClientOption
	 * 默认采用坐标bd0911； 刷新间隔时间5分钟；启用定位缓存；定位优先网络模块
	 * @return LocationClientOption
	 * */
	public LocationClientOption getDefaultLocationClientOption(){
		LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true);
//		option.setAddrType("all");//返回的定位结果包含地址信息 默认为无位置信息
		option.disableCache(true);
		option.setCoorType("gcj02");//返回的定位结果是百度经纬度,默认值gcj02  bd09ll
		option.setScanSpan(1000 * 60 * 5);//设置发起定位请求的间隔时间为5分钟
		option.disableCache(true);//禁止启用缓存定位
//		option.setPoiNumber(5);	//最多返回POI个数	
//		option.setPoiDistance(1000); //poi查询距离		
//		option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
		option.setPriority(LocationClientOption.NetWorkFirst);//设置网络优先
		return option;
	}

	public void setBDLocationListener(BDLocationListener listener){
		mBdLocationListener = listener;
	}
	
	public void removeBDLocationListener(){
		mBdLocationListener = null;
	}
	
	/**
	 * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
	 */
	private class MyLocationListenner implements BDLocationListener {
		public void onReceiveLocation(BDLocation location) {			
			if(location != null){
				CURRENT_BDLOCATION = location;				
				LOCAITON_CITY = location.getCity();			
				LOCAITON_LATITUDE = new DecimalFormat("0.0000").format(location.getLatitude()) ;
				LOCAITON_LONITUDE = new DecimalFormat("0.0000").format(location.getLongitude());
				mRequestLocationCount = -1;
				//发起context 监听
				requestHolderLocation(location);
				LOCAITON_SUCCESS = true;	
				Log.d(TAG, "location succ latitude:" + LOCAITON_LATITUDE + " lontitude:" + LOCAITON_LONITUDE);
			}else{
				Log.e(TAG, "location fail reRequest count:" + mRequestLocationCount);
				mRequestLocationCount++;
				//尝试定位3次				
				if(mRequestLocationCount < mmRequestLocationLimitCount){
					//延时一秒再次发起定位
					mHandler.postDelayed(new Runnable() {						
						@Override
						public void run() {
							requestLocation();
						} 
					}, 1000);
				}
				if(mRequestLocationCount == mmRequestLocationLimitCount){
					mRequestLocationCount = -1;
					//发起context 监听
					requestHolderLocation(location);					
				}
			}
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/** 发起activity 注册的监听 */
	private void requestHolderLocation(BDLocation location){
		if(mBdLocationListener != null){
			mBdLocationListener.onReceiveLocation(location);	
			//请求完毕后自动注销监听
			removeBDLocationListener();
		}
	}

}
