package top.soyask.calendarii.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.database.dao.EventDao;

/**
 * Created by mxf on 2017/8/16.
 */
public class DBUtils extends SQLiteOpenHelper {


    private static DBUtils dbUtils;
    private static final String EVENT_SQL;
    private static final String BIRTH_SQL;

    static {
        EVENT_SQL = "create table " +
                EventDao.EVENT +
                "(" +
                "id Integer primary key autoincrement," +
                "title varchar(255)," +
                "detail text," +
                "isDelete boolean," +
                "isComplete boolean," +
                "type int" +
                ");";

        BIRTH_SQL = "create table " +
                BirthdayDao.BIRTHDAY +
                "(" +
                "id Integer primary key autoincrement," +
                "who varchar(255)," +
                "when_ varchar(255)," +
                "isLunar boolean" +
                ");";
    }

    private DBUtils(Context context) {
        super(context, "db", null, 3);
    }


    public static DBUtils getInstance(Context context) {
        if (dbUtils == null) {
            dbUtils = new DBUtils(context);
        }
        return dbUtils;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EVENT_SQL);
        db.execSQL(BIRTH_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(BIRTH_SQL);
            case 2:
                db.execSQL("alter table "+EventDao.EVENT+" add column type int");
                break;
        }
    }

}
