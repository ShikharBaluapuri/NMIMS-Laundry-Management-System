package com.example.laundrymanagementsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * =========================================================================
 * VIVA EXPLANATION: RegisterActivity (Student Registration)
 * =========================================================================
 * Collects mandatory NMIMS student information:
 * - SAP ID (e.g. 11 digits)
 * - Phone Number (10 digits)
 * - Batch (Spinner with NMIMS engineering/pharma programs)
 * - Academic Year (1st to 4th year)
 * - Username & Password
 *
 * Saves data into the 'users' table in SQLite and pre-credits 30 clothes balance.
 * =========================================================================
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etSapId, etPhone, etUsername, etPassword;
    private Spinner spBatch, spYear;
    private Button btnDoRegister, btnBackLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Student Registration");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        etSapId = findViewById(R.id.et_reg_sap_id);
        etPhone = findViewById(R.id.et_reg_phone);
        spBatch = findViewById(R.id.sp_reg_batch);
        spYear = findViewById(R.id.sp_reg_year);
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        btnDoRegister = findViewById(R.id.btn_do_register);
        btnBackLogin = findViewById(R.id.btn_back_login);
    }

    private void setupSpinners() {
        // NMIMS Shirpur campus batches & courses
        String[] batches = {
                "B.Tech - Computer Engineering",
                "B.Tech - Information Technology",
                "B.Tech - AI & Data Science",
                "B.Tech - Electronics & Telecom",
                "MBA Tech - Computer / IT",
                "B.Pharm / M.Pharm",
                "Diploma in Engineering"
        };
        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                batches
        );
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBatch.setAdapter(batchAdapter);

        // Academic Years
        String[] years = {
                "1st Year (Freshman)",
                "2nd Year (Sophomore)",
                "3rd Year (Junior)",
                "4th Year (Senior)"
        };
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);
    }

    private void setupListeners() {
        btnDoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        btnBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to Login
            }
        });
    }

    private void performRegistration() {
        String sapId = etSapId.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String batch = spBatch.getSelectedItem().toString();
        String year = spYear.getSelectedItem().toString();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Mandatory Validations
        if (sapId.isEmpty()) {
            Toast.makeText(this, "SAP ID is mandatory!", Toast.LENGTH_SHORT).show();
            etSapId.requestFocus();
            return;
        }

        if (sapId.length() < 6) {
            Toast.makeText(this, "Please enter a valid SAP ID (at least 6 digits)", Toast.LENGTH_SHORT).show();
            etSapId.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone number is mandatory!", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Username is mandatory!", Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is mandatory!", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        // 2. Uniqueness Checks
        if (dbHelper.checkUsernameExists(username)) {
            Toast.makeText(this, "Username already exists! Please choose another.", Toast.LENGTH_LONG).show();
            etUsername.requestFocus();
            return;
        }

        if (dbHelper.checkSapIdExists(sapId)) {
            Toast.makeText(this, "A student with this SAP ID is already registered!", Toast.LENGTH_LONG).show();
            etSapId.requestFocus();
            return;
        }

        // 3. Save into SQLite database
        boolean success = dbHelper.registerStudent(username, password, sapId, phone, batch, year);
        if (success) {
            Toast.makeText(this, "Registration Successful! 30 clothes quota activated. Please login.", Toast.LENGTH_LONG).show();
            finish(); // Return to MainActivity so student can login
        } else {
            Toast.makeText(this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
