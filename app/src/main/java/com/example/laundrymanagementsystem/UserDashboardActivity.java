package com.example.laundrymanagementsystem;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * =========================================================================
 * VIVA EXPLANATION: UserDashboardActivity (Student Module)
 * =========================================================================
 * 1. Available Clothes Balance: Real-time balance displayed prominently in header
 *    and inside the booking form.
 * 2. Dynamic Balance Calculation (TextWatcher): As the user types the number of
 *    clothes, the UI calculates and previews the updated remaining balance live.
 * 3. Real-Time SQLite Balance Updates:
 *    - Deducts clothes immediately on booking submission.
 *    - Refunds clothes immediately on student booking cancellation or admin rejection.
 * 4. Daily Slot Limit: Max 3 bookings per day enforced via SQL COUNT check.
 * 5. SimpleAdapter with ListView: Populates student bookings and assigned washer staff.
 * =========================================================================
 */
public class UserDashboardActivity extends AppCompatActivity {

    private String loggedInUser;
    private DatabaseHelper dbHelper;

    // Header Widgets
    private TextView tvUserWelcome, tvStudentHeaderDetails, tvLiveBalance, tvTodaySlots, btnResetBalance;

    // Tab buttons & containers
    private Button btnTabBook, btnTabMyBookings;
    private ScrollView scrollBookingForm;
    private LinearLayout llMyBookings;

    // Booking Form Widgets
    private TextView tvFormAvailableBalance, tvFormBalancePreview;
    private EditText etClothesCount;
    private TextView tvDropDate, tvPickupDate;
    private Button btnSelectDropDate, btnChangePickupDate, btnSubmitBooking;
    private Spinner spDropTime;
    private CheckBox cbFragile, cbFold, cbIron;

    // History ListView Widgets
    private ListView lvUserBookings;
    private TextView tvEmptyUserBookings;
    private ArrayList<HashMap<String, String>> userBookingsList;
    private SimpleAdapter bookingsAdapter;

    // Date trackers
    private Calendar dropCalendar, pickupCalendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        loggedInUser = getIntent().getStringExtra("USERNAME");
        if (loggedInUser == null || loggedInUser.isEmpty()) {
            loggedInUser = "student1";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Student Laundry Portal");
            getSupportActionBar().setSubtitle("NMIMS Shirpur Campus");
        }

        dbHelper = new DatabaseHelper(this);
        dbHelper.ensureSampleData();

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dropCalendar = Calendar.getInstance();
        pickupCalendar = Calendar.getInstance();
        pickupCalendar.add(Calendar.DAY_OF_MONTH, 2);

        initViews();
        setupTimeSpinner();
        setupListeners();
        setupLiveBalanceCalculation();

