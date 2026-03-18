package com.example.mypet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class LineChartView extends View {
    private float[] values;
    private String[] labels;
    private Paint linePaint, fillPaint, gridPaint, textPaint;
    private Path linePath, fillPath;
    private float maxValue, minValue;

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(4f);
        linePaint.setColor(Color.BLUE);
        linePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.parseColor("#4080C0FF"));
        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(float[] values, String[] labels) {
        this.values = values;
        this.labels = labels;
        if (values != null && values.length > 0) {
            maxValue = minValue = values[0];
            for (float v : values) {
                if (v > maxValue) maxValue = v;
                if (v < minValue) minValue = v;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values == null || values.length < 2) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 60f;
        float graphWidth = width - 2 * padding;
        float graphHeight = height - 2 * padding;
        float valueRange = maxValue - minValue;

        // Сетка
        canvas.save();
        canvas.clipRect(padding, padding, width - padding, height - padding);
        for (int i = 0; i <= 5; i++) {
            float y = padding + (graphHeight * i / 5);
            canvas.drawLine(padding, y, width - padding, y, gridPaint);
        }
        for (int i = 0; i <= values.length - 1; i++) {
            float x = padding + (graphWidth * i / (values.length - 1));
            canvas.drawLine(x, padding, x, height - padding, gridPaint);
        }
        canvas.restore();

        // Линия графика
        linePath = new Path();
        fillPath = new Path();
        for (int i = 0; i < values.length; i++) {
            float x = padding + (graphWidth * i / (values.length - 1));
            float normalizedY = (maxValue - values[i]) / valueRange;
            float y = padding + (graphHeight * normalizedY);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height - padding);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(width - padding, height - padding);
        fillPath.lineTo(padding, height - padding);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Точки
        for (int i = 0; i < values.length; i++) {
            float x = padding + (graphWidth * i / (values.length - 1));
            float normalizedY = (maxValue - values[i]) / valueRange;
            float y = padding + (graphHeight * normalizedY);
            canvas.drawCircle(x, y, 8f, linePaint);
        }

        // Подписи
        textPaint.setTextSize(24f);
        for (int i = 0; i < labels.length; i++) {
            float x = padding + (graphWidth * i / (labels.length - 1));
            canvas.drawText(labels[i].substring(5), x, height - 10f, textPaint);
        }
    }
}
