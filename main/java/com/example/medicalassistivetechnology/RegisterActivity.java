package com.example.medicalassistivetechnology;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicalassistivetechnology.db.DatabaseHelper;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etRegisterUsername);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Objects.equals(password, confirmPassword)) {
            Toast.makeText(this, R.string.err_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUserExists(username, email)) {
            Toast.makeText(this, R.string.err_user_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.addUser(username, email, password);
        if (success) {
            Toast.makeText(this, R.string.msg_register_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
