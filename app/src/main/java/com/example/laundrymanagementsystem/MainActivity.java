package com.example.laundrymanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * =========================================================================
 * VIVA EXPLANATION: MainActivity (Authentication & Role Selection)
 * =========================================================================
 * 1. UI Components: EditText, RadioGroup, RadioButton, Button, ImageView.
 * 2. SQLite Database: Checks credentials against the 'users' table using DatabaseHelper.
 * 3. Explicit Intent: Navigates to UserDashboardActivity or AdminDashboardActivity
 *    based on the selected role (Intent intent = new Intent(this, Target.class)).
 * 4. Register Navigation: Launches RegisterActivity via Explicit Intent to capture
 *    mandatory SAP ID, phone number, batch, and year.
 * =========================================================================
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private RadioButton rbStudent, rbAdmin;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SQLiteOpenHelper
        dbHelper = new DatabaseHelper(this);

        // Bind standard XML widgets
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        rgRole = findViewById(R.id.rg_role);
        rbStudent = findViewById(R.id.rb_student);
        rbAdmin = findViewById(R.id.rb_admin);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        // Login Button Click Listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Register Button Click Listener: Navigates to RegisterActivity
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Handles authentication and explicit intent navigation based on user role.
     */
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter your username / SAP ID", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        // Determine selected role: User (Student) or Admin
        String selectedRole = rbStudent.isChecked() ? "User" : "Admin";

        // Verify credentials in SQLite database
        boolean isValid = dbHelper.checkUser(username, password, selectedRole);

        if (isValid) {
            Toast.makeText(this, "Login Successful! Welcome, " + username, Toast.LENGTH_SHORT).show();

            // Explicit Intent Navigation
            if (selectedRole.equals("User")) {
                Intent studentIntent = new Intent(MainActivity.this, UserDashboardActivity.class);
                studentIntent.putExtra("USERNAME", username);
                startActivity(studentIntent);
            } else {
                Intent adminIntent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                adminIntent.putExtra("USERNAME", username);
                startActivity(adminIntent);
            }
        } else {
            Toast.makeText(this, "Invalid credentials or role! Please check and try again.", Toast.LENGTH_LONG).show();
        }
    }
}