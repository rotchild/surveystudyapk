package com.whhcxw.adapter;

import java.util.HashMap;
import java.util.List;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GridViewAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private final String TAG = this.getClass().getSimpleName();
	private Context mContext;
	
	private List<HashMap<String, Object>> mData;
	private int layout;
	public GridViewAdapter(Context _c,List<HashMap<String, Object>> _mData,int layout) {
		this.mContext = _c;
		this.mData = _mData;
		this.layout = layout;
	}
	
	public void nitifyData(List<HashMap<String, Object>> _data){
		mData = _data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder =null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(layout, null);
			holder.imageView = (ImageView)convertView.findViewById(R.id.picture);
			holder.deleteimageView = (ImageView)convertView.findViewById(R.id.delete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		boolean b = false;
		try {
			b = (Boolean) mData.get(position).get("check");
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(!b){
			holder.deleteimageView.setVisibility(View.VISIBLE);
		}else {
			holder.deleteimageView.setVisibility(View.GONE);
		}
		
		try{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mData.get(position).get("path").toString(), options);
			options.inSampleSize = G.computeInitialSampleSize(options, -1, 80 * 80);
			options.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeFile(mData.get(position).get("path").toString(), options);	
			holder.imageView.setImageBitmap(bitmap);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return convertView;
	}
	
	private class ViewHolder{
		public ImageView imageView;
		public ImageView deleteimageView;
	}

	
}
