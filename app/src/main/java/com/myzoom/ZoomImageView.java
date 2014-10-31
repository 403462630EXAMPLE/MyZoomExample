package com.myzoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by rjhy on 14-10-31.
 */
public class ZoomImageView extends ImageView {

    private ZoomUtil zoomUtil;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        zoomUtil = ZoomUtil.getIntance(context);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomUtil.zoom((ImageView)v, bitmap);
            }
        });
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        zoomUtil.setZoomView(null);
        return super.onSaveInstanceState();
    }

}
