package com.whhcxw.camera.ui;



import com.whhcxw.MobileCheck.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;


public class FocusRectangle extends View {

    @SuppressWarnings("unused")
    private static final String TAG = "FocusRectangle";

    public FocusRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setDrawable(int resid) {
        setBackgroundDrawable(getResources().getDrawable(resid));
    }

    public void showStart() {
        setDrawable(R.drawable.camera_focus_focusing);
    }

    public void showSuccess() {
        setDrawable(R.drawable.camera_focus_focused);
    }

    public void showFail() {
        setDrawable(R.drawable.camera_focus_focus_failed);
    }

    public void clear() {
        setBackgroundDrawable(null);
    }
}