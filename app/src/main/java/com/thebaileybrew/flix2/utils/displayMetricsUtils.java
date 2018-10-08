package com.thebaileybrew.flix2.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class displayMetricsUtils {

    public static int calculateGridColumn(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 200;
        int columnCount = (int) (dpWidth / scalingFactor);
        if (columnCount < 2) {
            columnCount = 2;
        }
        return columnCount;
    }

    public static float displayToPixel(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float screenToPixel(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
}
