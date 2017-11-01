package top.soyask.calendarii.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.domain.Birthday;

/**
 * Created by mxf on 2017/10/30.
 */

public class BirthdayDao {

    public static final String BIRTHDAY = "BIRTHDAY";

    private DBUtils mDBUtils;
    private Context mContext;

    private static BirthdayDao mBirthdayDao;

    public BirthdayDao(DBUtils dBUtils, Context context) {
        this.mDBUtils = dBUtils;
        this.mContext = context;
    }

    public static BirthdayDao getInstance(Context context) {
        if (mBirthdayDao == null) {
            DBUtils dbUtils = DBUtils.getInstance(context);
            mBirthdayDao = new BirthdayDao(dbUtils, context);
        }
        return mBirthdayDao;
    }

    public void add(Birthday birthday) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("when_", birthday.getWhen());
        values.put("who", birthday.getWho());
        values.put("isLunar", birthday.isLunar());
        database.insert(BIRTHDAY, null, values);
    }

    public void delete(int id) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(BIRTHDAY, "id = ?", new String[]{id + ""});
    }

    public void update(Birthday birthday) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("when_", birthday.getWhen());
        values.put("who", birthday.getWho());
        values.put("isLunar", birthday.isLunar());
        database.update(BIRTHDAY, values, "id = ?", new String[]{birthday.getId() + ""});
    }

    public List<Birthday> queryAll() {

        SQLiteDatabase database = mDBUtils.getReadableDatabase();
        Cursor cursor = database.query(BIRTHDAY, null, null, null, null, null, null);
        List<Birthday> birthdays = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String who = cursor.getString(1);
            String when = cursor.getString(2);
            int isLunar = cursor.getInt(3);
            Birthday birthday = new Birthday();
            birthday.setId(id);
            birthday.setWhen(when);
            birthday.setWho(who);
            birthday.setLunar(isLunar == 1);
            birthdays.add(birthday);
        }
        return birthdays;
    }
}
