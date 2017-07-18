package com.whhcxw.ui;

import com.whhcxw.MobileCheck.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Titlebar2 extends RelativeLayout {

	private final String LEFT_TAG = "left";
	private final String CENTER_TAG = "center";
	private final String RIGHT_TAG = "right";

	private Context mContext;
	private Resources mResources;

	private View mLeftLayout;
	private ImageView mLeftImg;
	private TextView mLeftTxt;
	private ImageView mLeftBackImg;
	
	private View mCenterLayout;
	private TextView mCenterText;

	private View mRightLayout;
	private ImageView mRightImg;
	private TextView mRightTxt;
	private int bgId = 0;
	private int leftBgId = 0;
	private int rightBgId = 0;
	private LinearLayout titleBarLinear;

	public Titlebar2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		  TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UItitlebar, defStyle, 0);
		  bgId =  a.getResourceId(R.styleable.UItitlebar_titlebarBackground, 0);
		  leftBgId = a.getResourceId(R.styleable.UItitlebar_titlebarLeftBackground, 0);
		  rightBgId = a.getResourceId(R.styleable.UItitlebar_titlebarRightBackground, 0);
	      a.recycle();
	     setAlwaysDrawnWithCacheEnabled(false);
	     initView();
	}

	public Titlebar2(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.mContext = context;

	}

	public Titlebar2(Context context) {
		super(context);
		this.mContext = context;
		initView();
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
	}

	private void initView() {
		mResources = mContext.getResources();
		LayoutInflater.from(mContext).inflate(R.layout.ui_titlebar2, this, true);

		mLeftLayout = findViewById(R.id.titlebar_leftLayout);
		mLeftImg = (ImageView) findViewById(R.id.titlebar_leftBtn);
		mLeftBackImg = (ImageView)findViewById(R.id.titlebar_leftBackBtn);
		mLeftTxt = (TextView) findViewById(R.id.titlebar_leftTxt);

		mCenterLayout = findViewById(R.id.titlebar_centerLayout);
		mCenterText = (TextView) findViewById(R.id.titlebar_center_text);

		mRightLayout = findViewById(R.id.titlebar_rightLayout);
		// mLeftImg = (ImageView) findViewById(R.id.titlebar_rightBtn);
		mRightImg = (ImageView) findViewById(R.id.titlebar_rightBtn);
		mRightTxt = (TextView) findViewById(R.id.titlebar_rightTxt);
		
		titleBarLinear = (LinearLayout) findViewById(R.id.titlebar_layout);
		if (bgId!=0) {
			titleBarLinear.setBackgroundResource(bgId);
		}
		if (leftBgId!=0) {
			mLeftLayout.setBackgroundResource(leftBgId);
		}
		if (rightBgId!=0) {
			mRightLayout.setBackgroundResource(rightBgId);
		}
		
		

		mLeftLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) mContext).finish();
			}
		});

	}

	public void setCenterText(int txtResId) {
		String text = mResources.getString(txtResId);
		setCenterText(text);
	}

	public void setCenterText(String text) {
		mCenterText.setText(text);
	}

	public void showLeft() {
		layoutVisibility(LEFT_TAG);
	}

	public void showLeft(OnClickListener l) {
		mLeftLayout.setOnClickListener(l);
		layoutVisibility(LEFT_TAG);
	}
	
	public void setLeftBackground(Drawable drawable){
		mLeftLayout.setBackgroundDrawable(drawable);
	}

	public void setRightBackground(Drawable drawable){
		mRightLayout.setBackgroundDrawable(drawable);
	}
	
	public void setLeftText(int redId) {
		String text = mResources.getString(redId);
		setLeftText(text);
	}

	public void setLeftText(String text) {
		mLeftTxt.setText(text);
	}

	public void setLeftImageRes(int redId) {
		mLeftImg.setImageResource(redId);
	}

	public void setLeftBackImagesRes(int redId){
		mLeftBackImg.setImageResource(redId);
	}
	
	public void showRight() {
		layoutVisibility(RIGHT_TAG);
	}
	
	public void goneRight(){
		mRightLayout.setVisibility(View.GONE);
	}

	public void showRight(OnClickListener l) {
		mRightLayout.setOnClickListener(l);
		layoutVisibility(RIGHT_TAG);
	}

	public void setRightText(int redId) {
		String text = mResources.getString(redId);
		setRightText(text);
	}

	public void setRightText(String text) {
		mRightTxt.setText(text);
	}

	public void setRightImageRes(int redId) {
		mRightImg.setImageResource(redId);
	}

	public void rightImageOnclick(OnClickListener l) {
		mRightImg.setOnClickListener(l);
		layoutVisibility(RIGHT_TAG);
	}

	public void layoutVisibility(String tag) {
		if (tag.equals(LEFT_TAG)) {
			mLeftLayout.setVisibility(View.VISIBLE);
		} else if (tag.equals(CENTER_TAG)) {
			mCenterLayout.setVisibility(View.VISIBLE);
		} else if (tag.equals(RIGHT_TAG)) {
			mRightLayout.setVisibility(View.VISIBLE);
		}
	}

	

}