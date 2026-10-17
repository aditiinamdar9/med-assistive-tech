package com.aidfinder.auth.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
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
