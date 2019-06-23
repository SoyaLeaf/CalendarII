package top.soyask.calendarii.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.entity.Event;

/**
 * Created by mxf on 2017/8/16.
 */
public class EventDao {
    public static final String TABLE = "EVENT";

    private DBUtils mDBUtils;
    private static EventDao mEventDao;

    private EventDao(DBUtils dBUtils) {
        this.mDBUtils = dBUtils;
    }

    public static EventDao getInstance(Context context) {
        if (mEventDao == null) {
            DBUtils dbUtils = DBUtils.getInstance(context);
            mEventDao = new EventDao(dbUtils);
        }
        return mEventDao;
    }

    public void add(Event event) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", event.getTitle());
        values.put("detail", event.getDetail());
        values.put("isDelete", event.isDelete());
        values.put("isComplete", event.isComplete());
        values.put("type", event.getType());
        database.insert(TABLE, null, values);
        database.close();
    }

    public void update(Event event) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", event.getTitle());
        values.put("detail", event.getDetail());
        values.put("isDelete", event.isDelete());
        values.put("isComplete", event.isComplete());
        values.put("type", event.getType());
        database.update(TABLE, values, "id = ?", new String[]{String.valueOf(event.getId())});
        database.close();
    }

    public void delete(Event event) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(TABLE, "id = ?", new String[]{String.valueOf(event.getId())});
        database.close();
    }

    public void delete(String title) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(TABLE, "title = ?", new String[]{title});
        database.close();
    }

    public void deleteAll() {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(TABLE, null, null);
        database.close();
    }


    public List<Event> queryAll() {
        SQLiteDatabase database = mDBUtils.getReadableDatabase();
        Cursor cursor = database.query(TABLE, null, null, null, null, null, "title");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            Event event = map2Event(cursor);
            events.add(event);
        }
        cursor.close();
        database.close();
        return events;
    }

    @NonNull
    public static Event map2Event(Cursor cursor) {
        int id = cursor.getInt(0);
        String title = cursor.getString(1);
        String detail = cursor.getString(2);
        int type = cursor.getInt(5);
        Event event = new Event(title, detail, type);
        event.setId(id);
        event.setDelete(cursor.getInt(3) == 1);
        event.setComplete(cursor.getInt(4) == 1);
        return event;
    }


    public List<Event> query(String title) {
        return Collections.emptyList();
    }

    /**
     * 取消了回收站这个东西
     */
    @Deprecated
    public List<Event> query(boolean delete) {
        SQLiteDatabase database = mDBUtils.getReadableDatabase();
        Cursor cursor = database.query(TABLE, null, "isDelete = ?", new String[]{String.valueOf(delete)}, null, null, "title");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            Event event = map2Event(cursor);
            events.add(event);
        }
        cursor.close();
        database.close();
        return events;
    }
}
