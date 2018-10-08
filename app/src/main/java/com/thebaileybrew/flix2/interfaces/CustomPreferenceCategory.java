package com.thebaileybrew.flix2.interfaces;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class CustomPreferenceCategory extends PreferenceCategory {

    public CustomPreferenceCategory(Context context) {
        super(context);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = view.findViewById(android.R.id.title);
        titleView.setTextColor(Color.argb(225,0,230,118));
        titleView.setTextSize(20);
    }
}
