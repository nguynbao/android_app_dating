package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

public class Photo extends AppCompatActivity {
    private int currentImageIndex = -1;
    private int[] imageViewIds = {
            R.id.image1, R.id.image2, R.id.image3,
            R.id.image4, R.id.image5, R.id.image6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo);
        for (int i = 0; i < imageViewIds.length; i++) {
            final int index = i;
            ImageView imageView = findViewById(imageViewIds[i]);
            imageView.setOnClickListener(v -> {
                currentImageIndex = index;
                openImagePicker();
            });
        }
        ImageView backImg = findViewById(R.id.back_img);
        backImg.setOnClickListener(v -> {
            finish();
        });
        AppCompatButton btncontinue = findViewById(R.id.ctn);
        btncontinue.setOnClickListener(v -> {
            Intent intent = new Intent(Photo.this,  LocationActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && currentImageIndex != -1) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                ImageView imageView = findViewById(imageViewIds[currentImageIndex]);
                Glide.with(Photo.this)
                        .load(imageUri)
                        .transform(new RoundedCorners(dpToPx(70)))
                        .into(imageView);
            }
        }
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // Chọn 1 ảnh mỗi lần
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), 100);
    }

}