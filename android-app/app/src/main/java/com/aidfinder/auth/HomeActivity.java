package com.aidfinder.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aidfinder.MainActivity;
import com.aidfinder.R;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnSearchProducts = findViewById(R.id.btnSearchProducts);
        Button btnLogout = findViewById(R.id.btnLogout);

        username = getIntent().getStringExtra("USERNAME");
        if (username != null && !username.isEmpty()) {
            tvWelcomeMessage.setText(getString(R.string.msg_welcome, username));
        }

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        btnSearchProducts.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
