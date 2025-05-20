package com.example.myapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.PersonAdapter;
import com.example.myapplication.model.PersonModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeSwipeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private List<PersonModel> personList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_swipe);

        recyclerView = findViewById(R.id.recyclerView);
        personList = new ArrayList<>();
        adapter = new PersonAdapter(personList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadUsers();
        setupSwipeToDelete();
    }

    private void loadUsers() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("location").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(HomeSwipeActivity.this,
                        "Lỗi tải location: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            List<DocumentSnapshot> locations = task.getResult().getDocuments();

            if (locations.isEmpty()) {
                // Nếu không có dữ liệu
                personList.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(HomeSwipeActivity.this, "Không có dữ liệu location.", Toast.LENGTH_SHORT).show();
                return;
            }

            personList.clear();

            AtomicInteger processedCount = new AtomicInteger(0);
            int totalLocations = locations.size();

            for (DocumentSnapshot locationDoc : locations) {
                String uid = locationDoc.getId();

                if (uid.equals(currentUserId)) {
                    // Bỏ qua user hiện tại
                    if (processedCount.incrementAndGet() == totalLocations) {
                        adapter.notifyDataSetChanged();
                    }
                    continue;
                }

                Object locObj = locationDoc.get("location");
                if (!(locObj instanceof Map)) {
                    // Nếu không có location hoặc kiểu không đúng
                    if (processedCount.incrementAndGet() == totalLocations) {
                        adapter.notifyDataSetChanged();
                    }
                    continue;
                }

                Map<String, Object> loc = (Map<String, Object>) locObj;

                Double lat = toDoubleSafe(loc.get("lat"));
                Double lon = toDoubleSafe(loc.get("lon"));

                if (lat == null || lon == null) {
                    if (processedCount.incrementAndGet() == totalLocations) {
                        adapter.notifyDataSetChanged();
                    }
                    continue;
                }

                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String name = userDoc.getString("first_name");
                        String city = locationDoc.getString("city"); // lấy city từ location doc nếu có
                        personList.add(new PersonModel(uid, name, city, lat, lon));
                    }

                    if (processedCount.incrementAndGet() == totalLocations) {
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> {
                    if (processedCount.incrementAndGet() == totalLocations) {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(HomeSwipeActivity.this,
                    "Lỗi tải location: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    // Hàm chuyển Object thành Double an toàn
    private Double toDoubleSafe(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            final ColorDrawable backgroundLeft = new ColorDrawable(Color.RED);
            final ColorDrawable backgroundRight = new ColorDrawable(Color.GREEN);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // Không hỗ trợ kéo di chuyển
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos >= 0 && pos < personList.size()) {
                    personList.remove(pos);
                    adapter.notifyItemRemoved(pos);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                if (dX > 0) { // vuốt phải
                    backgroundLeft.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                    backgroundLeft.draw(c);
                } else if (dX < 0) { // vuốt trái
                    backgroundRight.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    backgroundRight.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }
}
