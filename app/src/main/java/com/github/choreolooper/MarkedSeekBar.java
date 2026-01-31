package com.github.choreolooper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


/**
 * Seekbar widget extension highlighting sections and positions on the seekbar.
 */
public class MarkedSeekBar extends AppCompatSeekBar {

    /// List of highlighted positions, in milliseconds
    ArrayList<Integer> markers = new ArrayList<>();

    /// Start position of the highlighted section, in milliseconds
    float sceneMin = -1;
    /// End position of the highlighted section, in milliseconds
    float sceneMax = -1;


    public MarkedSeekBar(Context context) {
        super(context);
    }

    public MarkedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Add a position to be highlighted.
     *
     * @param progress position of the new mark, in milliseconds
     */
    public void addMarker(int progress) {
        markers.add(progress);
        invalidate();
    }


    /**
     * Remove the highlight the specified position.
     * <p/>
     * If the position currently is not highlighted, no changes are made.
     *
     * @param progress position of the mark to be removed, in milliseconds
     */
    public void removeMarker(int progress) {
        markers.remove(Integer.valueOf(progress));
        invalidate();
    }


    /**
     * Set the highlighted section to the time span specified by the scene.
     *
     * @param scene scene to be highlighted
     */
    public void setScene(Scene scene) {
        if (scene == null) {
            sceneMin = sceneMax = -1;
        } else {
            sceneMin = (float) scene.begin / (float) getMax();
            sceneMax = (float) scene.end / (float) getMax();
        }
        invalidate();
    }


    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight();

        int offsetLeft = getPaddingLeft();

        Paint paint = new Paint();
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        paint.setColor(ContextCompat.getColor(getContext(), typedValue.resourceId));
        paint.setStrokeWidth(12);
        paint.setStrokeCap(Paint.Cap.ROUND);

        int centerY = height/2;

        if (sceneMin >= 0) {
            canvas.drawLine(sceneMin*width + offsetLeft, centerY, sceneMax*width + offsetLeft, centerY, paint);
        }


        paint.setStrokeWidth(6);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (int mark : markers) {
            int posX = (int)(((float)mark / (float)getMax()) * width + offsetLeft);
            canvas.drawLine(posX, (int)(height*0.25),
                    posX, (int)(height*0.75), paint);
        }
    }
}
