package com.example.myapplication.login;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.example.myapplication.HomeActivity;
import com.example.myapplication.PersonalDatailsActivity;
import com.example.myapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

/* ------------------- GOOGLE SIGN-IN ------------------- */
public class GoogleSignIn {
    private final Activity activity;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private FirebaseAuth mAuth;

    public GoogleSignIn(Activity activity, ActivityResultLauncher<Intent> launcher) {
        this.activity = activity;
        this.googleSignInLauncher = launcher;
        this.mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(activity, gso);
    }

    // Phương thức gọi khi nhấn nút Đăng nhập Google


// Luôn yêu cầu người dùng chọn lại tài khoản Google
    public void signInWithGoogle() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // Phương thức xử lý kết quả từ Google Sign-In Activity
    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign-In thành công
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if(account != null){
                String idToken = account.getIdToken();
                Log.w("Google Sign-In", "ID Token: " + idToken);
//                .makeText(this, "ID Token: " + idToken, Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(idToken);

            }

        } catch (ApiException e) {
            // Đăng nhập thất bại, hiển thị thông báo lỗi chi tiết hơn
            Log.w("Google Sign-In", "signInResult:failed code=" + e.getStatusCode(), e);


        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Log.d(TAG, "firebaseAuthWithGoogle:success");

                // Kiểm tra người dùng đã có thông tin cá nhân chưa trong Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String uid = user.getUid();

                db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Đã có thông tin => vào HomeActivity
                        Intent intent = new Intent(activity, HomeActivity.class);
                        activity.startActivity(intent);
                    } else {
                        // Chưa có thông tin => vào PersonalDatailsActivity để nhập
                        Intent intent = new Intent(activity, PersonalDatailsActivity.class);
                        activity.startActivity(intent);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(activity, "Không thể kiểm tra người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            } else {
                Log.w("FirebaseAuth", "signInWithCredential:failure", task.getException());
                Toast.makeText(activity, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }}




