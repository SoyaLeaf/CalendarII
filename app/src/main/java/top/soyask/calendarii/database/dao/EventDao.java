package top.soyask.calendarii.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.entity.Event;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.widget.WidgetManager;

/**
 * Created by mxf on 2017/8/16.
 */
public class EventDao {
    public static final String EVENT = "EVENT";

    private DBUtils mDBUtils;
    private WeakReference<Context> mContext;
    private static EventDao mEventDao;

    private EventDao(DBUtils dBUtils, Context context) {
        this.mDBUtils = dBUtils;
        this.mContext = new WeakReference<>(context);
    }

    public static EventDao getInstance(Context context) {
        if (mEventDao == null) {
            DBUtils dbUtils = DBUtils.getInstance(context);
            mEventDao = new EventDao(dbUtils, context);
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
        database.insert(EVENT, null, values);
        database.close();
        sendBroadcast(MonthFragment.ADD_EVENT);
    }

    private void sendBroadcast(String action) {
        Context context = mContext.get();
        if (context != null) {
            Intent intent = new Intent();
            intent.setAction(action);
            context.sendBroadcast(intent);
            WidgetManager.updateAllWidget(context);
        }
    }

    public void update(Event event) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", event.getTitle());
        values.put("detail", event.getDetail());
        values.put("isDelete", event.isDelete());
        values.put("isComplete", event.isComplete());
        values.put("type", event.getType());
        database.update(EVENT, values, "id = ?", new String[]{String.valueOf(event.getId())});
        database.close();
        sendBroadcast(MonthFragment.UPDATE_EVENT);
    }

    public void delete(Event event) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(EVENT, "id = ?", new String[]{String.valueOf(event.getId())});
        database.close();
        sendBroadcast(MonthFragment.DELETE_EVENT);
    }

    public void delete(String title) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(EVENT, "title = ?", new String[]{title});
        database.close();
        sendBroadcast(MonthFragment.DELETE_EVENT);
    }

    public void deleteAll() {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(EVENT, null, null);
        database.close();
        sendBroadcast(MonthFragment.DELETE_EVENT);
    }

    public void deleteComplete() {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(EVENT, "isComplete = ?", new String[]{String.valueOf(1)});
        database.close();
        sendBroadcast(MonthFragment.DELETE_EVENT);
    }

    public void deleteComplete(String title) {
        SQLiteDatabase database = mDBUtils.getWritableDatabase();
        database.delete(EVENT, "isComplete = ? and title = ?", new String[]{String.valueOf(1), title});
        database.close();
        sendBroadcast(MonthFragment.DELETE_EVENT);
    }


    public List<Event> queryAll() {
        SQLiteDatabase database = mDBUtils.getReadableDatabase();
        Cursor cursor = database.query(EVENT, null, null, null, null, null, "title");
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
    private Event map2Event(Cursor cursor) {
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
        synchronized (EventDao.this) {
            try {
                return queryByTitle(title);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }

    @NonNull
    private List<Event> queryByTitle(String title) {
        SQLiteDatabase database = mDBUtils.getReadableDatabase();
        Cursor cursor = database.query(EVENT, null, "title = ?", new String[]{title}, null, null, "title");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            Event event = map2Event(cursor);
            events.add(event);
        }
        cursor.close();
        database.close();
        return events;
    }

    /**
     * 取消了回收站这个东西
     */
    @Deprecated
    public List<Event> query(boolean delete) {
        SQLiteDatabase database = mDBUtils.getReadableDatabase();
        Cursor cursor = database.query(EVENT, null, "isDelete = ?", new String[]{String.valueOf(delete)}, null, null, "title");
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
