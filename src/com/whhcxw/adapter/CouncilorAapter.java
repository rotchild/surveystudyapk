package com.whhcxw.adapter;

import java.util.List;
import java.util.Map;

import com.whhcxw.MobileCheck.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CouncilorAapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private Context mContext;
	private List<Map<String, Object>> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	
	public CouncilorAapter(Context mContext, List<Map<String, Object>> mData, int mResourceID) {
		super();
		this.mContext = mContext;
		this.mData = mData;
		this.mInflater = LayoutInflater.from(mContext);
		this.mResourceID = mResourceID;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public void updata(List<Map<String, Object>> mData){
		this.mData = mData;
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(mResourceID, null);
			holder.name=(TextView)convertView.findViewById(R.id.name);
			//holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkbox);
			holder.imageView = (ImageView)convertView.findViewById(R.id.imageview);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		
		final Map<String, Object> map = mData.get(position);		
		boolean checked = (Boolean) map.get("check");
		
		if(checked){
			holder.imageView.setBackgroundResource(R.drawable.checkbox_btn_on);
		}else{
			holder.imageView.setBackgroundResource(R.drawable.checkbox_btn_off);
		}
		
		Log.d("councilorAdapter", position + "" +checked + "");		
		/*holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				map.put("check", isChecked);	
			}
		});*/
		
		
		//holder.checkBox.setChecked(checked);		
		holder.name.setText(map.get("RealName").toString());
		return convertView;
	}
	
	
	private class ViewHolder{
		public TextView name;
		//public CheckBox checkBox;
		public ImageView imageView;
	}

}
