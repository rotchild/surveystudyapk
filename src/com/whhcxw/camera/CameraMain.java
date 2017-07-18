package com.whhcxw.camera;

import java.io.IOException;

import com.baidu.mobstat.StatActivity;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.androidcamera.NetCameraService;
import com.whhcxw.androidcamera.NetEncoder;
import com.whhcxw.camera.config.CameraManager;
import com.whhcxw.camera.picture.PictureManager;
import com.whhcxw.camera.ui.RotateImageView;
import com.whhcxw.camera.ui.Switcher.OnSwitchListener;
import com.whhcxw.camera.ui.Switcher;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.whhcxw.camera.ui.FocusRectangle;
import com.whhcxw.crashreport.CrashHandler;

@SuppressLint("NewApi")
public class CameraMain extends StatActivity implements
SurfaceHolder.Callback ,OnSwitchListener,OnClickListener,OnLongClickListener
{
	private boolean 					hasSurface;
	private CameraManager 				cameraManager;
	private CaptureActivityHandler		handler;
//	private InactivityTimer 			inactivityTimer;
	private	String					TAG = "hcxw camera";
	private  SurfaceView 			surfaceView ;
	private final int				MESSAGE_RESTART_PREVIEW = 1;
	private final int				MESSAGE_TAKE_PIC = 3;
	private final int				MESSAGE_PREIVEW_PICTURE = 5;
	private final int				MESSAGE_START_RECORD= 6;
	private final int				MESSAGE_DO_FOCUS = 7;
	private final int				MESSAGE_STOP_RECORD= 8;
	private final int				MESSAGE_SWITCH_CAMERA= 9;
	private final int				CLEAR_SCREEN_DELAY= 10;
	private final int				MESSAGE_SHOW_THUM= 12;

	private ImageView				mShutterButton;
	private  FocusRectangle			mFocusRectangle;
	private RotateImageView			previewImage,flashButton,b2fButton,focusBtn;	
	private Switcher				mSwitcher;


	private  boolean				isCapture,isPause,isTakingPic;
	public static String			caseId;

	@SuppressWarnings("unused")
	private boolean					isVIP,isFirstLogin;
	private Bundle					mBundle,cameraConfigBundle;
	private PictureManager			pictureManager;


	///////////////////编码器对象//////////////////
	private NetEncoder				mNetEncoder;

	private PreferenceGroup 		group;
	private static final int 		SCREEN_DELAY = 30 * 1000;
	
	//对焦状态
	private static final int 		FOCUSING = 1;
	private static final int 		FOCUS_SUCCESS = 3;
	private static final int 		FOCUS_FAIL = 4;
	private boolean					bRefresh = false;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(getPackageName(), "oncreate");

		
		System.gc();
		MyOrientationEventListener.initInstance(this);
		initView();
		//获取相关默认配置信息
		getCameraConfig();
		

		hasSurface = false;
//		inactivityTimer = new InactivityTimer(this);
		
		//获取外部配置信息
		Intent i = getIntent();
		if (i != null) {
			CameraManager.serverIp = new String(i.getExtras().getString("IP"));
			CameraManager.sign = i.getExtras().getInt("SIGN",0);
			CameraManager.previewWidth = i.getExtras().getInt("PW", 352);
			CameraManager.previewHeight = i.getExtras().getInt("PH", 288);
			CameraManager.bitrate = i.getExtras().getInt("BIT", 300);
			CameraManager.fps = i.getExtras().getInt("FPS", 12);
		}
		
		//初始化网络服务模块
		initNetEncoder();
		

	}

	private void initNetEncoder() {
		Intent ServiceIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("sign",CameraManager.sign);
		bundle.putString("serverIP", CameraManager.serverIp);
		bundle.putInt("captureW", CameraManager.previewWidth);
		bundle.putInt("caputreH", CameraManager.previewHeight);
		bundle.putInt("fps", CameraManager.fps);
		bundle.putInt("bitrate", CameraManager.bitrate);
		
		ServiceIntent.putExtras(bundle);
		ServiceIntent.setClass(this, NetCameraService.class);
		startService(ServiceIntent);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(getPackageName(), "onResume");

		if(isPause){
			isPause = false;
			
		}
		
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			//如果activity是暂停状态而不是停止，因此surface 仍存在
			//因此 surfaceCreated() 不会被调用, 所以在这里初始化camera.
			initCamera(surfaceHolder);
		}else {
			//装载callback，等待surfaceCreated() 初始化camera
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		
		
//		inactivityTimer.onResume();
		MyOrientationEventListener.initInstance(this).enable();
		if (handler != null) {
			handler.resumeSynchronously();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(getPackageName(), "onStart");
	}

	@Override
	
	public void onPause() {
		Log.d(getPackageName(), "onPause");
		isPause = true;
		if (handler != null) {
			handler.quitSynchronously();
		}
//		inactivityTimer.onPause();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
			surfaceView.setOnClickListener(this);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		MyOrientationEventListener.initInstance(this).disable();
		cameraManager.closeDriver();
//		resetScreenOn();
		mFocusRectangle.clear();
		setButtonEnable(true);
		takingPicture(false);
		super.onPause();

	}

	@Override
	protected void onDestroy() {
//		inactivityTimer.shutdown();
		Log.d(getPackageName(), "onDestroy");
	
		super.onDestroy();
		if (!bRefresh) {
			stop();
		}else {
			bRefresh = false;
		}
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
//		keepScreenOnAwhile();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(getPackageName(), "onRestoreInstanceState");
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(getPackageName(), "onSaveInstanceState");
	}

	private void resetScreenOn() {
		if (handler!=null) {
			handler.removeMessages(CLEAR_SCREEN_DELAY);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	private void keepScreenOnAwhile() {
		if (handler!=null) {
			handler.removeMessages(CLEAR_SCREEN_DELAY);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			handler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (holder == null) {
			Log.e(TAG,"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		boolean preview = holder.isCreating();
		cameraManager.setViewFinder(width, height, preview,surfaceView);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;
	}

	public void setButtonEnable(boolean enabled){
		mShutterButton.setEnabled(enabled);
		previewImage.setEnabled(enabled);
		surfaceView.setEnabled(enabled);
		flashButton.setEnabled(enabled);
		b2fButton.setEnabled(enabled);
		focusBtn.setEnabled(enabled);
		mSwitcher.setEnabled(enabled);
//		Log.e("mShutterButton", "mShutterButton enable"+enable);
	}


	/**
	 * 获取相机配置信息
	 */
	private void getCameraConfig(){


		PreferenceInflater inflater = new PreferenceInflater(this);
		group =
				(PreferenceGroup) inflater.inflate(R.xml.camera_preferences);

		cameraManager = CameraManager.initCameraManagerInstance(this,group);
		pictureManager = PictureManager.initInstance(this);

		cameraManager.getCameraParameter(group);
	}

	private void initView(){
		// 初始化显示
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 关闭标题栏
		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
		setContentView(R.layout.camera_mian);
		
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 

		surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
		surfaceView.setOnClickListener(this);
		LayoutInflater inflater = getLayoutInflater();

		ViewGroup rootView = (ViewGroup) findViewById(R.id.camera);
		View controlVierw = inflater.inflate(R.layout.camera_control, rootView);
		//	            inflater.inflate(R.layout.camera_title_control, rootView);
		mSwitcher = ((Switcher)controlVierw.findViewById(R.id.camera_switch));
		mSwitcher.setOnSwitchListener(this);
		mSwitcher.addTouchView(findViewById(R.id.camera_switch_set));
		mSwitcher.setSwitch(true);


		// Initialize shutter button.
		mShutterButton = (RotateImageView) controlVierw.findViewById(R.id.shutter_button);
		mShutterButton.setOnClickListener(this);
		mShutterButton.setOnLongClickListener(this);
		mShutterButton.setVisibility(View.VISIBLE);

		mFocusRectangle = (FocusRectangle) controlVierw.findViewById(R.id.focus_rectangle);

		previewImage = (RotateImageView) controlVierw.findViewById(R.id.review_thumbnail);
		previewImage.setOnClickListener(this);

		View controlTileVierw = inflater.inflate(R.layout.camera_title_control, rootView);
		b2fButton = (RotateImageView) controlTileVierw.findViewById(R.id.camera_b2f_switch);
		flashButton = (RotateImageView) controlTileVierw.findViewById(R.id.camera_flash_switch);
		focusBtn = (RotateImageView) controlTileVierw.findViewById(R.id.camera_focus_switch);
		
		b2fButton.setOnClickListener(this);
		flashButton.setOnClickListener(this);
		focusBtn.setOnClickListener(this);

	}


	public void updatePreviewImage(Bitmap bitmap){
		previewImage.setImageBitmap(bitmap);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, cameraManager);
				cameraManager.setHandler(handler);
			}
		} catch (IOException e) {
			e.printStackTrace();
			displayFrameworkBugMessageAndExit();

		}catch (RuntimeException e) {
			e.printStackTrace();
			displayFrameworkBugMessageAndExit();
		}
	}




	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage("相机打开失败，请检查相机是否被其他程序占用！");
		builder.setPositiveButton("确定", new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}


	/**
	 * 处理退出的方法
	 */
	private void stop(){
		if (cameraManager!=null) {
			cameraManager.release();
			pictureManager.release();

		}
	}
	
	/**
	 * 判断是否在拍照
	 * @param isTakingPic
	 */
	public void takingPicture(boolean isTakingPic){
		this.isTakingPic = isTakingPic;
	}
	
	private boolean bTakingPic(){
		if(isTakingPic){
			Toast.makeText(this, "正在拍照，请稍后", Toast.LENGTH_SHORT).show();
		}
		return isTakingPic;
	}
	@Override
	public boolean onSwitchChanged(Switcher source, boolean onOff) {
		
		if (bTakingPic()) return false;
		CameraManager.getCameraManagerInstance().controlListener.switchCaptureMode(onOff);
		mSwitcher.setSwitch(onOff);
		
		return false;
	}
	
	
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.camera_b2f_switch:
			if (bTakingPic()) return;
			CameraManager.getCameraManagerInstance().controlListener.switchCamera();
			break;
		case R.id.camera_flash_switch:
			if (bTakingPic()) return;
			String flashMode = CameraManager.getCameraManagerInstance().controlListener.setFlashMode();
			if (flashMode.equals("torch")) {
				flashButton.setImageResource(R.drawable.camera_ic_torch);
			}
			if (flashMode.equals("off")) {
				flashButton.setImageResource(R.drawable.camera_ic_flash_off);
			}
			if (flashMode.equals("on")) {
				flashButton.setImageResource(R.drawable.camera_ic_flash_on);
			}
			if (flashMode.equals("auto")) {
				flashButton.setImageResource(R.drawable.camera_ic_flash_auto);
			}
			break;
		case R.id.review_thumbnail:
			if (bTakingPic()) return;
			pictureManager.showPictureCurrent(CameraMain.this,handler.getCurrentPicture());
			break;
		case R.id.shutter_button:
			setButtonEnable(false);
			takingPicture(true);
			CameraManager.getCameraManagerInstance().controlListener.takePhoto();
			break;
			
		case R.id.camera_preview:
			if (isTakingPic) return;
			setButtonEnable(false);
			updateFocusIndicator();
			CameraManager.getCameraManagerInstance().controlListener.dofocus();
			break;
			
		case R.id.camera_focus_switch:
			if (bTakingPic()) return;
			String focusMode = CameraManager.getCameraManagerInstance().controlListener.setFocusMode();
			if (focusMode.equals(Parameters.FOCUS_MODE_AUTO)) {
				focusBtn.setImageResource(R.drawable.camera_button_focus_auto);
				Toast.makeText(CameraMain.this,getResources().getString(R.string.camera_focus_micro_closed), Toast.LENGTH_LONG).show();
			}
			if (focusMode.equals(Parameters.FOCUS_MODE_MACRO)) {
				focusBtn.setImageResource(R.drawable.camera_button_focus_micro);
				Toast.makeText(CameraMain.this, getResources().getString(R.string.camera_focus_micro_open), Toast.LENGTH_LONG).show();
			}
				break;
		default:
			break;
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			setButtonEnable(false);
			handler.sendEmptyMessage(MESSAGE_TAKE_PIC);
			break;
			
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void refresh(){
		bRefresh = true;
		finish();
		if (cameraManager!=null) {
			cameraManager.release();

		}
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		
	}
	
	
	protected void updateFocusIndicator(){
		if (mFocusRectangle ==null) {
			return;
		}
		int focusState = cameraManager.mFocusState ;
		if (focusState== FOCUSING ) {
			mFocusRectangle.showStart();
		} else if (focusState == FOCUS_SUCCESS) {
			mFocusRectangle.showSuccess();
		} else if (focusState == FOCUS_FAIL) {
			mFocusRectangle.showFail();
		} else {
			mFocusRectangle.clear();
		}
		Log.d("updateFocusIndicator", "focusState = "+focusState);
	}
	
	 protected void clearFocusState() {
		mFocusRectangle.clear();
		
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.shutter_button:
//			setButtonEnable(false);
//			handler.sendEmptyMessage(MESSAGE_DO_FOCUS);
				break;

		default:
			break;
		}
	
		return false;
	}
}
