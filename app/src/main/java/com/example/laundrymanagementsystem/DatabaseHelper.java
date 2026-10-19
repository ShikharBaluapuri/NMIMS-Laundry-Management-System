package com.example.laundrymanagementsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * =========================================================================
 * VIVA EXPLANATION: SQLiteOpenHelper with Student Details & Staff Allocation
 * =========================================================================
 * Tables:
 * 1. users: id, username, password, role, balance, sap_id, phone, batch, year
 * 2. bookings: booking_id, username, clothes_count, options, drop_date,
 *              drop_time, pickup_date, status, allocated_to
 * =========================================================================
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NMIMS_Laundry.db";
    // Version 3 guarantees complete table structures and sample bookings
    private static final int DATABASE_VERSION = 3;

    // Table 1: USERS
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";
    public static final String COL_BALANCE = "balance";
    public static final String COL_SAP_ID = "sap_id";
    public static final String COL_PHONE = "phone";
    public static final String COL_BATCH = "batch";
    public static final String COL_YEAR = "year";

    // Table 2: BOOKINGS
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COL_BOOKING_ID = "booking_id";
    public static final String COL_B_USERNAME = "username";
    public static final String COL_CLOTHES_COUNT = "clothes_count";
    public static final String COL_OPTIONS = "options";
    public static final String COL_DROP_DATE = "drop_date";
    public static final String COL_DROP_TIME = "drop_time";
    public static final String COL_PICKUP_DATE = "pickup_date";
    public static final String COL_STATUS = "status";
    public static final String COL_ALLOCATED_TO = "allocated_to";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT, "
                + COL_ROLE + " TEXT, "
                + COL_BALANCE + " INTEGER DEFAULT 30, "
                + COL_SAP_ID + " TEXT, "
                + COL_PHONE + " TEXT, "
                + COL_BATCH + " TEXT, "
                + COL_YEAR + " TEXT"
                + ")";
        db.execSQL(createUsersTable);

        // 2. Create Bookings Table
        String createBookingsTable = "CREATE TABLE " + TABLE_BOOKINGS + " ("
                + COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_B_USERNAME + " TEXT, "
                + COL_CLOTHES_COUNT + " INTEGER, "
                + COL_OPTIONS + " TEXT, "
                + COL_DROP_DATE + " TEXT, "
                + COL_DROP_TIME + " TEXT, "
                + COL_PICKUP_DATE + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_ALLOCATED_TO + " TEXT DEFAULT 'Unassigned'"
                + ")";
        db.execSQL(createBookingsTable);

        // 3. Pre-seed Default Admin Account
        ContentValues adminValues = new ContentValues();
        adminValues.put(COL_USERNAME, "admin");
        adminValues.put(COL_PASSWORD, "admin123");
        adminValues.put(COL_ROLE, "Admin");
        adminValues.put(COL_BALANCE, 0);
        adminValues.put(COL_SAP_ID, "STAFF-001");
        adminValues.put(COL_PHONE, "9876543210");
        adminValues.put(COL_BATCH, "Laundry Dept");
        adminValues.put(COL_YEAR, "Admin/Staff");
        db.insert(TABLE_USERS, null, adminValues);

        // 4. Pre-seed Demo Student Account
        ContentValues studentValues = new ContentValues();
        studentValues.put(COL_USERNAME, "student1");
        studentValues.put(COL_PASSWORD, "pass123");
        studentValues.put(COL_ROLE, "User");
        studentValues.put(COL_BALANCE, 30);
        studentValues.put(COL_SAP_ID, "70012345678");
        studentValues.put(COL_PHONE, "9823012345");
        studentValues.put(COL_BATCH, "B.Tech Computer");
        studentValues.put(COL_YEAR, "3rd Year");
        db.insert(TABLE_USERS, null, studentValues);

        // 5. Pre-seed Sample Bookings for Admin & Student Viva Testing
        insertInitialSampleBookings(db);
    }

    private void insertInitialSampleBookings(SQLiteDatabase db) {
        ContentValues sampleBooking1 = new ContentValues();
        sampleBooking1.put(COL_B_USERNAME, "student1");
        sampleBooking1.put(COL_CLOTHES_COUNT, 6);
        sampleBooking1.put(COL_OPTIONS, "Gentle Wash, Fold Only");
        sampleBooking1.put(COL_DROP_DATE, "2026-10-17");
        sampleBooking1.put(COL_DROP_TIME, "10:00 AM - 12:00 PM");
        sampleBooking1.put(COL_PICKUP_DATE, "2026-10-19");
        sampleBooking1.put(COL_STATUS, "Pending Admin Approval");
        sampleBooking1.put(COL_ALLOCATED_TO, "Unassigned");
        db.insert(TABLE_BOOKINGS, null, sampleBooking1);

        ContentValues sampleBooking2 = new ContentValues();
        sampleBooking2.put(COL_B_USERNAME, "student1");
        sampleBooking2.put(COL_CLOTHES_COUNT, 4);
        sampleBooking2.put(COL_OPTIONS, "Ironing Required");
        sampleBooking2.put(COL_DROP_DATE, "2026-10-16");
        sampleBooking2.put(COL_DROP_TIME, "02:00 PM - 04:00 PM");
        sampleBooking2.put(COL_PICKUP_DATE, "2026-10-18");
        sampleBooking2.put(COL_STATUS, "Admitted / In Wash");
        sampleBooking2.put(COL_ALLOCATED_TO, "Ramesh (Machine 1)");
        db.insert(TABLE_BOOKINGS, null, sampleBooking2);

        ContentValues sampleBooking3 = new ContentValues();
        sampleBooking3.put(COL_B_USERNAME, "student1");
        sampleBooking3.put(COL_CLOTHES_COUNT, 8);
        sampleBooking3.put(COL_OPTIONS, "Standard Wash");
        sampleBooking3.put(COL_DROP_DATE, "2026-10-15");
        sampleBooking3.put(COL_DROP_TIME, "08:00 AM - 10:00 AM");
        sampleBooking3.put(COL_PICKUP_DATE, "2026-10-17");
        sampleBooking3.put(COL_STATUS, "Pickup Approved (Ready)");
        sampleBooking3.put(COL_ALLOCATED_TO, "Suresh (Machine 2)");
        db.insert(TABLE_BOOKINGS, null, sampleBooking3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Safety check called on dashboard load:
     * Guarantees that if the database has 0 bookings on disk, sample student requests
     * are automatically created so the Admin always has requests to approve/disapprove/allocate!
     */
    public void ensureSampleData() {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Ensure student1 exists
        if (!checkUsernameExists("student1")) {
            ContentValues studentValues = new ContentValues();
            studentValues.put(COL_USERNAME, "student1");
            studentValues.put(COL_PASSWORD, "pass123");
            studentValues.put(COL_ROLE, "User");
            studentValues.put(COL_BALANCE, 30);
            studentValues.put(COL_SAP_ID, "70012345678");
            studentValues.put(COL_PHONE, "9823012345");
            studentValues.put(COL_BATCH, "B.Tech Computer");
            studentValues.put(COL_YEAR, "3rd Year");
            db.insert(TABLE_USERS, null, studentValues);
        }

        // 2. Ensure admin exists
        if (!checkUsernameExists("admin")) {
            ContentValues adminValues = new ContentValues();
            adminValues.put(COL_USERNAME, "admin");
            adminValues.put(COL_PASSWORD, "admin123");
            adminValues.put(COL_ROLE, "Admin");
            adminValues.put(COL_BALANCE, 0);
            adminValues.put(COL_SAP_ID, "STAFF-001");
            adminValues.put(COL_PHONE, "9876543210");
            adminValues.put(COL_BATCH, "Laundry Dept");
            adminValues.put(COL_YEAR, "Admin/Staff");
            db.insert(TABLE_USERS, null, adminValues);
        }

        // 3. Ensure bookings exist
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        if (count == 0) {
            insertInitialSampleBookings(db);
        }
    }

    // =========================================================================
    // USER AUTHENTICATION & REGISTRATION
    // =========================================================================

    public boolean checkUser(String username, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COL_USER_ID};
        String selection = COL_USERNAME + "=? AND " + COL_PASSWORD + "=? AND " + COL_ROLE + "=?";
        String[] selectionArgs = {username.trim(), password.trim(), role.trim()};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USERNAME + "=?", new String[]{username.trim()},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkSapIdExists(String sapId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_SAP_ID + "=?", new String[]{sapId.trim()},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean registerStudent(String username, String password, String sapId,
                                   String phone, String batch, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username.trim());
        values.put(COL_PASSWORD, password.trim());
        values.put(COL_ROLE, "User");
        values.put(COL_BALANCE, 30);
        values.put(COL_SAP_ID, sapId.trim());
        values.put(COL_PHONE, phone.trim());
        values.put(COL_BATCH, batch.trim());
        values.put(COL_YEAR, year.trim());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public HashMap<String, String> getUserDetails(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, String> map = new HashMap<>();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USERNAME + "=?", new String[]{username.trim()},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            map.put("username", cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)));
            map.put("sap_id", cursor.getString(cursor.getColumnIndexOrThrow(COL_SAP_ID)));
            map.put("phone", cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));
            map.put("batch", cursor.getString(cursor.getColumnIndexOrThrow(COL_BATCH)));
            map.put("year", cursor.getString(cursor.getColumnIndexOrThrow(COL_YEAR)));
            map.put("balance", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BALANCE))));
            cursor.close();
        }
        return map;
    }

    public int getUserBalance(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int balance = 0;
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_BALANCE},
                COL_USERNAME + "=?", new String[]{username.trim()},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            balance = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BALANCE));
            cursor.close();
        }
        return balance;
    }

    public boolean deductUserBalance(String username, int amount) {
        int currentBalance = getUserBalance(username);
        int updatedBalance = Math.max(0, currentBalance - amount);
        return updateUserBalance(username, updatedBalance);
    }

    public boolean refundUserBalance(String username, int amount) {
        int currentBalance = getUserBalance(username);
        int updatedBalance = currentBalance + amount;
        return updateUserBalance(username, updatedBalance);
    }

    public boolean updateUserBalance(String username, int newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BALANCE, newBalance);
        int rows = db.update(TABLE_USERS, values, COL_USERNAME + "=?", new String[]{username.trim()});
        return rows > 0;
    }

    // =========================================================================
    // LAUNDRY BOOKINGS CRUD & ALLOCATION
    // =========================================================================

    public long insertBooking(String username, int clothesCount, String options,
                              String dropDate, String dropTime, String pickupDate, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_B_USERNAME, username.trim());
        values.put(COL_CLOTHES_COUNT, clothesCount);
        values.put(COL_OPTIONS, options);
        values.put(COL_DROP_DATE, dropDate);
        values.put(COL_DROP_TIME, dropTime);
        values.put(COL_PICKUP_DATE, pickupDate);
        values.put(COL_STATUS, status);
        values.put(COL_ALLOCATED_TO, "Unassigned");

        return db.insert(TABLE_BOOKINGS, null, values);
    }

    public int getBookingsCountByDate(String username, String dropDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_BOOKINGS + " WHERE "
                + COL_B_USERNAME + "=? AND " + COL_DROP_DATE + "=? AND "
                + COL_STATUS + " != 'Disapproved'";
        Cursor cursor = db.rawQuery(query, new String[]{username.trim(), dropDate.trim()});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public boolean allocateStaff(int bookingId, String staffName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ALLOCATED_TO, staffName.trim());
        int rows = db.update(TABLE_BOOKINGS, values, COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    public boolean updateBookingStatus(int bookingId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, newStatus);
        int rows = db.update(TABLE_BOOKINGS, values, COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    public boolean deleteBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_BOOKINGS, COL_BOOKING_ID + "=?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    public ArrayList<HashMap<String, String>> getBookingsByUser(String username) {
        ArrayList<HashMap<String, String>> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_BOOKINGS
                + " WHERE " + COL_B_USERNAME + "=?"
                + " ORDER BY " + COL_BOOKING_ID + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{username.trim()});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOKING_ID));
                int clothes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CLOTHES_COUNT));
                String options = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPTIONS));
                String dropDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_DROP_DATE));
                String dropTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_DROP_TIME));
                String pickupDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_PICKUP_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
                String allocatedTo = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALLOCATED_TO));

                map.put("raw_id", String.valueOf(id));
                map.put("booking_id_display", "Booking #" + id);
                map.put("clothes_display", clothes + " Clothes");
                map.put("raw_clothes", String.valueOf(clothes));
                map.put("drop_display", "Drop: " + dropDate + " (" + dropTime + ")");
                map.put("pickup_display", "Est. Pickup: " + pickupDate);
                map.put("options_display", (options == null || options.trim().isEmpty()) ? "Standard Wash" : options);
                map.put("status_display", status);
                map.put("allocated_display", "Assigned: " + (allocatedTo == null || allocatedTo.isEmpty() ? "Unassigned" : allocatedTo));

                bookingList.add(map);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return bookingList;
    }

    public ArrayList<HashMap<String, String>> getAllBookingsWithStudentDetails(String filterStatus) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query;
        String[] args = null;

        String baseQuery = "SELECT b.*, u." + COL_SAP_ID + ", u." + COL_PHONE + ", u." + COL_BATCH + ", u." + COL_YEAR
                + " FROM " + TABLE_BOOKINGS + " b "
                + " LEFT JOIN " + TABLE_USERS + " u ON b." + COL_B_USERNAME + " = u." + COL_USERNAME;

        if (filterStatus == null || filterStatus.equals("All Requests") || filterStatus.isEmpty()) {
            query = baseQuery + " ORDER BY b." + COL_BOOKING_ID + " DESC";
        } else {
            query = baseQuery + " WHERE b." + COL_STATUS + "=? ORDER BY b." + COL_BOOKING_ID + " DESC";
            args = new String[]{filterStatus};
        }

        Cursor cursor = db.rawQuery(query, args);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOKING_ID));
                String student = cursor.getString(cursor.getColumnIndexOrThrow(COL_B_USERNAME));
                int clothes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CLOTHES_COUNT));
                String options = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPTIONS));
                String dropDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_DROP_DATE));
                String dropTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_DROP_TIME));
                String pickupDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_PICKUP_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
                String allocatedTo = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALLOCATED_TO));

                String sapId = cursor.getString(cursor.getColumnIndexOrThrow(COL_SAP_ID));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
                String batch = cursor.getString(cursor.getColumnIndexOrThrow(COL_BATCH));
                String year = cursor.getString(cursor.getColumnIndexOrThrow(COL_YEAR));

                map.put("raw_id", String.valueOf(id));
                map.put("raw_username", student);
                map.put("raw_clothes", String.valueOf(clothes));
                map.put("raw_status", status);
                map.put("booking_id_display", "Booking #" + id);
                map.put("student_display", "Student: " + student + " (SAP: " + (sapId == null ? "N/A" : sapId) + ")");
                map.put("student_meta", "📞 " + (phone == null ? "N/A" : phone) + " | " + (batch == null ? "" : batch) + " (" + (year == null ? "" : year) + ")");
                map.put("clothes_display", clothes + " Clothes");
                map.put("drop_display", "Drop: " + dropDate + " (" + dropTime + ")");
                map.put("pickup_display", "Pickup: " + pickupDate);
                map.put("options_display", (options == null || options.trim().isEmpty()) ? "Standard Wash" : options);
                map.put("status_display", status);
                map.put("allocated_display", "Assigned Staff: " + (allocatedTo == null || allocatedTo.isEmpty() ? "Unassigned" : allocatedTo));

                list.add(map);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public int getPendingCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS + " WHERE " + COL_STATUS + "='Pending Admin Approval'", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getActiveWashCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS + " WHERE " + COL_STATUS + "='Admitted / In Wash'", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalBookingsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }
}
