package com.aidfinder.auth.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aidfinder.auth.model.Favorite;
import com.aidfinder.auth.model.JournalEntry;
import com.aidfinder.auth.model.UserProfile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_FAV_USERNAME = "username";
    private static final String COLUMN_FAV_PRODUCT_ID = "product_id";
    private static final String COLUMN_FAV_PRODUCT_NAME = "product_name";
    private static final String COLUMN_FAV_WHY = "why";

    private static final String TABLE_JOURNAL = "journal_entries";
    private static final String COLUMN_JOURNAL_USERNAME = "username";
    private static final String COLUMN_JOURNAL_PRODUCT_ID = "product_id";
    private static final String COLUMN_JOURNAL_PRODUCT_NAME = "product_name";
    private static final String COLUMN_JOURNAL_TEXT = "entry_text";
    private static final String COLUMN_JOURNAL_RATING = "rating";
    private static final String COLUMN_JOURNAL_CREATED_AT = "created_at";

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
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_JOURNAL_TABLE);
    }

    private static final String CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FAV_USERNAME + " TEXT,"
            + COLUMN_FAV_PRODUCT_ID + " TEXT,"
            + COLUMN_FAV_PRODUCT_NAME + " TEXT,"
            + COLUMN_FAV_WHY + " TEXT,"
            + "UNIQUE(" + COLUMN_FAV_USERNAME + ", " + COLUMN_FAV_PRODUCT_ID + "))";

    // No UNIQUE constraint - this is an append-only log, not one row per device.
    private static final String CREATE_JOURNAL_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_JOURNAL + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_JOURNAL_USERNAME + " TEXT,"
            + COLUMN_JOURNAL_PRODUCT_ID + " TEXT,"
            + COLUMN_JOURNAL_PRODUCT_NAME + " TEXT,"
            + COLUMN_JOURNAL_TEXT + " TEXT,"
            + COLUMN_JOURNAL_RATING + " INTEGER,"
            + COLUMN_JOURNAL_CREATED_AT + " INTEGER)";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Both run unconditionally (not inside the chain below) so they're not
        // skipped by the existing if/else-if ladder for whichever old version
        // someone is upgrading from.
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_JOURNAL_TABLE);

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

    public boolean addFavorite(String username, String productId, String productName, String why) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAV_USERNAME, username);
        values.put(COLUMN_FAV_PRODUCT_ID, productId);
        values.put(COLUMN_FAV_PRODUCT_NAME, productName);
        values.put(COLUMN_FAV_WHY, why);

        long id = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return id != -1;
    }

    public boolean removeFavorite(String username, String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FAVORITES,
                COLUMN_FAV_USERNAME + " = ? AND " + COLUMN_FAV_PRODUCT_ID + " = ?",
                new String[]{username, productId});
        db.close();
        return rows > 0;
    }

    public boolean isFavorite(String username, String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID},
                COLUMN_FAV_USERNAME + " = ? AND " + COLUMN_FAV_PRODUCT_ID + " = ?",
                new String[]{username, productId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /** Most recently favorited first. */
    public List<Favorite> getFavorites(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Favorite> favorites = new ArrayList<>();
        Cursor cursor = db.query(TABLE_FAVORITES, null, COLUMN_FAV_USERNAME + " = ?",
                new String[]{username}, null, null, COLUMN_ID + " DESC");
        while (cursor.moveToNext()) {
            favorites.add(new Favorite(
                    getStringValue(cursor, COLUMN_FAV_PRODUCT_ID),
                    getStringValue(cursor, COLUMN_FAV_PRODUCT_NAME),
                    getStringValue(cursor, COLUMN_FAV_WHY)));
        }
        cursor.close();
        db.close();
        return favorites;
    }

    public boolean addJournalEntry(String username, String productId, String productName,
                                    String entryText, int rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JOURNAL_USERNAME, username);
        values.put(COLUMN_JOURNAL_PRODUCT_ID, productId);
        values.put(COLUMN_JOURNAL_PRODUCT_NAME, productName);
        values.put(COLUMN_JOURNAL_TEXT, entryText);
        values.put(COLUMN_JOURNAL_RATING, rating);
        values.put(COLUMN_JOURNAL_CREATED_AT, System.currentTimeMillis());

        long id = db.insert(TABLE_JOURNAL, null, values);
        db.close();
        return id != -1;
    }

    /** Oldest first - the order a timeline reads left to right. */
    public List<JournalEntry> getJournalEntries(String username, String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<JournalEntry> entries = new ArrayList<>();
        Cursor cursor = db.query(TABLE_JOURNAL, null,
                COLUMN_JOURNAL_USERNAME + " = ? AND " + COLUMN_JOURNAL_PRODUCT_ID + " = ?",
                new String[]{username, productId}, null, null, COLUMN_JOURNAL_CREATED_AT + " ASC");
        while (cursor.moveToNext()) {
            entries.add(new JournalEntry(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    getStringValue(cursor, COLUMN_JOURNAL_PRODUCT_ID),
                    getStringValue(cursor, COLUMN_JOURNAL_PRODUCT_NAME),
                    getStringValue(cursor, COLUMN_JOURNAL_TEXT),
                    getIntValue(cursor, COLUMN_JOURNAL_RATING),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_JOURNAL_CREATED_AT))));
        }
        cursor.close();
        db.close();
        return entries;
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
