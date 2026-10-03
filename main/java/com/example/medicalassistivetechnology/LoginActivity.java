package com.example.medicalassistivetechnology;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicalassistivetechnology.db.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginInput, etPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        etLoginInput = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String loginInput = etLoginInput.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(loginInput) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValid = databaseHelper.checkUser(loginInput, password);
        if (isValid) {
            String username = databaseHelper.getUsernameByLoginInput(loginInput);
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.err_invalid_credentials, Toast.LENGTH_SHORT).show();
        }
    }
}
