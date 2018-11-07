package com.thebaileybrew.flix2.utils.behaviors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.material.appbar.AppBarLayout;
import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.R;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PosterBehaviorUtils extends CoordinatorLayout.Behavior<ImageView> {
    Animation fadeOut, fadeIn;

    public PosterBehaviorUtils(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull ImageView child, @NonNull View dependency) {
        return dependency instanceof AppBarLayout;
    }


    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, ImageView child, View directTarget,
                                       View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout,
                child, directTarget, target, axes,type);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, ImageView child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type);
        fadeOut = AnimationUtils.loadAnimation(FlixApplication.getContext(), R.anim.anim_fade_out_ratingbar);
        fadeIn = AnimationUtils.loadAnimation(FlixApplication.getContext(), R.anim.anim_fade_in_ratingbar);


        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
            child.startAnimation(fadeOut);
            child.setVisibility(View.INVISIBLE);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            child.startAnimation(fadeIn);
            child.setVisibility(View.VISIBLE);
        }
    }

}
