package com.example.mypet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackerActivity extends AppCompatActivity {

    private MapView map;
    private IMapController mapController;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Polyline pathOverlay;
    private List<GeoPoint> pathPoints = new ArrayList<>();
    private Chronometer chronometer;
    private TextView tvDistance, tvStatus;
    private Button btnToggle, btnSave, btnHistory;
    private boolean isTracking = false;
    private long pauseTime = 0;
    private double totalDistance = 0;
    private DatabaseReference walksRef;
    private String userId, petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("com.example.mypet");

        setContentView(R.layout.activity_tracker);

        initFirebase();
        initViews();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocation();
        checkPermissions();
    }


    private void initFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = user.getUid();


        DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets");
        petsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    petId = petsRef.push().getKey();
                } else {
                    petId = snapshot.getChildren().iterator().next().getKey();
                }
                walksRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets").child(petId).child("walks");
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    private void initViews() {
        chronometer = findViewById(R.id.chronometer);
        tvDistance = findViewById(R.id.tvDistance);
        tvStatus = findViewById(R.id.tvStatus);
        btnToggle = findViewById(R.id.btnToggle);
        btnSave = findViewById(R.id.btnSave);
        btnHistory = findViewById(R.id.btnHistory);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15.0);

        btnToggle.setOnClickListener(v -> toggleTracking());
        btnSave.setOnClickListener(v -> saveWalk());
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, WalkHistoryActivity.class)));
    }


    private void setupLocation() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (isTracking && locationResult.getLocations().size() > 0) {
                    Location location = locationResult.getLocations().get(0);
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    updateMap(geoPoint);
                }
            }
        };
    }

    private void toggleTracking() {
        if (!isTracking) {
            startTracking();
        } else {
            pauseTracking();
        }
    }

    private void startTracking() {
        isTracking = true;
        pathPoints.clear();
        totalDistance = 0;
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseTime);
        chronometer.start();
        btnToggle.setText("Пауза");
        tvStatus.setText("Идёт запись...");
        tvStatus.setTextColor(Color.GREEN);
        tvDistance.setText("0.0 км");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void pauseTracking() {
        isTracking = false;
        pauseTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        chronometer.stop();
        btnToggle.setText("Продолжить");
        tvStatus.setText("На паузе");
        tvStatus.setTextColor(Color.parseColor("#FF9800"));
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateMap(GeoPoint geoPoint) {
        pathPoints.add(geoPoint);
        mapController.setCenter(geoPoint);
        mapController.setZoom(17.0);


        if (pathOverlay != null) {
            map.getOverlays().remove(pathOverlay);
        }

        pathOverlay = new Polyline();
        pathOverlay.setPoints(pathPoints);
        pathOverlay.setColor(Color.BLUE);
        pathOverlay.setWidth(10f);
        pathOverlay.getPaint().setStrokeCap(Paint.Cap.ROUND);
        map.getOverlays().add(pathOverlay);
        map.invalidate();

        calculateDistance(geoPoint);
    }

    private void calculateDistance(GeoPoint newPoint) {
        if (pathPoints.size() > 1) {
            GeoPoint prevPoint = pathPoints.get(pathPoints.size() - 2);
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    prevPoint.getLatitude(), prevPoint.getLongitude(),
                    newPoint.getLatitude(), newPoint.getLongitude(), results
            );
            totalDistance += results[0];
            tvDistance.setText(String.format(Locale.getDefault(), "%.2f км", totalDistance / 1000));
        }
    }

    private void saveWalk() {
        if (totalDistance < 50) {
            Toast.makeText(this, "Слишком короткая прогулка", Toast.LENGTH_SHORT).show();
            return;
        }

        String walkId = walksRef.push().getKey();
        String time = chronometer.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String date = sdf.format(new Date());
        String distanceStr = String.format(Locale.getDefault(), "%.2f", totalDistance / 1000);

        Walk walk = new Walk(walkId, date, time, distanceStr, pathPoints);
        walksRef.child(walkId).setValue(walk).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Прогулка сохранена!", Toast.LENGTH_LONG).show();
            pathPoints.clear();
            totalDistance = 0;
            pauseTracking();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "GPS разрешения получены", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
