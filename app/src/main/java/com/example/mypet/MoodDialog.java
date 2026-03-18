package com.example.mypet;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MoodDialog extends Dialog {
    private MoodActivity activity;
    private int year, month, day;

    public MoodDialog(Context context, int year, int month, int day) {
        super(context);
        this.activity = (MoodActivity) context;
        this.year = year;
        this.month = day;
        this.day = day;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mood);

        LinearLayout colorsLayout = findViewById(R.id.colorsLayout);
        int[] colors = activity.getMoodColors();
        String[] names = activity.getMoodNames();

        for (int i = 0; i < colors.length; i++) {
            View colorView = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 60);
            params.setMargins(8, 8, 8, 8);
            colorView.setLayoutParams(params);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(colors[i]);
            colorView.setBackground(bg);

            int finalI = i;
            colorView.setOnClickListener(v -> {
                activity.saveMood(year, month, day, finalI);
                dismiss();
            });

            colorsLayout.addView(colorView);
        }

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            activity.saveMood(year, month, day, -1);
            dismiss();
        });
    }
}
