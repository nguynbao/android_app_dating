package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MessageAdapter;
import com.example.myapplication.model.Message;

import com.example.myapplication.model.MessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<MessageModel> messageList;

    public MessagesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewMess);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(getContext(), messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d("MessagesFragment", "onCreateView:");
        recyclerView.setAdapter(adapter);
        simulateIncomingMessages();
        return view;
    }
    private void simulateIncomingMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";

        CollectionReference usersRef = db.collection("users");

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Handler handler = new Handler();
                int[] delay = {0}; // dùng mảng để giữ giá trị bên trong lambda

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String userId = document.getId();

                    // Bỏ qua chính mình
                    if (!userId.equals(currentUserId)) {
                        String chatId = generateChatId(currentUserId, userId);
                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
                        chatRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                                    MessageModel msg = msgSnap.getValue(MessageModel.class);
                                    if (msg != null && !msg.getSender().equals(currentUserId)) {
                                        String content = msg.getText();  // đây là tin nhắn mới nhất từ đối phương
                                        messageList.add(new MessageModel(userId, document.getString("first_name"), content,false)); // class Message bạn đang dùng
                                        adapter.notifyItemInserted(messageList.size() - 1);
                                        recyclerView.scrollToPosition(messageList.size() - 1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Xử lý lỗi nếu cần
                            }

                        });
                    }
                }
            }
        });
    }
    private String generateChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}