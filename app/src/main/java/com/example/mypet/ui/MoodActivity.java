package com.example.mypet.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mypet.data.FirebasePaths;
import com.example.mypet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

public class MoodActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;

    private DatabaseReference moodRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        calendarView = findViewById(R.id.calendarView);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        moodRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebasePaths.USERS)
                .child(uid)
                .child("pets")
                .child("mood");

        loadMoods();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            showMoodMenu(date);
        });
    }

    private void showMoodMenu(CalendarDay date){

        PopupMenu popupMenu = new PopupMenu(this, calendarView);

        popupMenu.getMenu().add("Активный");
        popupMenu.getMenu().add("Игривый");
        popupMenu.getMenu().add("Грустный");
        popupMenu.getMenu().add("Вялый");
        popupMenu.getMenu().add("Спокойный");
        popupMenu.getMenu().add("Переменчивое");

        popupMenu.setOnMenuItemClickListener(item -> {

            String mood = item.getTitle().toString();

            int color = getMoodColor(mood);

            calendarView.addDecorator(
                    new MoodDecorator(date, color)
            );

            saveMood(date, mood, color);

            return true;
        });

        popupMenu.show();
    }

    private int getMoodColor(String mood){

        switch (mood){

            case "Активный":
                return 0xFF6EC5FF;

            case "Игривый":
                return 0xFF8DE37D;

            case "Грустный":
                return 0xFFBDBDBD;

            case "Вялый":
                return 0xFFFFD166;

            case "Спокойный":
                return 0xFFC7B8EA;

            case "Переменчивое":
                return 0xFFFF8FAB;
        }

        return 0xFFFFFFFF;
    }

    private void saveMood(CalendarDay date,
                          String mood,
                          int color){

        String key = createKey(date);

        moodRef.child(key).child("mood").setValue(mood);

        moodRef.child(key).child("color").setValue(color);
    }

    private void loadMoods(){

        moodRef.get().addOnCompleteListener(task -> {

            if(task.isSuccessful()){

                DataSnapshot snapshot = task.getResult();

                for(DataSnapshot data : snapshot.getChildren()){

                    String key = data.getKey();

                    Long colorLong =
                            data.child("color").getValue(Long.class);

                    if(key == null || colorLong == null)
                        continue;

                    int color = colorLong.intValue();

                    String[] parts = key.split("-");

                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);

                    CalendarDay date =
                            CalendarDay.from(year, month, day);

                    calendarView.addDecorator(
                            new MoodDecorator(date, color)
                    );
                }
            }
        });
    }

    private String createKey(CalendarDay date){

        return date.getYear() + "-"
                + date.getMonth() + "-"
                + date.getDay();
    }

    public static class MoodDecorator
            implements DayViewDecorator {

        private final CalendarDay day;
        private final int color;

        public MoodDecorator(CalendarDay day,
                             int color){

            this.day = day;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(
                CalendarDay calendarDay) {

            return calendarDay.equals(day);
        }

        @Override
        public void decorate(
                @NonNull DayViewFacade view) {

            view.setBackgroundDrawable(
                    new ColorDrawable(color)
            );
        }
    }
}
