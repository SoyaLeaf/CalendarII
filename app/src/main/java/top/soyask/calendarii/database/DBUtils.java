package top.soyask.calendarii.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.utils.SqlGenerator;

/**
 * Created by mxf on 2017/8/16.
 */
public class DBUtils extends SQLiteOpenHelper {


    private static DBUtils dbUtils;
    private static final String EVENT_SQL;
    private static final String BIRTH_SQL;

    static {
        EVENT_SQL = "create table " +
                EventDao.TABLE +
                "(" +
                "id Integer primary key autoincrement," +
                "title varchar(255)," +
                "detail text," +
                "isDelete boolean," +
                "isComplete boolean," +
                "type int" +
                ");";

        BIRTH_SQL = "create table " +
                BirthdayDao.TABLE +
                "(" +
                "id Integer primary key autoincrement," +
                "who varchar(255)," +
                "when_ varchar(255)," +
                "isLunar boolean" +
                ");";
    }

    private DBUtils(Context context) {
        super(context, "db", null, 4);
    }


    public static DBUtils getInstance(Context context) {
        if (dbUtils == null) {
            dbUtils = new DBUtils(context);
        }
        return dbUtils;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(EVENT_SQL);
//        db.execSQL(BIRTH_SQL);
        db.execSQL(SqlGenerator.createTableSQL(Thing.class));
        db.execSQL(SqlGenerator.createTableSQL(MemorialDay.class));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 4) {
            String sql = SqlGenerator.createTableSQL(Thing.class);
            db.execSQL(sql);
            sql = SqlGenerator.createTableSQL(MemorialDay.class);
            db.execSQL(sql);
        }
        switch (oldVersion) {
            case 1:
            case 2:
            case 3:
                migrateEvent2Thing(db);
                dropOldTable(db);
                break;
        }

    }



    private void migrateEvent2Thing(SQLiteDatabase db) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月DD日", Locale.CHINA);
        Cursor cursor = db.query(EventDao.TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String detail = cursor.getString(cursor.getColumnIndex("detail"));
            int typeIndex = cursor.getColumnIndex("type");
            int type = typeIndex == -1 ? 0 : cursor.getInt(typeIndex);
            boolean complete = cursor.getInt(cursor.getColumnIndex("isComplete")) == 1;
            Thing thing = new Thing();
            thing.setDetail(detail);
            thing.setType(type);
            thing.setDone(complete);
            thing.setUpdateTime(System.currentTimeMillis());
            thing.setCreateTime(title2Time(format, title));
            ContentValues values = SqlGenerator.getContentValues(thing);
            db.insert(ThingDao.TABLE, null, values);
        }
        cursor.close();
    }

    private void dropOldTable(SQLiteDatabase db) {
        db.execSQL("drop table event");
        db.execSQL("drop table birthday");
    }

    private long title2Time(SimpleDateFormat format, String title) {
        try {
            return format.parse(title).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    public static <T> T cursor2Object(Class<T> clazz, Cursor cursor) {
        try {
            return cursor2ObjectThrowException(clazz, cursor);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T> T cursor2ObjectThrowException(Class<T> clazz, Cursor cursor)
            throws InstantiationException, IllegalAccessException {
        T t = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String columnName = SqlGenerator.convertWordWithUnderline(field.getName());
            field.setAccessible(true);
            int index = cursor.getColumnIndex(columnName);
            switch (field.getType().getSimpleName()) {
                case "String":
                    field.set(t, cursor.getString(index));
                    break;
                case "Integer":
                case "int":
                    field.set(t, cursor.getInt(index));
                    break;
                case "Long":
                case "long":
                    field.set(t, cursor.getLong(index));
                    break;
                case "Boolean":
                case "boolean":
                    field.set(t, cursor.getInt(index) == 1);
                    break;
                case "Float":
                case "float":
                    field.set(t, cursor.getFloat(index));
                    break;
                case "Double":
                case "double":
                    field.set(t, cursor.getFloat(index));
            }
        }
        return t;
    }
}
