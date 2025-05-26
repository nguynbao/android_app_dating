package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.adapters.CityDropdownAdapter;
import com.example.myapplication.model.City;
import com.example.myapplication.network.ProvinceApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LocationActivity extends AppCompatActivity {

    private FirebaseFirestore database;
    private AppCompatButton btncontinue;
    private TextView getLocation;
    private AutoCompleteTextView autoCompleteCity;
    private final List<String> cities = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    String selected;
    double lat = 0.0, lon = 0.0;

    FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);
        autoCompleteCity = findViewById(R.id.getCity);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://provinces.open-api.vn/").addConverterFactory(GsonConverterFactory.create()).build();

        ProvinceApi api = retrofit.create(ProvinceApi.class);
        api.getProvinces().enqueue(new Callback<List<City>>() {
            @Override
            public void onResponse(@NonNull Call<List<City>> call, @NonNull Response<List<City>> response) {
                if (response.isSuccessful()){
                    assert response.body() != null;
                    for (City city : response.body()){
                        cities.add(city.getName());
                    }
                    CityDropdownAdapter adapter = new CityDropdownAdapter(LocationActivity.this, cities);
                    autoCompleteCity.setAdapter(adapter);
                    autoCompleteCity.setThreshold(1);
                    autoCompleteCity.setDropDownVerticalOffset(15);
                    autoCompleteCity.setOnItemClickListener((parent, view, position, id) -> {
                        selected = adapter.getItem(position);
                        Toast.makeText(LocationActivity.this, "Bạn chọn: " + selected, Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<City>> call, @NonNull Throwable t) {
                Toast.makeText(LocationActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();

            }
        });

        getLocation = findViewById(R.id.getLocation);
        getLocation.setOnClickListener(v -> {
            Log.d("Tag", "đã ấn");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            setGetLocation();

        });
        btncontinue = findViewById(R.id.ctn);
        btncontinue.setOnClickListener(v -> {
            if (selected == null || selected.isEmpty() || lat == 0.0 || lon == 0.0) {
                Toast.makeText(LocationActivity.this, "Vui lòng chọn thành phố và vị trí", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(LocationActivity.this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("city", selected);
            userInfo.put("location", new HashMap<String, Object>() {{
                put("lat", lat);
                put("lon", lon);
            }});

            FirebaseFirestore database = FirebaseFirestore.getInstance();  // ✅ Dùng Firestore
            database.collection("location").document(uid)
                    .set(userInfo)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(LocationActivity.this, "Đã lưu thành phố và vị trí", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LocationActivity.this, HomeActivity.class));
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(LocationActivity.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setGetLocation(); // Gọi lại khi có quyền
        }
    }

    private void setGetLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d("tag", "đang ở đây");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Vui lòng cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            onRequestPermissionsResult(1, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new int[]{PackageManager.PERMISSION_GRANTED});

        }
        else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                     lat = location.getLatitude();
                     lon = location.getLongitude();
                    Log.d("LOCATION", "Last location - Lat: " + lat + ", Lon: " + lon);
                    getLocation.setText("Đã lấy được vị trí của bạn");
                    Toast.makeText(LocationActivity.this, "Lat: " + lat + ", Lon: " + lon, Toast.LENGTH_SHORT).show();
                } else {
                    LocationRequest locationRequest = new LocationRequest.Builder(1000L)
                            .setMinUpdateIntervalMillis(500L)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .build();


                    fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            fusedLocationClient.removeLocationUpdates(this); // chỉ cần 1 lần
                            Location loc = locationResult.getLastLocation();
                            if (loc != null) {
                                lat = loc.getLatitude();
                                lon = loc.getLongitude();
                                Log.d("LOCATION", "New location - Lat: " + lat + ", Lon: " + lon);
                                getLocation.setText("");
                                Toast.makeText(LocationActivity.this, "Lat: " + lat + ", Lon: " + lon, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LocationActivity.this, "Không lấy được vị trí", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                            getMainLooper());
            }});
}}}