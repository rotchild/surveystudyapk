package com.whhcxw.ui;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class PictureProgressBar extends ProgressBar {

	private Context mContext;

    private String text; 
    private Paint mPaint; 
	public PictureProgressBar(Context context) {
		super(context);
		this.mContext = context;
		initText(); 
	}

	  public PictureProgressBar(Context context, AttributeSet attrs, int defStyle) { 
	        super(context, attrs, defStyle); 
	        this.mContext = context;
	        initText(); 
	    } 
	  
	    public PictureProgressBar(Context context, AttributeSet attrs) { 
	        super(context, attrs); 
	        this.mContext = context;
	        initText(); 
	    } 

	    // 初始化，画笔 
	    private void initText() { 
	        this.mPaint = new Paint(); 
	        this.mPaint.setAntiAlias(true); 
	        this.mPaint.setColor(Color.RED); 
	        this.mPaint.setTextSize(30);
	    } 
	    
	    @Override
	    public void setProgress(int progress) { 
	        setText(progress); 
	        super.setProgress(progress); 
	  
	    } 

	    @Override
	    protected synchronized void onDraw(Canvas canvas) { 
	        super.onDraw(canvas); 
	        Rect rect = new Rect(); 
	        this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect); 
	        int x = (getWidth() / 2) - rect.centerX(); 
	        int y = (getHeight() / 2) - rect.centerY(); 
	        canvas.drawText(this.text, x, y, this.mPaint); 
	    } 
	    
	    // 设置文字内容 
	    private void setText(int progress) { 
//	        int i = (int) ((progress * 1.0f / this.getMax()) * 100); 
//	        this.text = String.valueOf(i) + "%"; 
	    	
	    	this.text = String.valueOf(progress);
	    } 


}