        refreshLiveBalance();
        loadBookingsList();
    }

    private void initViews() {
        tvUserWelcome = findViewById(R.id.tv_user_welcome);
        tvStudentHeaderDetails = findViewById(R.id.tv_student_header_details);
        tvLiveBalance = findViewById(R.id.tv_live_balance);
        tvTodaySlots = findViewById(R.id.tv_today_slots);
        btnResetBalance = findViewById(R.id.btn_reset_balance);

        btnTabBook = findViewById(R.id.btn_tab_book);
        btnTabMyBookings = findViewById(R.id.btn_tab_my_bookings);
        scrollBookingForm = findViewById(R.id.scroll_booking_form);
        llMyBookings = findViewById(R.id.ll_my_bookings);

        tvFormAvailableBalance = findViewById(R.id.tv_form_available_balance);
        tvFormBalancePreview = findViewById(R.id.tv_form_balance_preview);
        etClothesCount = findViewById(R.id.et_clothes_count);
        tvDropDate = findViewById(R.id.tv_drop_date);
        tvPickupDate = findViewById(R.id.tv_pickup_date);
        btnSelectDropDate = findViewById(R.id.btn_select_drop_date);
        btnChangePickupDate = findViewById(R.id.btn_change_pickup_date);
        spDropTime = findViewById(R.id.sp_drop_time);
        cbFragile = findViewById(R.id.cb_fragile);
        cbFold = findViewById(R.id.cb_fold);
        cbIron = findViewById(R.id.cb_iron);
        btnSubmitBooking = findViewById(R.id.btn_submit_booking);

        lvUserBookings = findViewById(R.id.lv_user_bookings);
        tvEmptyUserBookings = findViewById(R.id.tv_empty_user_bookings);

        tvUserWelcome.setText("Welcome, " + loggedInUser + "!");
        HashMap<String, String> userProfile = dbHelper.getUserDetails(loggedInUser);
        if (userProfile != null && !userProfile.isEmpty()) {
            String sap = userProfile.get("sap_id");
            String batch = userProfile.get("batch");
            String year = userProfile.get("year");
            tvStudentHeaderDetails.setText("SAP: " + (sap == null ? "N/A" : sap) + " | " + (batch == null ? "" : batch) + " (" + (year == null ? "" : year) + ")");
        }

        tvDropDate.setText(dateFormatter.format(dropCalendar.getTime()));
        tvPickupDate.setText(dateFormatter.format(pickupCalendar.getTime()));
    }

    private void setupTimeSpinner() {
        String[] timeSlots = {
                "08:00 AM - 10:00 AM",
                "10:00 AM - 12:00 PM",
                "02:00 PM - 04:00 PM",
                "04:00 PM - 06:00 PM",
                "06:00 PM - 08:00 PM"
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                timeSlots
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDropTime.setAdapter(spinnerAdapter);
    }

    private void setupListeners() {
        btnTabBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(true);
            }
        });

        btnTabMyBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(false);
            }
        });

        btnSelectDropDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDatePicker();
            }
        });

        btnChangePickupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickupDatePicker();
            }
        });

        btnSubmitBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processBookingSubmission();
            }
        });

        // Reset Quota button (convenient for Viva demonstrations)
        btnResetBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.updateUserBalance(loggedInUser, 30);
                refreshLiveBalance();
                Toast.makeText(UserDashboardActivity.this, "Monthly quota reset to 30 clothes!", Toast.LENGTH_SHORT).show();
            }
        });

        lvUserBookings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (userBookingsList != null && position < userBookingsList.size()) {
                    HashMap<String, String> selectedItem = userBookingsList.get(position);
                    handleBookingItemClick(selectedItem);
                }
            }
        });
    }

    /**
     * DYNAMIC BALANCE CALCULATION:
     * As the user enters clothes count, automatically calculates and previews
     * the updated balance in real time!
     */
    private void setupLiveBalanceCalculation() {
        etClothesCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBalancePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateBalancePreview() {
        int currentBalance = dbHelper.getUserBalance(loggedInUser);
        String input = etClothesCount.getText().toString().trim();

        if (input.isEmpty()) {
            tvFormBalancePreview.setText("Enter number of clothes (1 - 10) to preview updated balance.");
            tvFormBalancePreview.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            return;
        }

        try {
            int clothes = Integer.parseInt(input);

            if (clothes <= 0) {
                tvFormBalancePreview.setText("⚠️ Count must be at least 1 cloth.");
                tvFormBalancePreview.setTextColor(Color.parseColor("#DC2626"));
            } else if (clothes > 10) {
                tvFormBalancePreview.setText("⚠️ Limit exceeded! Maximum 10 clothes allowed per booking.");
                tvFormBalancePreview.setTextColor(Color.parseColor("#DC2626"));
            } else if (clothes > currentBalance) {
                tvFormBalancePreview.setText("⚠️ Insufficient balance! You only have " + currentBalance + " clothes available.");
                tvFormBalancePreview.setTextColor(Color.parseColor("#DC2626"));
            } else {
                int newBalance = currentBalance - clothes;
                tvFormBalancePreview.setText("✓ Updated Balance will be: " + newBalance + " Clothes (" + clothes + " will be deducted)");
                tvFormBalancePreview.setTextColor(Color.parseColor("#059669"));
            }
        } catch (NumberFormatException e) {
            tvFormBalancePreview.setText("⚠️ Invalid number entered.");
            tvFormBalancePreview.setTextColor(Color.parseColor("#DC2626"));
        }
    }

    private void switchTab(boolean showBookForm) {
        if (showBookForm) {
            scrollBookingForm.setVisibility(View.VISIBLE);
            llMyBookings.setVisibility(View.GONE);
            btnTabBook.setBackgroundResource(R.drawable.bg_button_primary);
            btnTabBook.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnTabMyBookings.setBackgroundResource(R.drawable.bg_button_secondary);
            btnTabMyBookings.setTextColor(ContextCompat.getColor(this, R.color.primary));
            refreshLiveBalance();
        } else {
            scrollBookingForm.setVisibility(View.GONE);
            llMyBookings.setVisibility(View.VISIBLE);
            btnTabMyBookings.setBackgroundResource(R.drawable.bg_button_primary);
            btnTabMyBookings.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnTabBook.setBackgroundResource(R.drawable.bg_button_secondary);
            btnTabBook.setTextColor(ContextCompat.getColor(this, R.color.primary));
            loadBookingsList();
        }
    }

    private void showDropDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                UserDashboardActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dropCalendar.set(Calendar.YEAR, year);
                        dropCalendar.set(Calendar.MONTH, month);
                        dropCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formattedDrop = dateFormatter.format(dropCalendar.getTime());
                        tvDropDate.setText(formattedDrop);

                        pickupCalendar.setTime(dropCalendar.getTime());
                        pickupCalendar.add(Calendar.DAY_OF_MONTH, 2);
                        tvPickupDate.setText(dateFormatter.format(pickupCalendar.getTime()));

                        updateDailySlotsCounter(formattedDrop);
                    }
                },
                dropCalendar.get(Calendar.YEAR),
                dropCalendar.get(Calendar.MONTH),
                dropCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showPickupDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                UserDashboardActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        pickupCalendar.set(Calendar.YEAR, year);
                        pickupCalendar.set(Calendar.MONTH, month);
                        pickupCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvPickupDate.setText(dateFormatter.format(pickupCalendar.getTime()));
                    }
                },
                pickupCalendar.get(Calendar.YEAR),
                pickupCalendar.get(Calendar.MONTH),
                pickupCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void processBookingSubmission() {
        String clothesStr = etClothesCount.getText().toString().trim();
        if (clothesStr.isEmpty()) {
            Toast.makeText(this, "Please enter the number of clothes to wash", Toast.LENGTH_SHORT).show();
            etClothesCount.requestFocus();
            return;
        }

        int clothesCount;
        try {
            clothesCount = Integer.parseInt(clothesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clothesCount <= 0) {
            Toast.makeText(this, "Number of clothes must be at least 1", Toast.LENGTH_SHORT).show();
            return;
        }

        // STRICT REQUIREMENT 1: Max 10 clothes limit per booking
        if (clothesCount > 10) {
            Toast.makeText(this, "Limit exceeded! Maximum 10 clothes allowed per booking.", Toast.LENGTH_LONG).show();
            return;
        }

        // STRICT REQUIREMENT 2: Must not exceed current available clothes balance
        int currentBalance = dbHelper.getUserBalance(loggedInUser);
        if (clothesCount > currentBalance) {
            Toast.makeText(this, "Insufficient balance! You only have " + currentBalance + " clothes left this month.", Toast.LENGTH_LONG).show();
            return;
        }

        // STRICT REQUIREMENT 3 (DAILY LIMIT): Max 3 drop-offs per day
        String dropDate = tvDropDate.getText().toString().trim();
        int bookingsOnDate = dbHelper.getBookingsCountByDate(loggedInUser, dropDate);
        if (bookingsOnDate >= 3) {
            Toast.makeText(this, "Daily limit reached! You can only give laundry 3 times a day.", Toast.LENGTH_LONG).show();
            return;
        }

        // Collect special wash preferences from CheckBoxes
        ArrayList<String> optionsList = new ArrayList<>();
        if (cbFragile.isChecked()) optionsList.add("Gentle Wash");
        if (cbFold.isChecked()) optionsList.add("Fold Only");
        if (cbIron.isChecked()) optionsList.add("Ironing");
        String washPreferences = optionsList.isEmpty() ? "Standard Wash" : android.text.TextUtils.join(", ", optionsList);

        String dropTime = spDropTime.getSelectedItem().toString();
        String pickupDate = tvPickupDate.getText().toString().trim();

        // 1. Insert into SQLite database
        long bookingId = dbHelper.insertBooking(
                loggedInUser,
                clothesCount,
                washPreferences,
                dropDate,
                dropTime,
                pickupDate,
                "Pending Admin Approval"
        );

        if (bookingId != -1) {
            // 2. DEDUCT CLOTHES FROM USER'S AVAILABLE BALANCE IN SQLITE
            dbHelper.deductUserBalance(loggedInUser, clothesCount);
            int newBalance = dbHelper.getUserBalance(loggedInUser);

            Toast.makeText(this, "Booking #" + bookingId + " Created! " + clothesCount + " clothes deducted. New Balance: " + newBalance, Toast.LENGTH_LONG).show();

            // Reset form fields
            etClothesCount.setText("");
            cbFragile.setChecked(false);
            cbFold.setChecked(false);
            cbIron.setChecked(false);

            // 3. Immediately refresh Live Balance everywhere
            refreshLiveBalance();
            switchTab(false);
        } else {
            Toast.makeText(this, "Failed to submit booking. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes Available Balance in both the top header and inside the booking form.
     */
    private void refreshLiveBalance() {
        int balance = dbHelper.getUserBalance(loggedInUser);
        tvLiveBalance.setText(balance + " Clothes");
        tvFormAvailableBalance.setText("Available Balance: " + balance + " Clothes");

        String selectedDropDate = tvDropDate.getText().toString().trim();
        updateDailySlotsCounter(selectedDropDate);
        updateBalancePreview();
    }

    private void updateDailySlotsCounter(String date) {
        int usedSlots = dbHelper.getBookingsCountByDate(loggedInUser, date);
        tvTodaySlots.setText(usedSlots + " / 3 used");
    }

    private void loadBookingsList() {
        userBookingsList = dbHelper.getBookingsByUser(loggedInUser);

        if (userBookingsList.isEmpty()) {
            tvEmptyUserBookings.setVisibility(View.VISIBLE);
            lvUserBookings.setVisibility(View.GONE);
        } else {
            tvEmptyUserBookings.setVisibility(View.GONE);
            lvUserBookings.setVisibility(View.VISIBLE);

            String[] from = {
                    "booking_id_display",
                    "status_display",
                    "clothes_display",
                    "options_display",
                    "drop_display",
                    "pickup_display",
                    "allocated_display"
            };

            int[] to = {
                    R.id.tv_booking_id,
                    R.id.tv_status,
                    R.id.tv_clothes,
                    R.id.tv_options,
                    R.id.tv_drop,
                    R.id.tv_pickup,
                    R.id.tv_allocated
            };

            bookingsAdapter = new SimpleAdapter(
                    UserDashboardActivity.this,
                    userBookingsList,
                    R.layout.list_item_booking,
                    from,
                    to
            );

            lvUserBookings.setAdapter(bookingsAdapter);
        }
    }

    private void handleBookingItemClick(HashMap<String, String> item) {
        final int bookingId = Integer.parseInt(item.get("raw_id"));
        final int clothesCount = Integer.parseInt(item.get("raw_clothes"));
        final String status = item.get("status_display");

        AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboardActivity.this);
        builder.setTitle("Booking #" + bookingId + " Options");

        if (status.equals("Pending Admin Approval")) {
            builder.setMessage("This booking is pending laundry staff admission.\n\n"
                    + "Clothes: " + clothesCount + "\n"
                    + "Drop-off: " + item.get("drop_display") + "\n\n"
                    + "Do you want to cancel this request and refund " + clothesCount + " clothes back to your balance?");

            builder.setPositiveButton("Cancel Booking & Refund", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete booking and refund balance
                    dbHelper.deleteBooking(bookingId);
                    dbHelper.refundUserBalance(loggedInUser, clothesCount);
                    int updatedBalance = dbHelper.getUserBalance(loggedInUser);

                    Toast.makeText(UserDashboardActivity.this, "Booking #" + bookingId + " cancelled. " + clothesCount + " clothes refunded! New Balance: " + updatedBalance, Toast.LENGTH_LONG).show();
                    refreshLiveBalance();
                    loadBookingsList();
                }
            });

            builder.setNegativeButton("Keep Booking", null);

        } else if (status.equals("Pickup Approved (Ready)")) {
            builder.setMessage("Great news! Your clothes are washed, dried, and ready for pickup.\n\n"
                    + "Confirm that you have collected your clean laundry from the counter?");

            builder.setPositiveButton("Confirm Picked Up", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.updateBookingStatus(bookingId, "Picked Up");
                    Toast.makeText(UserDashboardActivity.this, "Laundry marked as Picked Up! Thank you.", Toast.LENGTH_SHORT).show();
                    loadBookingsList();
                }
            });

            builder.setNegativeButton("Not Yet", null);

        } else if (status.equals("Admitted / In Wash")) {
            builder.setMessage("Your clothes are currently inside the washing machine / being processed.\n\n"
                    + item.get("allocated_display") + "\n\n"
                    + "You will be notified once laundry staff approves pickup.");
            builder.setPositiveButton("OK", null);

        } else if (status.equals("Disapproved")) {
            builder.setMessage("This booking was disapproved by laundry staff.\n\n"
                    + "Your " + clothesCount + " clothes quota has already been refunded back to your account balance.");
            builder.setPositiveButton("OK", null);

        } else {
            builder.setMessage("This laundry booking has been completed and collected. Thank you for using NMIMS Laundry Portal!");
            builder.setPositiveButton("OK", null);
        }

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshLiveBalance();
            loadBookingsList();
            Toast.makeText(this, "Dashboard refreshed!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            performLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out of NMIMS Laundry Portal?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(UserDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Stay", null);
        builder.show();
    }
}
