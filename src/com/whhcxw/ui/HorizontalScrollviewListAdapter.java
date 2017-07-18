package com.whhcxw.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.whhcxw.MobileCheck.R;
import com.whhcxw.utils.CommonUtil;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HorizontalScrollviewListAdapter extends ArrayAdapter<HashMap<String, Object>> {
	Context context;
	private ArrayList<HashMap<String, Object>> list;
	int layoutId;
	Holder holder;
	public View view;
	public int currPosition = 0;
	private int padding = 0;

	public HorizontalScrollviewListAdapter(Context context, int textViewResourceId,
			ArrayList<HashMap<String, Object>> list,int scrollerViewWidth) {
		super(context, android.R.layout.simple_list_item_1, list);
		this.context = context;
		this.list = list;
		layoutId = textViewResourceId;
		padding = calcViews(scrollerViewWidth);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public HashMap<String, Object> getItem(int position) {
		return list.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		RelativeLayout layout;

		if (convertView == null) {

			layout = (RelativeLayout) View.inflate(context, layoutId, null);
			layout.setPadding(padding, 0, padding, 0);
			holder = new Holder();
			if (layoutId==R.layout.horizon_list_item) {
				holder.title = (TextView) layout.findViewById(R.id.horizen_list_item_text);
				holder.icon = (ImageButton) layout.findViewById(R.id.horizen_list_item_image);
//				holder.radioButton = (RadioButton) layout.findViewById(R.id.horizon_RadioButton);
			}
		
			layout.setTag(holder);

		} else {
			layout = (RelativeLayout) convertView;
			view = layout;
			holder = (Holder) layout.getTag();
		}
		HashMap<String, Object> map= getItem(position);
		holder.title.setText((String)(map.get("title")));
		holder.icon.setImageResource((Integer)(map.get("picN")));	
//		holder.radioButton.setText((String)(map.get("title")));
//		Drawable top = context.getResources().getDrawable((Integer)(map.get("picN")));
//		holder.radioButton.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

		return layout;
	}

	private class Holder {
		public TextView title;
		public ImageButton icon;

	}
//	private class Holder {
//		public RadioButton radioButton;
//		
//	}
	public int getCurrentPosition(){
		return currPosition;
	}
	
	/**
	 * calc the width of each item
	 * @return
	 */
	private int calcViews(int scrollerViewWidth){
		int totalWidth = scrollerViewWidth; 
		int guiderWidth = 0;
		int sigleWidth = (totalWidth-guiderWidth)/4;
		int realSigleWidth = CommonUtil.dip2px(context, 40);
		int padding = (sigleWidth-realSigleWidth)/2;
		
		return padding;
	}
}
