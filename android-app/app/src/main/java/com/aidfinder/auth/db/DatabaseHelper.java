package com.aidfinder.auth.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aidfinder.auth.model.UserProfile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Profile & Vitals columns
    private static final String COLUMN_BIOLOGICAL_SEX = "biological_sex";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_HEIGHT_FEET = "height_feet";
    private static final String COLUMN_HEIGHT_INCHES = "height_inches";
    private static final String COLUMN_WEIGHT_LBS = "weight_lbs";
    private static final String COLUMN_BLOOD_PRESSURE = "blood_pressure";
    private static final String COLUMN_HEART_RATE = "heart_rate";
    private static final String COLUMN_ACTIVITY_LEVEL = "activity_level";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_BIOLOGICAL_SEX + " TEXT,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_HEIGHT_FEET + " INTEGER,"
                + COLUMN_HEIGHT_INCHES + " INTEGER,"
                + COLUMN_WEIGHT_LBS + " REAL,"
                + COLUMN_BLOOD_PRESSURE + " TEXT,"
                + COLUMN_HEART_RATE + " INTEGER,"
                + COLUMN_ACTIVITY_LEVEL + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_BIOLOGICAL_SEX + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_AGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_HEIGHT_FEET + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_HEIGHT_INCHES + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_WEIGHT_LBS + " REAL");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_BLOOD_PRESSURE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_HEART_RATE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ACTIVITY_LEVEL + " TEXT");
        } else if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_BIOLOGICAL_SEX + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_HEIGHT_FEET + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_HEIGHT_INCHES + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_WEIGHT_LBS + " REAL");
        }
    }

    public boolean addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, PasswordHashing.hash(password));

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String loginInput, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PASSWORD};
        String selection = "(" + COLUMN_USERNAME + " = ? OR " + COLUMN_EMAIL + " = ?)";
        String[] selectionArgs = {loginInput, loginInput};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean matches = false;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(COLUMN_PASSWORD);
            String stored = index != -1 ? cursor.getString(index) : null;
            matches = stored != null && PasswordHashing.matches(password, stored);
        }
        cursor.close();
        db.close();

        return matches;
    }

    public boolean checkUserExists(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? OR " + COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {username, email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    public String getUsernameByLoginInput(String loginInput) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USERNAME};
        String selection = COLUMN_USERNAME + " = ? OR " + COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {loginInput, loginInput};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        String resolvedUsername = loginInput;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(COLUMN_USERNAME);
            if (index != -1) {
                resolvedUsername = cursor.getString(index);
            }
        }
        cursor.close();
        db.close();
        return resolvedUsername;
    }

    public boolean saveUserProfile(UserProfile profile) {
        if (profile == null || profile.getUsername() == null || profile.getUsername().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BIOLOGICAL_SEX, profile.getBiologicalSex());
        values.put(COLUMN_AGE, profile.getAge());
        values.put(COLUMN_HEIGHT_FEET, profile.getHeightFeet());
        values.put(COLUMN_HEIGHT_INCHES, profile.getHeightInches());
        values.put(COLUMN_WEIGHT_LBS, profile.getWeightLbs());
        values.put(COLUMN_BLOOD_PRESSURE, profile.getBloodPressure());
        values.put(COLUMN_HEART_RATE, profile.getHeartRate());
        values.put(COLUMN_ACTIVITY_LEVEL, profile.getActivityLevel());

        int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{profile.getUsername()});
        if (rows == 0) {
            values.put(COLUMN_USERNAME, profile.getUsername());
            if (profile.getEmail() != null) {
                values.put(COLUMN_EMAIL, profile.getEmail());
            }
            long id = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
            return id != -1;
        }
        db.close();
        return true;
    }

    public UserProfile getUserProfile(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        UserProfile profile = new UserProfile();
        profile.setUsername(username);

        if (cursor.moveToFirst()) {
            profile.setEmail(getStringValue(cursor, COLUMN_EMAIL));
            String bioSex = getStringValue(cursor, COLUMN_BIOLOGICAL_SEX);
            if (bioSex.isEmpty()) {
                bioSex = getStringValue(cursor, "gender");
            }
            profile.setBiologicalSex(bioSex);
            profile.setAge(getIntValue(cursor, COLUMN_AGE));
            profile.setHeightFeet(getIntValue(cursor, COLUMN_HEIGHT_FEET));
            profile.setHeightInches(getIntValue(cursor, COLUMN_HEIGHT_INCHES));
            profile.setWeightLbs(getDoubleValue(cursor, COLUMN_WEIGHT_LBS));
            profile.setBloodPressure(getStringValue(cursor, COLUMN_BLOOD_PRESSURE));
            profile.setHeartRate(getIntValue(cursor, COLUMN_HEART_RATE));
            profile.setActivityLevel(getStringValue(cursor, COLUMN_ACTIVITY_LEVEL));
        }
        cursor.close();
        db.close();

        return profile;
    }

    public boolean updateProfileField(String username, String columnName, Object value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (value instanceof Integer) {
            values.put(columnName, (Integer) value);
        } else if (value instanceof Double) {
            values.put(columnName, (Double) value);
        } else if (value != null) {
            values.put(columnName, value.toString());
        } else {
            values.putNull(columnName);
        }

        int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
        return rows > 0;
    }

    private String getStringValue(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return (index != -1 && !cursor.isNull(index)) ? cursor.getString(index) : "";
    }

    private int getIntValue(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return (index != -1 && !cursor.isNull(index)) ? cursor.getInt(index) : 0;
    }

    private double getDoubleValue(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return (index != -1 && !cursor.isNull(index)) ? cursor.getDouble(index) : 0.0;
    }

    /**
     * Salted SHA-256, stored as "saltHex:hashHex". No external dependency,
     * since Android ships java.security out of the box.
     */
    private static final class PasswordHashing {
        private static final int SALT_BYTES = 16;

        static String hash(String password) {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            byte[] digest = digest(password, salt);
            return toHex(salt) + ":" + toHex(digest);
        }

        static boolean matches(String password, String stored) {
            int separator = stored.indexOf(':');
            if (separator < 0) {
                return false;
            }
            byte[] salt = fromHex(stored.substring(0, separator));
            byte[] expected = fromHex(stored.substring(separator + 1));
            byte[] actual = digest(password, salt);
            return MessageDigest.isEqual(expected, actual);
        }

        private static byte[] digest(String password, byte[] salt) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);
                return md.digest(password.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private static String toHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        private static byte[] fromHex(String hex) {
            int len = hex.length();
            byte[] out = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
            return out;
        }
    }
}
