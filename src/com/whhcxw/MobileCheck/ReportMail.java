package com.whhcxw.MobileCheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.whhcxw.adapter.GridViewAdapter;
import com.whhcxw.camera.picture.PictureManager;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 用于填写邮件内容
 * @author you tao
 *
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ReportMail extends BaseActivity implements OnPageChangeListener{

	private Context context;
	private String[] processTitles;
	private View layout ;
	private ViewPager mViewPager;
	private List<View> pagerViews = new ArrayList<View>();
	private SlideMenuAdapter adapter;
	private LinearLayout loadingLayout;
	private LinearLayout report_head_bar;
	private TextView centerTextView ;
	private StringBuilder stringBuilder = new StringBuilder();;
	private EditText[] editTexts = new EditText[6];
	private ArrayList<HashMap<String, Object>> mPaths_list;
	private LinearLayout mNopicture_layout;
	private GridViewAdapter mGridViewAdapter;
	/**相片编辑状态  1 编辑状态 2 不可编辑 */
	private int mPicEdit = 1;
//	private  RelativeLayout right;

	private String filePath;

	private String model;

	private ArrayList<File> selectedFiles = new ArrayList<File>(); 
	private HashMap<Integer, String> content= new HashMap<Integer, String>();
	
	private   String TEMPFILE = Environment.getExternalStorageDirectory()+"/MobileCheck/temp/";
	
	
	private TextView chooseButton;
	
	
	private Titlebar2 titlebar;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this;
		setContentView(R.layout.report_mail);
		
		Intent i = getIntent();
		this.filePath = i.getExtras().getString("filePath");
		this.content = (HashMap<Integer, String>) i.getExtras().getSerializable("map");
		this.stringBuilder =new StringBuilder(i.getExtras().getString("text"));
		initView();
	}


	private List<View> makeViewPagers(){
		pagerViews.clear();
		for (int i = 0; i < 7; i++) {
			if (i<6) {
				View mailCotnentView = LayoutInflater.from(context).inflate(R.layout.report_mail_content, null);
				initButtons(mailCotnentView,mViewPager, i);
				initEditText(mailCotnentView, i);
				pagerViews.add(mailCotnentView);
			}else {
				View mailCotnentView = LayoutInflater.from(context).inflate(R.layout.report_mail_content_pic, null);
				initButtons(mailCotnentView,mViewPager, i);
				initGridView(mailCotnentView);
				pagerViews.add(mailCotnentView);
			}

		}
		return pagerViews;
	}

	private void initTitle(){
		titlebar = (Titlebar2)findViewById(R.id.titlebar);
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
		titlebar.showLeft(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doReturn();
			}
		});
		
	}
	

	/**
	 * 初始化dialogue 框架布局
	 */
	private void initView(){
	
		initTitle();

		processTitles = context.getResources().getStringArray(R.array.report_case_array);
		

		mViewPager = (ViewPager) findViewById(R.id.report_mail_viewpager);

		loadingLayout = (LinearLayout) findViewById(R.id.report_mail_loading_layout);


		centerTextView = (TextView) findViewById(R.id.titlebar_center_text);
		centerTextView.setText(processTitles[0]);
		centerTextView.setTextSize(18);

		chooseButton = (TextView) findViewById(R.id.titlebar_rightTxt);


		adapter = new SlideMenuAdapter(makeViewPagers());
		mViewPager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		mViewPager.setOnPageChangeListener(ReportMail.this);
		loadingLayout.setVisibility(View.GONE);
		
		
	}

	/**
	 * 初始化编辑框
	 * @param v
	 * @param i
	 */
	private void initEditText(View v,int i){
		editTexts[i] = (EditText) v.findViewById(R.id.report_mail_edittext);
		if (i<6) {
			editTexts[i].setHint(processTitles[i]);
			if (content != null&&!content.isEmpty()) {
				if (!content.get(i).equals(context.getResources().getString(R.string.report_case_process_content_default))) {
					editTexts[i].setText(content.get(i));
				}

			}
		}

	}

	/**
	 * 初始化按钮
	 * @param v
	 * @param mViewPager
	 * @param i
	 */
	private void initButtons(View v,final ViewPager mViewPager,final int i){
		Button preButton = (Button) v.findViewById(R.id.report_mail_pre);
		Button nextButton = (Button) v.findViewById(R.id.report_mail_next);
		if (i<7) {
			preButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (i>0) {
						if(mViewPager != null)
							mViewPager.setCurrentItem(i-1);
					}

				}
			});




			nextButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (i<6) {
						if(mViewPager != null)
							mViewPager.setCurrentItem(i+1);
					}
				}
			});

		}
		if (i == 0) {
			preButton.setVisibility(View.GONE);
		}
		if (i == 6) {
			nextButton.setText(R.string.report_case_ok);
			nextButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean isComplete = checkIsComplete();
					if (!isComplete) {
						showTips();
					}else {
						doReturn();
					}
				}
			});
		}

	}
	/**
	 * 初始化照片
	 * @param v
	 */
	private void initGridView(View v){
		GridView mGridView = (GridView) v.findViewById(R.id.gridview);
		mNopicture_layout = (LinearLayout) v.findViewById(R.id.nopicture_layout);

		getPictures(filePath);
		mGridViewAdapter= new GridViewAdapter(context,mPaths_list,R.layout.gridview_report_item);
		mGridView.setOnItemClickListener(browseOnClickListener);
		mGridView.setAdapter(mGridViewAdapter);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

	}


	/**获取照片路径*/
	public boolean getPictures(String FilePath){
		boolean b =false;
		File file = new File(FilePath);
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
				PictureManager.initInstance(context).showPicture(arrayList,context,postion);
			}else {
				HashMap<String, Object> map = mPaths_list.get(postion);
				if((Boolean) map.get("check")){
					map.put("check", false);
				}else{
					map.put("check", true);
				}
				ImageView chooseview = (ImageView) arg1.findViewById(R.id.delete);
				if (chooseview.getVisibility() == View.VISIBLE) {
					chooseview.setVisibility(View.GONE);
				}else {
					chooseview.setVisibility(View.VISIBLE);
				}
			}
		}
	};


	// 滑动菜单数据适配器
	class SlideMenuAdapter extends PagerAdapter {  

		private List<View> pagerViews;
		public SlideMenuAdapter(List<View> pagers) {
			pagerViews = pagers;
		}
		@Override  
		public int getCount() {  
			//        	View v = menuViews.get(0).findViewWithTag(SlideMenuUtil.ITEM_MOBILE);
			//        	v.setBackgroundResource(R.drawable.menu_bg);
			return pagerViews.size();  
		}  

		@Override  
		public boolean isViewFromObject(View arg0, Object arg1) {  
			return arg0 == arg1;  
		}  

		@Override  
		public int getItemPosition(Object object) {  
			// TODO Auto-generated method stub  
			return super.getItemPosition(object);  
		}  

		@Override  
		public void destroyItem(View arg0, int arg1, Object arg2) {  
			// TODO Auto-generated method stub  
			((ViewPager) arg0).removeView(pagerViews.get(arg1));  
		}  

		@Override  
		public Object instantiateItem(View arg0, int arg1) {  
			// TODO Auto-generated method stub  
			((ViewPager) arg0).addView(pagerViews.get(arg1));

			return pagerViews.get(arg1);  
		}  

		@Override  
		public void restoreState(Parcelable arg0, ClassLoader arg1) {  
			// TODO Auto-generated method stub  

		}  

		@Override  
		public Parcelable saveState() {  
			// TODO Auto-generated method stub  
			return null;  
		}  

		@Override  
		public void startUpdate(View arg0) {  
			// TODO Auto-generated method stub  

		}  

		@Override  
		public void finishUpdate(View arg0) {  
			// TODO Auto-generated method stub  

		}  
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		centerTextView.setText(processTitles[arg0]);
		if(arg0 == 6){
			chooseButton.setVisibility(View.VISIBLE);
			chooseButton.setText(R.string.report_case_photo_choose);
			titlebar.showRight(tiltleRightOnClickListener);
		}else {
			chooseButton.setVisibility(View.GONE);
			titlebar.goneRight();
		}
	}

	
	OnClickListener tiltleRightOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mPicEdit == 1){
				mPicEdit =2 ;
				chooseButton.setText(R.string.report_case_photo_preview);
			}else if(mPicEdit == 2){
				mPicEdit =1;
				chooseButton.setText(R.string.report_case_photo_choose);
			}
		}
	};

	/**
	 * 检查是否填写完毕
	 */
	private boolean checkIsComplete(){
		boolean isComplete = true;
		if (stringBuilder.length()>0) {
			stringBuilder.delete(0, stringBuilder.length()-1);


		}
		if (content != null) {
			content.clear();
		}else {
			content = new HashMap<Integer, String>();
		}
		String[] nums = context.getResources().getStringArray(R.array.report_case_array_num);
		for (int i = 0; i < editTexts.length; i++) {
			if (editTexts[i].getText().toString().isEmpty()) {
				isComplete = false;
			}
			String text = null;
			if (editTexts[i].getText().toString().trim().equals("")) {
				text = context.getResources().getString(R.string.report_case_process_content_default);
			}else {
				text = editTexts[i].getText().toString();
			}

			stringBuilder.append(nums[i]+processTitles[i]+":"+text);
			stringBuilder.append("\n");
			content.put(i, text);

		}


		selectedFiles.clear();
		HashMap<String , Object> map;
		File picFile;
		for(int i = 0; i<mPaths_list.size();i++){
			map = mPaths_list.get(i);
			if(!(Boolean) map.get("check")){
				picFile = new File(map.get("path").toString());
				selectedFiles.add(picFile);
			}
		}


		return isComplete;
	}

	/**
	 * 处理返回
	 */
	private void doReturn(){
		final ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setCancelable(false);
		progressDialog.setTitle(R.string.report_case_tips_title);
		progressDialog.setMessage("正在处理照片，请稍后");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				Intent i = new Intent(ReportMail.this, Report.class);
				Bundle bundle = new Bundle();
				bundle.putString("text", stringBuilder.toString());
				bundle.putSerializable("map", content);
				bundle.putSerializable("files", copyFile(selectedFiles, TEMPFILE));
				i.putExtras(bundle);
				setResult(Report.MAIL, i);
				finish();
			}
		});
	}


	/**
	 * 弹出提示框
	 */
	private void showTips(){
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(R.string.report_case_tips_title);
		builder.setMessage(R.string.report_case_tips_content);
		builder.setPositiveButton(R.string.report_case_tips_sure, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				doReturn();
			}
		});

		builder.setNegativeButton(R.string.report_case_tips_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.show();
	}


	/**
	 * 批量复制文件 ，并重命名
	 * @param files
	 * @param path
	 */
	private ArrayList<File> copyFile(final ArrayList<File> files,final String path){
		ArrayList<File> filesNow = new ArrayList<File>();
		if (files!= null&&files.size()>0) {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}else {
				deleteFiles(file.listFiles());
			}
		
			for (int i =0; i < files.size(); i++) {
				File currentFile = files.get(i);
				copy(currentFile.getAbsolutePath(), path+i+".jpg");
				File filenow = new File(path+i+".jpg");
				filesNow.add(filenow);
			}
			
		}else {
			Log.e("com.whhcxw.MobileCheck", "selected files is null");
		}
		
		return filesNow;

	}


	/**
	 * 复制文件
	 * @param fileFrom
	 * @param fileTo
	 * @return
	 */
	private boolean copy(final String fileFrom, final String fileTo) {  
		boolean isCommpleted = true;
		FileInputStream in = null;
		FileOutputStream out = null;
		try {  
			 in = new java.io.FileInputStream(fileFrom);  
			 out = new FileOutputStream(fileTo);  
			byte[] bt = new byte[1024];  
			int count;  
			while ((count = in.read(bt)) > 0) {  
				out.write(bt, 0, count);  
			}  
			in.close();  
			out.close();  
			Log.d("com.whhcxw.MobileCheck", "copy file success");
		} catch (IOException ex) {  
			Log.e("com.whhcxw.MobileCheck", "copy file faile");
			ex.printStackTrace();
			isCommpleted = false;
			try {
				in.close();
				out.close(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			
		}  
		return isCommpleted;

	} 
	
	/**
	 * 删除文件
	 * @param files
	 */
	private void deleteFiles(File[] files){
		if (files!=null&&files.length>0) {
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			doReturn();
		}
		return super.onKeyDown(keyCode, event);
	}

}
