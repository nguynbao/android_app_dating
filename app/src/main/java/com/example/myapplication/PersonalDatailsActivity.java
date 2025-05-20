package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.AdapterInterests;
import com.example.myapplication.textstyle.TextGradientUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalDatailsActivity extends AppCompatActivity {
//    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    List<String> Intesrest_array = Arrays.asList("Photography", "Video Games", "Travelling", "Speeches", "Swimming", "Ex Sports ", "Cooking", "Music", "Shopping", "Art & Crafts","Drinking","Fitness");
    String[] gender_array = { "Select Gender","Male", "Female", "Other"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ImageView back_img = findViewById(R.id.back_img);
        back_img.setOnClickListener(v -> {
            finish();
        });
        ImageView camera = findViewById(R.id.camera);
        camera.setOnClickListener(v -> {
            openGallery();
        });


        EditText FName = findViewById(R.id.FName);
        EditText LName = findViewById(R.id.LName);
        EditText DoB = findViewById(R.id.DoB);
        DoB.setOnClickListener(v -> {
                    showDatePicker();
                });
        Spinner Gender = findViewById(R.id.Gender);
        ArrayAdapter<String> GenderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gender_array);
        GenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Gender.setAdapter(GenderAdapter);
        androidx.appcompat.widget.AppCompatButton ctn = findViewById(R.id.ctn);
        ctn.setOnClickListener(v -> {
                saveProfileDetails();
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            try {
                InputStream input = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundedDrawable.setCircular(true); // Bo tròn hoàn toàn

                ImageView imageView = findViewById(R.id.profile_pic);
                imageView.setImageDrawable(roundedDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openGallery() {
        // Handle camera button click
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    private  void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int month =  calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(

                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    EditText DoB = findViewById(R.id.DoB);
                    DoB.setText(date);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void saveProfileDetails() {
        EditText FName = findViewById(R.id.FName);
        EditText LName = findViewById(R.id.LName);
        EditText DoB = findViewById(R.id.DoB);
        Spinner Gender = findViewById(R.id.Gender);

        // Kiểm tra đầu vào
        if (FName.getText().toString().isEmpty() ||
                LName.getText().toString().isEmpty() ||
                DoB.getText().toString().isEmpty() ||
                Gender.getSelectedItemPosition() == 0) {

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu dữ liệu vào Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("first_name", FName.getText().toString());
        userInfo.put("last_name", LName.getText().toString());
        userInfo.put("date_of_birth", DoB.getText().toString());
        userInfo.put("gender", Gender.getSelectedItem().toString());

        db.collection("users").document(userId).set(userInfo).addOnSuccessListener(unused -> {
                    Log.d("Firestore", "User info saved");

                    // Sau khi lưu thành công thì chuyển layout
                    loadInterestSelectionScreen();
                }).addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving user info", e);
                    Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
                });
    }
    private void loadInterestSelectionScreen() {
        // Chuyển layout
        setContentView(R.layout.activity_interests);

        // Thiết lập RecyclerView
        RecyclerView recyclerView = findViewById(R.id.like_interest_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        AdapterInterests adapter = new AdapterInterests(this, Intesrest_array);
        recyclerView.setAdapter(adapter);

        // Thiết lập gradient cho text
        TextView textView = findViewById(R.id.Textgradient);
        TextGradientUtil.applyGradient(textView);

        // Nút back
        ImageView back_img = findViewById(R.id.back_img);
        back_img.setOnClickListener(v -> finish());

        // Bạn có thể thêm nút lưu sở thích ở đây nếu cần
        androidx.appcompat.widget.AppCompatButton save_btn = findViewById(R.id.ctn);
        save_btn.setOnClickListener(v -> {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            String userID = auth.getCurrentUser().getUid();
            // Lấy danh sách sở thích được chọn
            List<String> selectedItems = adapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Bạn chưa chọn sở thích nào!", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("userID", userID);
            data.put("interests", selectedItems);

            db.collection("user_interests")
                    .document(userID)
                    .set(data)  // sử dụng set để ghi đè nếu có
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã lưu sở thích!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Photo.class));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        });
        TextView skip = findViewById(R.id.skip);
        skip.setOnClickListener(v -> {
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                Map<String, Object> data = new HashMap<>();
            data.put("userID", userID);
            data.put("interests", new ArrayList<>());
            db.collection("user_interests")
                    .document(userID)
                    .set(data)  // sử dụng set để ghi đè nếu có
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã lưu sở thích!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Photo.class));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        });

    }


}