package com.example.mypet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MoodDayView extends FrameLayout {
    private MoodActivity activity;
    private int year, month, day;

    public MoodDayView(Context context, int year, int month, int day) {
        super(context);
        this.activity = (MoodActivity) context;
        this.year = year;
        this.month = month;
        this.day = day;

        initView();
    }

    private void initView() {
        setLayoutParams(new LayoutParams(60, 60));
        setPadding(4, 4, 4, 4);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.LTGRAY);
        String moodColor = activity.getMoodColor(year, month, day);
        if (moodColor != null) {
            bg.setColor(Color.parseColor(moodColor));
        }
        setBackground(bg);

        TextView dayText = new TextView(getContext());
        dayText.setText(String.valueOf(day));
        dayText.setTextColor(Color.WHITE);
        dayText.setGravity(Gravity.CENTER);
        dayText.setTextSize(14);
        addView(dayText);

        setOnClickListener(v -> showMoodDialog());
    }

    private void showMoodDialog() {
        MoodDialog dialog = new MoodDialog(activity, year, month, day);
        dialog.show();
    }
}
