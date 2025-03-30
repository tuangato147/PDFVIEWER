package com.example.thunghiem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText edtUsername, edtPassword;
    private ImageView imgViewLock;
    private Switch switchRemember;
    private Button btnLogin;
    private TextView txtForgotPassword;
    private SharedPreferences sharedPreferences;
    private boolean isPasswordVisible = false;
    private boolean isFirstLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkFirstLogin();
        loadSavedCredentials();
        setupListeners();
    }

    private void initViews() {
        edtUsername = findViewById(R.id.editTextText);
        edtPassword = findViewById(R.id.editTextText2);
        imgViewLock = findViewById(R.id.imageView2);
        switchRemember = findViewById(R.id.switch1);
        btnLogin = findViewById(R.id.button2);
        txtForgotPassword = findViewById(R.id.textView4);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
    }

    private void checkFirstLogin() {
        isFirstLogin = !sharedPreferences.contains("registeredUsername");
        if (isFirstLogin) {
            btnLogin.setText("Đăng ký");
            Toast.makeText(this, "Hãy tạo tài khoản cho lần đăng nhập đầu tiên",
                    Toast.LENGTH_LONG).show();
        } else {
            btnLogin.setText("Đăng nhập");
        }
    }

    private void loadSavedCredentials() {
        if (!isFirstLogin && sharedPreferences.getBoolean("rememberMe", false)) {
            String savedUsername = sharedPreferences.getString("savedUsername", "");
            String savedPassword = sharedPreferences.getString("savedPassword", "");
            edtUsername.setText(savedUsername);
            edtPassword.setText(savedPassword);
            switchRemember.setChecked(true);
        }
    }

    private void setupListeners() {
        imgViewLock.setOnClickListener(v -> togglePasswordVisibility());

        txtForgotPassword.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc muốn xóa tài khoản hiện tại và đăng ký lại?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> resetAccount())
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (isFirstLogin) {
                handleFirstLogin(username, password);
            } else {
                handleLogin(username, password);
            }
        });
    }

    private void resetAccount() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        isFirstLogin = true;
        btnLogin.setText("Đăng ký");
        edtUsername.setText("");
        edtPassword.setText("");
        switchRemember.setChecked(false);

        Toast.makeText(this, "Đã xóa tài khoản. Vui lòng đăng ký tài khoản mới",
                Toast.LENGTH_LONG).show();
    }

    private void handleFirstLogin(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("registeredUsername", username);
        editor.putString("registeredPassword", password);
        editor.apply();

        Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập lại",
                Toast.LENGTH_SHORT).show();

        isFirstLogin = false;
        btnLogin.setText("Đăng nhập");
        edtUsername.setText("");
        edtPassword.setText("");
        switchRemember.setChecked(false);
    }

    private void handleLogin(String username, String password) {
        String registeredUsername = sharedPreferences.getString("registeredUsername", "");
        String registeredPassword = sharedPreferences.getString("registeredPassword", "");

        if (username.equals(registeredUsername) && password.equals(registeredPassword)) {
            if (switchRemember.isChecked()) {
                saveCredentials(username, password);
            } else {
                clearSavedCredentials();
            }
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, MainPDFActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imgViewLock.setImageResource(R.drawable.lock);
        } else {
            edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imgViewLock.setImageResource(R.drawable.unlock);
        }
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setSelection(edtPassword.getText().length());
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedUsername", username);
        editor.putString("savedPassword", password);
        editor.putBoolean("rememberMe", true);
        editor.apply();
    }

    private void clearSavedCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("savedUsername");
        editor.remove("savedPassword");
        editor.remove("rememberMe");
        editor.apply();
    }
}