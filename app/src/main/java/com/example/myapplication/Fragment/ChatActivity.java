package com.example.myapplication.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.chatAdapter;
import com.example.myapplication.model.MessageModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private TextView name;
    private ImageView back;
    private EditText message;
    private Button send;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private chatAdapter adapter;
    private List<MessageModel> messageList;
    private DatabaseReference chatRef;
    private String chatId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String receiverId = getIntent().getStringExtra("receiver_id");
        String receiverName = getIntent().getStringExtra("sender_name");
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.message);
        messageList = new ArrayList<>();
        adapter = new chatAdapter(this, messageList, receiverName);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        name = findViewById(R.id.name1);
        name.setText(receiverName);

        chatId = generateChatId(senderId, receiverId);
        chatRef = database.getReference("chats").child(chatId);

        loadMessages();

        send = findViewById(R.id.send);
        send.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                // Gửi message kèm senderId và senderName để dễ quản lý
                MessageModel message = new MessageModel(senderId, senderName, text, true);

                String key = chatRef.push().getKey();
                chatRef.child(key).setValue(message);
                messageInput.setText("");
            }
        });

        back = findViewById(R.id.back_img);
        back.setOnClickListener(v -> {
            finish();
        });
    }
    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    MessageModel msg = child.getValue(MessageModel.class);
                    if (msg != null) {
                        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        msg.setSentByMe(msg.getSender().equals(currentUser));
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }
    private String generateChatId(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }
}