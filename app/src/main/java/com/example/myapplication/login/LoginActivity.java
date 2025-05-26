package com.example.myapplication.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.PersonalDatailsActivity;
import com.example.myapplication.R;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import com.facebook.FacebookSdk;
import com.facebook.CallbackManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.google.firebase.auth.FirebaseUser;


import java.util.Arrays;



public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignIn googleSignIn;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ImageView imageViewfb  ;
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            googleSignIn.handleSignInResult(data);
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        FacebookSdk.setApplicationId("facebook_app_id");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        // Nếu người dùng đã đăng nhập, chuyển sang màn PersonalDatailsActivity luôn
//        if (mAuth.getCurrentUser() != null) {
//            startActivity(new Intent(LoginActivity.this, HomeSwipeActivity.class));
//            finish();
//            return;
//        }


        // Khởi tạo CallbackManager để xử lý kết quả đăng nhập
        callbackManager = CallbackManager.Factory.create();

        // Tìm nút đăng nhập Facebook
        loginButton = findViewById(R.id.fb_login_button);
        imageViewfb = findViewById(R.id.fb);
        imageViewfb.setOnClickListener(v -> loginButton.performClick());

        // Đặt quyền cần thiết để Facebook login

        loginButton.setPermissions(Arrays.asList("email", "public_profile"));

        // Đăng ký Facebook callback để nhận kết quả login
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Đăng nhập thành công
                AccessToken accessToken = loginResult.getAccessToken();
                handleFacebookAccessToken(accessToken);

            }

            @Override
            public void onCancel() {
                // Người dùng hủy bỏ đăng nhập
                Log.d("Facebook", "Login canceled.");
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                // Có lỗi khi đăng nhập
                Log.d("Facebook", "Login failed: " + error.getMessage());
            }
        });

        googleSignIn = new GoogleSignIn(LoginActivity.this, googleSignInLauncher);
        ImageView imageView = findViewById(R.id.google);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn.signInWithGoogle(); // Gọi phương thức signInWithGoogle
            }
            });
        }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("Facebook", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseAuth", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Lấy thông tin Profile Facebook, có thể null
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            Log.d("Facebook", "User ID: " + profile.getId());
                            Log.d("Facebook", "User Name: " + profile.getName());
                        } else {
                            Log.d("Facebook", "Profile is null.");
                        }

                        // Chuyển sang màn hình chi tiết cá nhân
                        Intent intent = new Intent(LoginActivity.this, PersonalDatailsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w("FirebaseAuth", "signInWithCredential:failure", task.getException());
                    }
                });
    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
