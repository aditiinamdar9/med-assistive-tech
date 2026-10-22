package com.aidfinder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.aidfinder.R;

import java.util.Collections;
import java.util.List;

/**
 * Plots a device's 1-5 helpfulness ratings over time as a simple line chart -
 * oldest entry on the left, newest on the right. No charting library: for a
 * fixed 1-5 scale this is a handful of lines and dots on a Canvas.
 */
public class RatingTimelineView extends View {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private List<Integer> ratings = Collections.emptyList();

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RatingTimelineView(Context context) {
        super(context);
        init();
    }

    public RatingTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        axisPaint.setColor(ContextCompat.getColor(getContext(), R.color.line));
        axisPaint.setStrokeWidth(density);

        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
        linePaint.setStrokeWidth(density * 2);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
        dotPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.muted));
        labelPaint.setTextSize(density * 11);
        labelPaint.setTextAlign(Paint.Align.RIGHT);
    }

    /** Oldest first - the same order getJournalEntries/addJournalEntry produce. */
    public void setRatings(List<Integer> ratingsOldestFirst) {
        this.ratings = ratingsOldestFirst;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float leftLabelWidth = density * 20;
        float topPad = density * 12;
        float bottomPad = density * 12;

        float plotLeft = leftLabelWidth;
        float plotRight = getWidth() - density * 8;
        float plotTop = topPad;
        float plotBottom = getHeight() - bottomPad;

        if (plotBottom <= plotTop || plotRight <= plotLeft) {
            return;
        }

        // Axis labels and guide lines for 1 and 5, so the scale is legible at a glance.
        canvas.drawText(String.valueOf(MAX_RATING), leftLabelWidth - density * 4, plotTop + labelPaint.getTextSize() / 3, labelPaint);
        canvas.drawText(String.valueOf(MIN_RATING), leftLabelWidth - density * 4, plotBottom + labelPaint.getTextSize() / 3, labelPaint);
        canvas.drawLine(plotLeft, plotTop, plotRight, plotTop, axisPaint);
        canvas.drawLine(plotLeft, plotBottom, plotRight, plotBottom, axisPaint);

        if (ratings.size() < 2) {
            // One point (or none) doesn't make a line - just show the single dot if there is one.
            if (ratings.size() == 1) {
                float y = yForRating(ratings.get(0), plotTop, plotBottom);
                canvas.drawCircle((plotLeft + plotRight) / 2f, y, density * 4, dotPaint);
            }
            return;
        }

        float stepX = (plotRight - plotLeft) / (ratings.size() - 1);
        float prevX = plotLeft;
        float prevY = yForRating(ratings.get(0), plotTop, plotBottom);

        for (int i = 1; i < ratings.size(); i++) {
            float x = plotLeft + stepX * i;
            float y = yForRating(ratings.get(i), plotTop, plotBottom);
            canvas.drawLine(prevX, prevY, x, y, linePaint);
            prevX = x;
            prevY = y;
        }

        for (int i = 0; i < ratings.size(); i++) {
            float x = plotLeft + stepX * i;
            float y = yForRating(ratings.get(i), plotTop, plotBottom);
            canvas.drawCircle(x, y, density * 4, dotPaint);
        }
    }

    private float yForRating(int rating, float plotTop, float plotBottom) {
        float clamped = Math.max(MIN_RATING, Math.min(MAX_RATING, rating));
        float fraction = (clamped - MIN_RATING) / (float) (MAX_RATING - MIN_RATING);
        return plotBottom - fraction * (plotBottom - plotTop);
    }
}
