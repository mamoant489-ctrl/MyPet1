package com.example.mypet;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MoodPagerAdapter adapter;
    private List<MoodMonth> months;
    private DatabaseReference moodRef;
    private String userId, petId;
    private Map<String, String> moodData = new HashMap<>();


    private final int[] MOOD_COLORS = {
            Color.RED,
            Color.YELLOW,
            Color.GRAY,
            Color.rgb(139,69,19),
            Color.rgb(138,43,226),
            Color.CYAN
    };
    private final String[] MOOD_NAMES = {
            "Активный", "Игривый", "Грустный", "Вялый", "Спокойный", "Переменчивое"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        initFirebase();
        setupViewPager();
        loadMoodData();
        setupLegend();
    }

    private void initFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        userId = user.getUid();

        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets");
        petsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    petId = petsRef.push().getKey();
                } else {
                    petId = snapshot.getChildren().iterator().next().getKey();
                }
                moodRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets").child(petId).child("mood");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        months = generateMonths(6);  // 6 месяцев
        adapter = new MoodPagerAdapter(months, this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
    }

    private List<MoodMonth> generateMonths(int count) {
        List<MoodMonth> monthsList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < count; i++) {
            monthsList.add(new MoodMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)));
            cal.add(Calendar.MONTH, -1);
        }
        return monthsList;
    }

    private void loadMoodData() {
        moodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moodData.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    moodData.put(child.getKey(), child.getValue(String.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupLegend() {
        LinearLayout legend = findViewById(R.id.legendLayout);
        for (int i = 0; i < MOOD_COLORS.length; i++) {
            View colorView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
            params.setMargins(8, 0, 8, 0);
            colorView.setLayoutParams(params);
            colorView.setBackgroundColor(MOOD_COLORS[i]);

            TextView textView = new TextView(this);
            textView.setText(MOOD_NAMES[i]);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(12);

            LinearLayout row = new LinearLayout(this);
            row.addView(colorView);
            row.addView(textView);
            legend.addView(row);
        }
    }

    public void saveMood(int year, int month, int day, int moodIndex) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = sdf.format(cal.getTime());

        if (moodIndex >= 0) {
            moodData.put(dateKey, MOOD_NAMES[moodIndex]);
            moodRef.child(dateKey).setValue(MOOD_NAMES[moodIndex]);
        } else {
            moodData.remove(dateKey);
            moodRef.child(dateKey).removeValue();
        }
        adapter.notifyDataSetChanged();
    }

    public String getMoodColor(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = sdf.format(cal.getTime());
        String mood = moodData.get(dateKey);
        for (int i = 0; i < MOOD_NAMES.length; i++) {
            if (MOOD_NAMES[i].equals(mood)) {
                return String.valueOf(MOOD_COLORS[i]);
            }
        }
        return null;
    }

    public int[] getMoodColors() { return MOOD_COLORS; }
    public String[] getMoodNames() { return MOOD_NAMES; }
}


class MoodPagerAdapter extends RecyclerView.Adapter<MoodPagerAdapter.ViewHolder> {
    private List<MoodMonth> months;
    private MoodActivity activity;

    MoodPagerAdapter(List<MoodMonth> months, MoodActivity activity) {
        this.months = months;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_month, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodMonth month = months.get(position);
        holder.tvMonthName.setText(getMonthName(month.month));
        holder.tvYear.setText(String.valueOf(month.year));

        // Заполняем дни
        holder.dayGrid.removeAllViews();
        Calendar cal = Calendar.getInstance();
        cal.set(month.year, month.month, 1);
        int firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);


        for (int i = 0; i < firstDay; i++) {
            holder.dayGrid.addView(new View(activity));
        }


        for (int day = 1; day <= daysInMonth; day++) {
            MoodDayView dayView = new MoodDayView(activity, month.year, month.month, day);
            holder.dayGrid.addView(dayView);
        }
    }

    @Override
    public int getItemCount() { return months.size(); }

    private String getMonthName(int month) {
        String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        return monthNames[month];
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthName, tvYear;
        LinearLayout dayGrid;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonthName = itemView.findViewById(R.id.tvMonthName);
            tvYear = itemView.findViewById(R.id.tvYear);
            dayGrid = itemView.findViewById(R.id.dayGrid);
        }
    }
}

class MoodMonth {
    int year, month;
    MoodMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }
}
