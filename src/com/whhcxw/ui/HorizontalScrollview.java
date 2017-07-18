package com.whhcxw.ui;

import java.util.HashMap;

import com.whhcxw.MobileCheck.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

@SuppressLint("ResourceAsColor")
public class HorizontalScrollview extends HorizontalScrollView {
	Context context;
	int prevIndex = 0;
	private OnHorizontalScrollviewItemSelectedListener itemSelectedListener;
	private OnScrollChangedListener onScrollChangedListener;

	public HorizontalScrollview(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.setSmoothScrollingEnabled(true);

	}

	public void setAdapter(Context context, HorizontalScrollviewListAdapter mAdapter) {

		try {
			fillViewWithAdapter(mAdapter);
		} catch (ZeroChildException e) {

			e.printStackTrace();
		}
	}

	@SuppressLint("ResourceAsColor")
	private void fillViewWithAdapter(final HorizontalScrollviewListAdapter mAdapter)
			throws ZeroChildException {
		if (getChildCount() == 0) {
			throw new ZeroChildException(
					"CenterLockHorizontalScrollView must have one child");
		}
		if (getChildCount() == 0 || mAdapter == null)
			return;

		final ViewGroup parent = (ViewGroup) getChildAt(0);

		parent.removeAllViews();
		for ( int i =0; i < mAdapter.getCount(); i++) {
			View v = mAdapter.getView(i, null, parent);
			View buttonView = v.findViewById(R.id.horizen_list_item_image);
			ImageButton button = ((ImageButton)(buttonView));
			button.setTag(i);
			v.setTag(i);
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final HashMap<String, Object> map = mAdapter.getItem((Integer)v.getTag());
					if (itemSelectedListener != null) {
						itemSelectedListener.onSelected(v,(Integer)v.getTag(),map);

					}

				}
			});
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final HashMap<String, Object> map = mAdapter.getItem((Integer)v.getTag());
					if (itemSelectedListener != null) {
						itemSelectedListener.onSelected(v,(Integer)v.getTag(),map);
						
					}
					
				}
			});
			
			boolean enable = (Boolean) mAdapter.getItem((Integer)v.getTag()).get("enable");
//			((ImageButton)(v.findViewById(R.id.horizen_list_item_image))).setEnabled(enable);
			v.setEnabled(enable);
			button.setEnabled(enable);
			parent.addView(v);

		}
	}

	public interface OnHorizontalScrollviewItemSelectedListener{
		void onSelected(View view,int position,HashMap<String, Object> itemData);
	}

	public void setOnHorizontalScrollviewItemSelectedListener(OnHorizontalScrollviewItemSelectedListener itemSelectedListener){
		this.itemSelectedListener = itemSelectedListener;
	}

	public interface OnScrollChangedListener{
		/**
		 * 
		 * @param l Current horizontal scroll origin.
		 * @param t Current vertical scroll origin.
		 * @param oldl Previous horizontal scroll origin.
		 * @param oldt Previous vertical scroll origin.
		 */
		void onScrollChanged(int l, int t, int oldl, int oldt);
	}

	public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener){
		this.onScrollChangedListener = onScrollChangedListener;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollChangedListener != null) {
			onScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
		}
	}



	public void setCenter(int index) {

		ViewGroup parent = (ViewGroup) getChildAt(0);

		View preView = parent.getChildAt(prevIndex);
		preView.setBackgroundColor(Color.parseColor("#64CBD8"));
		android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(5, 5, 5, 5);
		preView.setLayoutParams(lp);

		View view = parent.getChildAt(index);
		view.setBackgroundColor(Color.RED);

		int screenWidth = ((Activity) context).getWindowManager()
				.getDefaultDisplay().getWidth();

		int scrollX = (view.getLeft() - (screenWidth / 2))
				+ (view.getWidth() / 2);
		this.smoothScrollTo(scrollX, 0);
		prevIndex = index;
	}



}
