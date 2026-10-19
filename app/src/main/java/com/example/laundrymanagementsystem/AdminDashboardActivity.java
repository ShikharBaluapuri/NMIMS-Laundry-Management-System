package com.example.laundrymanagementsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * =========================================================================
 * VIVA EXPLANATION: AdminDashboardActivity (Staff / Admin Module)
 * =========================================================================
 * 1. Metrics Header: Live counts for Pending, In Wash, and Total requests.
 * 2. Spinner Filter: Filters bookings by status.
 * 3. Direct Actions per Item: Each card in the ListView has direct buttons:
 *    - [ Admit ]: Moves status to "Admitted / In Wash".
 *    - [ Ready ]: Moves status to "Pickup Approved (Ready)".
 *    - [ Reject ]: Moves status to "Disapproved" and refunds quota to student.
 *    - [ Allocate ]: Assigns washer/staff to handle the booking.
 * 4. Sample Data Auto-Generation: Guarantees sample student bookings are present.
 * =========================================================================
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String loggedInAdmin;

    // Summary Metric Widgets
    private TextView tvAdminWelcome;
    private TextView tvMetricPending, tvMetricActive, tvMetricTotal;

    // Filter & List Widgets
    private Spinner spAdminFilter;
    private Button btnAdminAddTest, btnRestoreDemo;
    private ListView lvAdminBookings;
    private LinearLayout llEmptyContainer;

    private ArrayList<HashMap<String, String>> adminBookingsList;
    private SimpleAdapter adminAdapter;
    private String currentFilter = "All Requests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        loggedInAdmin = getIntent().getStringExtra("USERNAME");
        if (loggedInAdmin == null || loggedInAdmin.isEmpty()) {
            loggedInAdmin = "admin";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Laundry Console");
            getSupportActionBar().setSubtitle("NMIMS Shirpur Campus");
        }

        dbHelper = new DatabaseHelper(this);

        // Guarantees that sample student bookings are pre-populated on device
        dbHelper.ensureSampleData();

        initViews();
        setupFilterSpinner();
        setupListView();
        setupQuickActionButtons();

        refreshDashboard();
    }

    private void initViews() {
        tvAdminWelcome = findViewById(R.id.tv_admin_welcome);
        tvMetricPending = findViewById(R.id.tv_metric_pending);
        tvMetricActive = findViewById(R.id.tv_metric_active);
        tvMetricTotal = findViewById(R.id.tv_metric_total);

        spAdminFilter = findViewById(R.id.sp_admin_filter);
        btnAdminAddTest = findViewById(R.id.btn_admin_add_test);
        btnRestoreDemo = findViewById(R.id.btn_restore_demo_bookings);
        lvAdminBookings = findViewById(R.id.lv_admin_bookings);
        llEmptyContainer = findViewById(R.id.ll_empty_admin_container);

        tvAdminWelcome.setText("Welcome, " + loggedInAdmin + "!");
    }

    private void setupQuickActionButtons() {
        // Quick button to add a test booking on the fly
        btnAdminAddTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.insertBooking("student1", 5, "Fragile / Gentle Wash", "2026-10-17", "10:00 AM - 12:00 PM", "2026-10-19", "Pending Admin Approval");
                Toast.makeText(AdminDashboardActivity.this, "Test student booking created! Ready for approval.", Toast.LENGTH_SHORT).show();
                refreshDashboard();
            }
        });

        // Restore demo bookings button when empty
        btnRestoreDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.ensureSampleData();
                Toast.makeText(AdminDashboardActivity.this, "Demo student bookings loaded!", Toast.LENGTH_SHORT).show();
                refreshDashboard();
            }
        });
    }

    private void setupFilterSpinner() {
        String[] filters = {
                "All Requests",
                "Pending Admin Approval",
                "Admitted / In Wash",
                "Pickup Approved (Ready)",
                "Picked Up",
                "Disapproved"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filters
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAdminFilter.setAdapter(spinnerAdapter);

        spAdminFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                loadAdminBookings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListView() {
        // Fallback row click
        lvAdminBookings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adminBookingsList != null && position < adminBookingsList.size()) {
                    HashMap<String, String> selectedItem = adminBookingsList.get(position);
                    showAdminActionDialog(selectedItem);
                }
            }
        });
    }

    private void refreshDashboard() {
        int pending = dbHelper.getPendingCount();
        int active = dbHelper.getActiveWashCount();
        int total = dbHelper.getTotalBookingsCount();

        tvMetricPending.setText(String.valueOf(pending));
        tvMetricActive.setText(String.valueOf(active));
        tvMetricTotal.setText(String.valueOf(total));

        loadAdminBookings();
    }

    private void loadAdminBookings() {
        adminBookingsList = dbHelper.getAllBookingsWithStudentDetails(currentFilter);

        if (adminBookingsList.isEmpty()) {
            llEmptyContainer.setVisibility(View.VISIBLE);
            lvAdminBookings.setVisibility(View.GONE);
        } else {
            llEmptyContainer.setVisibility(View.GONE);
            lvAdminBookings.setVisibility(View.VISIBLE);

            String[] from = {
                    "booking_id_display",
                    "student_display",
                    "student_meta",
                    "status_display",
                    "clothes_display",
                    "options_display",
                    "drop_display",
                    "pickup_display",
                    "allocated_display"
            };

            int[] to = {
                    R.id.tv_admin_booking_id,
                    R.id.tv_admin_student,
                    R.id.tv_admin_student_meta,
                    R.id.tv_admin_status,
                    R.id.tv_admin_clothes,
                    R.id.tv_admin_options,
                    R.id.tv_admin_drop,
                    R.id.tv_admin_pickup,
                    R.id.tv_admin_allocated
            };

            // SimpleAdapter with direct button click listeners wired in getView
            adminAdapter = new SimpleAdapter(
                    AdminDashboardActivity.this,
                    adminBookingsList,
                    R.layout.list_item_admin_booking,
                    from,
                    to
            ) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View rowView = super.getView(position, convertView, parent);

                    if (adminBookingsList != null && position < adminBookingsList.size()) {
                        final HashMap<String, String> item = adminBookingsList.get(position);
                        final int bId = Integer.parseInt(item.get("raw_id"));
                        final String stUser = item.get("raw_username");
                        final int cCount = Integer.parseInt(item.get("raw_clothes"));
                        final String stStatus = item.get("status_display");

                        Button btnAdmit = rowView.findViewById(R.id.btn_row_admit);
                        Button btnReady = rowView.findViewById(R.id.btn_row_ready);
                        Button btnDisapprove = rowView.findViewById(R.id.btn_row_disapprove);
                        Button btnAllocate = rowView.findViewById(R.id.btn_row_allocate);

                        // 1. Admit Clothes Button
                        if (btnAdmit != null) {
                            btnAdmit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dbHelper.updateBookingStatus(bId, "Admitted / In Wash");
                                    Toast.makeText(AdminDashboardActivity.this, "Booking #" + bId + " admitted into wash!", Toast.LENGTH_SHORT).show();
                                    refreshDashboard();
                                }
                            });
                        }

                        // 2. Mark Ready for Pickup Button
                        if (btnReady != null) {
                            btnReady.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dbHelper.updateBookingStatus(bId, "Pickup Approved (Ready)");
                                    Toast.makeText(AdminDashboardActivity.this, "Booking #" + bId + " approved for pickup!", Toast.LENGTH_SHORT).show();
                                    refreshDashboard();
                                }
                            });
                        }

                        // 3. Reject & Refund Button
                        if (btnDisapprove != null) {
                            btnDisapprove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showDisapproveConfirmDialog(bId, stUser, cCount, stStatus);
                                }
                            });
                        }

                        // 4. Allocate Staff Button
                        if (btnAllocate != null) {
                            btnAllocate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showStaffAllocationDialog(bId);
                                }
                            });
                        }
                    }

                    return rowView;
                }
            };

            lvAdminBookings.setAdapter(adminAdapter);
        }
    }

    private void showAdminActionDialog(final HashMap<String, String> booking) {
        final int bookingId = Integer.parseInt(booking.get("raw_id"));
        final String student = booking.get("raw_username");
        final int clothes = Integer.parseInt(booking.get("raw_clothes"));
        final String currentStatus = booking.get("status_display");

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminDashboardActivity.this);
        builder.setTitle("Manage Booking #" + bookingId);

        String message = booking.get("student_display") + "\n"
                + booking.get("student_meta") + "\n\n"
                + "Clothes Count: " + clothes + " items\n"
                + "Wash Options: " + booking.get("options_display") + "\n"
                + "Drop-off: " + booking.get("drop_display") + "\n"
                + "Pickup: " + booking.get("pickup_display") + "\n"
                + "Current Status: " + currentStatus + "\n"
                + booking.get("allocated_display") + "\n\n"
                + "Select staff action:";

        builder.setMessage(message);

        final String[] actions = {
                "✅ 1. Admit Clothes (Accept Drop-off)",
                "👕 2. Approve Pickup (Clothes Ready)",
                "❌ 3. Disapprove / Reject (Auto-Refund Quota)",
                "👤 4. Allocate Staff / Washer Person",
                "🗑️ 5. Delete Record",
                "Cancel / Close"
        };

        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dbHelper.updateBookingStatus(bookingId, "Admitted / In Wash");
                        Toast.makeText(AdminDashboardActivity.this, "Booking #" + bookingId + " status updated to 'Admitted / In Wash'", Toast.LENGTH_SHORT).show();
                        refreshDashboard();
                        break;

                    case 1:
                        dbHelper.updateBookingStatus(bookingId, "Pickup Approved (Ready)");
                        Toast.makeText(AdminDashboardActivity.this, "Booking #" + bookingId + " approved for student pickup!", Toast.LENGTH_SHORT).show();
                        refreshDashboard();
                        break;

                    case 2:
                        showDisapproveConfirmDialog(bookingId, student, clothes, currentStatus);
                        break;

                    case 3:
                        showStaffAllocationDialog(bookingId);
                        break;

                    case 4:
                        showDeleteConfirmDialog(bookingId);
                        break;

                    case 5:
                        dialog.dismiss();
                        break;
                }
            }
        });

        builder.show();
    }

    private void showStaffAllocationDialog(final int bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Allocate Staff for Booking #" + bookingId);

        final String[] staffList = {
                "Ramesh (Washing Machine 1)",
                "Suresh (Washing Machine 2)",
                "Mahesh (Dry Clean & Pressing)",
                "Pooja (Sorting & Folding)",
                "Kailash (Express Wash Counter)",
                "Custom Staff Name / Other..."
        };

        builder.setItems(staffList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == staffList.length - 1) {
                    showCustomStaffInputDialog(bookingId);
                } else {
                    String selectedStaff = staffList[which];
                    dbHelper.allocateStaff(bookingId, selectedStaff);
                    Toast.makeText(AdminDashboardActivity.this, "Booking #" + bookingId + " allocated to: " + selectedStaff, Toast.LENGTH_LONG).show();
                    refreshDashboard();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCustomStaffInputDialog(final int bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Staff / Washer Name");

        final EditText input = new EditText(this);
        input.setHint("e.g. Counter Staff Rahul");
        input.setPadding(40, 30, 40, 20);
        builder.setView(input);

        builder.setPositiveButton("Assign", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String customName = input.getText().toString().trim();
                if (!customName.isEmpty()) {
                    dbHelper.allocateStaff(bookingId, customName);
                    Toast.makeText(AdminDashboardActivity.this, "Booking #" + bookingId + " allocated to: " + customName, Toast.LENGTH_LONG).show();
                    refreshDashboard();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Staff name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDisapproveConfirmDialog(final int bookingId, final String student, final int clothes, final String currentStatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disapprove Booking #" + bookingId);
        builder.setMessage("Are you sure you want to reject this booking?\n\n"
                + "NOTE: " + clothes + " clothes will be automatically refunded back to " + student + "'s monthly balance.");

        builder.setPositiveButton("Confirm Disapprove & Refund", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.updateBookingStatus(bookingId, "Disapproved");

                if (!currentStatus.equals("Disapproved")) {
                    dbHelper.refundUserBalance(student, clothes);
                    Toast.makeText(AdminDashboardActivity.this, "Booking #" + bookingId + " disapproved. " + clothes + " clothes refunded to " + student + ".", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Booking is already marked as Disapproved.", Toast.LENGTH_SHORT).show();
                }

                refreshDashboard();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmDialog(final int bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Booking #" + bookingId);
        builder.setMessage("Are you sure you want to permanently delete this booking record from SQLite?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteBooking(bookingId);
                Toast.makeText(AdminDashboardActivity.this, "Booking #" + bookingId + " permanently deleted.", Toast.LENGTH_SHORT).show();
                refreshDashboard();
            }
        });

        builder.setNegativeButton("Cancel", null);
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
            refreshDashboard();
            Toast.makeText(this, "Admin console refreshed!", Toast.LENGTH_SHORT).show();
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
        builder.setMessage("Are you sure you want to log out of Admin Console?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Stay", null);
        builder.show();
    }
}
