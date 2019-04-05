package top.soyask.calendarii.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.utils.SqlGenerator;

public class ThingDao {

    public static final String TABLE = SqlGenerator.convertWordWithUnderline(Thing.class.getSimpleName());
    private DBUtils mDbUtils;

    private ThingDao(Context context) {
        this.mDbUtils = DBUtils.getInstance(context);
    }

    public static ThingDao getInstance(Context context) {
        return new ThingDao(context);
    }

    public List<Thing> listAll() {
        SQLiteDatabase database = mDbUtils.getReadableDatabase();
        Cursor cursor = database.query(TABLE, null, null, null, null, null, null);
        List<Thing> things = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Thing thing = DBUtils.cursor2Object(Thing.class, cursor);
            things.add(thing);
        }
        cursor.close();
        return things;
    }
}
