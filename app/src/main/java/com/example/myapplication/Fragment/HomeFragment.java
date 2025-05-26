package com.example.myapplication.Fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.PersonAdapter;
import com.example.myapplication.model.PersonModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private ImageView dislike, love, like;
    private RecyclerView recyclerView;
    private PersonAdapter adapter;
    private List<PersonModel> personList;
    private ItemTouchHelper itemTouchHelper;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        like = view.findViewById(R.id.like);
        love = view.findViewById(R.id.love);
        dislike = view.findViewById(R.id.dislike);
        recyclerView = view.findViewById(R.id.recyclerView);
        personList = new ArrayList<>();
        adapter = new PersonAdapter(personList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()){
                @Override
        public boolean canScrollVertically() {
            return false;
        }
        });
        recyclerView.setAdapter(adapter);
        setupSwipeToDelete();
        dislike.setOnClickListener(v ->{
            Toast.makeText(getContext(), "Dislike", Toast.LENGTH_SHORT).show();
            dislike();
        });
        love.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Love", Toast.LENGTH_SHORT).show();
            love();
        });
        like.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Like", Toast.LENGTH_SHORT).show();
            like();
        });
        loadUsers();
        return view;
    }
    private void loadUsers() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("location").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(),
                        "Lỗi tải location: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            List<DocumentSnapshot> locations = task.getResult().getDocuments();
            if (locations.isEmpty()) {
                personList.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Không có dữ liệu location.", Toast.LENGTH_SHORT).show();
                return;
            }

            personList.clear();
            List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();

            for (DocumentSnapshot locationDoc : locations) {
                String uid = locationDoc.getId();
                if (uid.equals(currentUserId)) continue;

                Object locObj = locationDoc.get("location");
                if (!(locObj instanceof Map)) continue;

                Map<String, Object> loc = (Map<String, Object>) locObj;
                Double lat = toDoubleSafe(loc.get("lat"));
                Double lon = toDoubleSafe(loc.get("lon"));
                if (lat == null || lon == null) continue;

                Task<DocumentSnapshot> userTask = db.collection("users").document(uid).get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                String name = userDoc.getString("first_name");
                                String city = locationDoc.getString("city");
                                personList.add(new PersonModel(uid, name, city, lat, lon));
                            }
                        });
                userTasks.add(userTask);
            }

            Tasks.whenAllSuccess(userTasks).addOnSuccessListener(result -> {
                adapter.notifyDataSetChanged();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi tải user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }
    private Double toDoubleSafe(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private void love() {
        if (!personList.isEmpty()) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            if (viewHolder == null) {
                recyclerView.postDelayed(this::love, 100);
                return;
            }

            View itemView = viewHolder.itemView;
            int height = recyclerView.getHeight();

            itemView.animate()
                    .translationY(-height / 2f) // Lướt lên
                    .scaleX(3.5f)               // To dần
                    .scaleY(3.5f)
                    .alpha(0f)                  // Mờ dần
                    .setDuration(400)
                    .withEndAction(() -> {
                        personList.remove(0);
                        adapter.notifyItemRemoved(0);
                        // Reset trạng thái View để tái sử dụng
                        itemView.setTranslationY(0);
                        itemView.setScaleX(1f);
                        itemView.setScaleY(1f);
                        itemView.setAlpha(1f);
                    })
                    .start();
        }
    }
    private void dislike() {
        if (!personList.isEmpty()) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            if (viewHolder == null) {
                // Nếu viewHolder chưa tạo xong, thử delay 100ms rồi gọi lại
                recyclerView.postDelayed(this::dislike, 100);
                return;
            }
            View itemView = viewHolder.itemView;
            int width = recyclerView.getWidth();
            itemView.animate()
                    .translationX(-width)  // Dịch sang trái bằng chiều rộng recyclerView
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(()->{
                        // Sau khi animation kết thúc, remove item
                        personList.remove(0);
                        adapter.notifyItemRemoved(0);
                        // Reset lại vị trí itemView để tái sử dụng
                        itemView.setTranslationX(0);
                    }).start();
        }
    }
    private void like() {
        if (!personList.isEmpty()) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
            if (viewHolder == null) {
                // Nếu viewHolder chưa tạo xong, thử delay 100ms rồi gọi lại
                recyclerView.postDelayed(this::like, 100);
                return;
            }

            View itemView = viewHolder.itemView;
            int width = recyclerView.getWidth();

            // Vuốt animation sang phải, mất 300ms
            itemView.animate()
                    .translationX(width)  // Dịch sang phải bằng chiều rộng recyclerView
                    .alpha(0f)           // Giảm mờ dần cho đẹp
                    .setDuration(400)
                    .withEndAction(() -> {
                        // Sau khi animation kết thúc, remove item
                        personList.remove(0);
                        adapter.notifyItemRemoved(0);
                        // Reset lại vị trí itemView để tái sử dụng
                        itemView.setTranslationX(0);
//                        itemView.setAlpha(1f);
                    })
                    .start();
        } else {
            Toast.makeText(getContext(), "Không còn người nào để like", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            final ColorDrawable backgroundLeft = new ColorDrawable(Color.GREEN);
            final ColorDrawable backgroundRight = new ColorDrawable(Color.RED);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
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

                if (dX > 0) {
                    backgroundLeft.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                    backgroundLeft.draw(c);
                } else if (dX < 0) {
                    backgroundRight.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    backgroundRight.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}

