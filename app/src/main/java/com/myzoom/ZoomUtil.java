package com.myzoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by rjhy on 14-10-31.
 */
public class ZoomUtil {

    private AnimatorSet mCurrentAnimator;
    private Context context;
    private static ZoomUtil zoomUtil;

    private ImageView zoomView;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private int duration;

    public void setZoomView(ImageView zoomView) {
        this.zoomView = zoomView;
    }

    private ZoomUtil(Context context) {
        this.context = context;
        duration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public static ZoomUtil getIntance(Context context) {
        if (zoomUtil == null) {
            zoomUtil = new ZoomUtil(context);
        }
        return zoomUtil;
    }

    public void zoom(ImageView view) {
        zoom(view, null);
    }

    public void zoom(ImageView view, Bitmap bitmap) {
        View decorView = view.getRootView();
        if (zoomView == null) {
            FrameLayout frameLayout = (FrameLayout) decorView.findViewById(android.R.id.content);
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setVisibility(View.GONE);
            frameLayout.addView(imageView);
            zoomView = imageView;
        }
        zoom(view, zoomView, bitmap);
    }

    public void zoom(ImageView view, ImageView zoomView, Bitmap bitmap) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final Rect start = new Rect();
        Rect end = new Rect();
        Point point = new Point();

        View parent = (View) zoomView.getParent();
        int paddingLeft = parent.getPaddingLeft();
        int paddingTop = parent.getPaddingTop();
//        Log.i(TAG, "paddingLeft:" + paddingLeft + "paddingTop:" + paddingTop);

        view.getGlobalVisibleRect(start);
        parent.getGlobalVisibleRect(end, point);

        start.offset(-point.x, -point.y);
        end.offset(-point.x + paddingLeft, -point.y + paddingTop);
        float startScale = computeScale(start, end);

        view.setAlpha(0f);
        zoomView.setVisibility(View.VISIBLE);
        if (bitmap != null) {
            zoomView.setImageBitmap(bitmap);
        } else {
            zoomView.setImageDrawable(view.getDrawable());
        }

        zoomView.setPivotX(0f);
        zoomView.setPivotY(0f);

        animatorZoomIn(view, zoomView, start, end, startScale);
    }

    private void animatorZoomIn(final ImageView view, final ImageView zoomView, final Rect start, final Rect end, final float startScale) {
        final AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(zoomView, "x", start.left, end.left))
                .with(ObjectAnimator.ofFloat(zoomView, "y", start.top, end.top))
                .with(ObjectAnimator.ofFloat(zoomView, "scaleX", startScale, 1f))
                .with(ObjectAnimator.ofFloat(zoomView, "scaleY", startScale, 1f));
        set.setDuration(duration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
        zoomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatorZoomOut(view, zoomView, start, end, startScale);
            }
        });
    }

    private void animatorZoomOut(final ImageView view, final ImageView zoomView, Rect start, Rect end, float startScale) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(zoomView, "x", start.left))
                .with(ObjectAnimator.ofFloat(zoomView, "y", start.top))
                .with(ObjectAnimator.ofFloat(zoomView, "scaleX", startScale))
                .with(ObjectAnimator.ofFloat(zoomView, "scaleY", startScale));
        set.setDuration(duration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setAlpha(1f);
                zoomView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setAlpha(1f);
                zoomView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    private static float computeScale(Rect start, Rect end) {
        float startScale = 1.0f;
        if ((float)end.width()/end.height() > (float)start.width()/start.height()) {
            startScale = (float)start.height()/end.height();
            float startWidth = startScale * end.width();
            float detailWidth = (startWidth - start.width())/2;
            start.left -= detailWidth;
            start.right += detailWidth;
        } else {
            startScale = (float)start.width()/end.width();
            float startHeight = startScale * end.height();
            float detailHeight = (startHeight - start.height())/2;
            start.top -= detailHeight;
            start.bottom += detailHeight;
        }
        return startScale;
    }
}
