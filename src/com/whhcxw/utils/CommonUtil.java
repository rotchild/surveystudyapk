package com.whhcxw.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class CommonUtil {

	/**
	 * 根据手机分辨率从dp转成px
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static  int dip2px(Context context, float dpValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (dpValue * scale + 0.5f);  
	}  

	/** 
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
	 */  
	public static  int px2dip(Context context, float pxValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (pxValue / scale + 0.5f)-15;  
	}


	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param fontScale（DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(float pxValue, float fontScale) {
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param fontScale（DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(float spValue, float fontScale) {
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * 获取屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getPhoneWidth(Context context){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	/**
	 * 获取屏幕高度
	 * @param context
	 * @return
	 */
	public static int getPhoneHight(Context context){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}
	
	
	
}
