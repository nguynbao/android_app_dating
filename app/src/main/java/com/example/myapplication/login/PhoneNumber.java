package com.example.myapplication.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.PersonalDatailsActivity;
import com.example.myapplication.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneNumber extends AppCompatActivity {
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth mAuth;
    private String verificationId;
    private Button btnSubmit;

    private Button   btnVerify;
    private EditText etPhone;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    public void setUpLoginViews() {
        etPhone   = findViewById(R.id.etPhone);
        btnSubmit = findViewById(R.id.button);

        // Ban đầu disable nút
        btnSubmit.setEnabled(false);
        btnSubmit.setAlpha(0.5f);

        // Bật nút khi nhập đủ 10 ký tự
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            @Override public void afterTextChanged(Editable s)     {}
            @Override public void onTextChanged(CharSequence s,int st,int bf,int c) {
                boolean ok = s.toString().trim().length() == 10;
                btnSubmit.setEnabled(ok);
                btnSubmit.setAlpha(ok ? 1f : 0.5f);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!phone.startsWith("+")) phone = "+84" + phone.substring(1); // định dạng +84
            showVerifyLayout();           // chuyển layout
            sendVerificationCode(phone);  // gửi OTP tới số đó
        });
    }

    /* ------------------- BƯỚC 2: HIỂN THỊ LAYOUT XÁC MINH OTP ------------------- */
    private void showVerifyLayout() {
        setContentView(R.layout.activity_verify_code);  // nạp layout khác
        TextView tvRc = findViewById(R.id.textrc);
        tvRc.post(() -> {
            tvRc.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            float width = tvRc.getPaint().measureText(tvRc.getText().toString());
            if (width == 0) {
                width = tvRc.getWidth(); // fallback nếu chưa đo được
            }
            Shader textShader = new LinearGradient(
                    0, 0, width, tvRc.getTextSize(),
                    new int[]{
                            Color.parseColor("#FA457E"),
                            Color.parseColor("#7B49FF")
                    },
                    null, Shader.TileMode.CLAMP);

            tvRc.getPaint().setShader(textShader);
            tvRc.invalidate();
        });

        etOtp1   = findViewById(R.id.etOtp1);
        etOtp2   = findViewById(R.id.etOtp2);
        etOtp3   = findViewById(R.id.etOtp3);
        etOtp4   = findViewById(R.id.etOtp4);
        etOtp5   = findViewById(R.id.etOtp5);
        etOtp6   = findViewById(R.id.etOtp6);
        btnVerify = findViewById(R.id.button);

        // tự chuyển focus sang ô tiếp theo
        setUpOtpAutoMove(etOtp1, etOtp2);
        setUpOtpAutoMove(etOtp2, etOtp3);
        setUpOtpAutoMove(etOtp3, etOtp4);
        setUpOtpAutoMove(etOtp4, etOtp5);
        setUpOtpAutoMove(etOtp5, etOtp6);

        btnVerify.setOnClickListener(v -> {
            String code = etOtp1.getText().toString().trim()
                    + etOtp2.getText().toString().trim()
                    + etOtp3.getText().toString().trim()
                    + etOtp4.getText().toString().trim()
                    + etOtp5.getText().toString().trim()
                    + etOtp6.getText().toString().trim();

            if (code.length() == 6 && verificationId != null) {
                verifyCode(code);
            } else {
                Toast.makeText(this,"Please enter all 6 digits",Toast.LENGTH_SHORT).show();
            }
        });
    }

    // helper: tự động nhảy sang ô kế tiếp sau khi nhập 1 ký tự
    private void setUpOtpAutoMove(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s,int st,int bf,int c){
                if (s.length()==1) {
                    // Kiểm tra nếu ô kế tiếp không phải là null trước khi request focus
                    if (next != null) {
                        next.requestFocus();
                    }
                }
            }
        });
    }

    /* ------------------- FIREBASE: GỬI OTP ------------------- */
    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /* ------------------- FIREBASE: CALLBACKS ------------------- */
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Tự động nhận OTP (trường hợp Google Play Services tự đọc SMS)
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // Xử lý lỗi xác minh, ví dụ: số điện thoại không hợp lệ
                    Log.w(TAG, "onVerificationFailed", e);
                    Toast.makeText(PhoneNumber.this,
                            "Xác minh số điện thoại thất bại: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verifId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // Mã xác minh đã được gửi đến số điện thoại của người dùng
                    verificationId = verifId;
                    resendToken    = token;
                    Log.d(TAG, "onCodeSent:" + verifId);
                    Toast.makeText(PhoneNumber.this,
                            "Mã OTP đã được gửi. Vui lòng kiểm tra tin nhắn SMS.",
                            Toast.LENGTH_SHORT).show();


                }
            };

    /* ------------------- XÁC THỰC OTP ------------------- */
    private void verifyCode(String code) {
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithCredential:success");
                        Toast.makeText(this,"Đăng nhập thành công!",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, PersonalDatailsActivity.class));
                        finish(); // Đóng Activity Login
                    } else {
                        // Đăng nhập thất bại
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this,"Mã OTP không đúng!",Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
