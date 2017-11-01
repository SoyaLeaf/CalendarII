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
        EVENT_SQL = new StringBuffer()
                .append("create table ")
                .append(EventDao.EVENT)
                .append("(")
                .append("id Integer primary key autoincrement,")
                .append("title varchar(255),")
                .append("detail text,")
                .append("isDelete boolean,")
                .append("isComplete boolean")
                .append(");")
                .toString();

        BIRTH_SQL = new StringBuffer()
                .append("create table ")
                .append(BirthdayDao.BIRTHDAY)
                .append("(")
                .append("id Integer primary key autoincrement,")
                .append("who varchar(255),")
                .append("when_ varchar(255),")
                .append("isLunar boolean")
                .append(");")
                .toString();
    }

    public DBUtils(Context context) {
        super(context, "db", null, 2);
    }


    public static DBUtils getInstance(Context context){
        if(dbUtils == null){
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
                break;
        }
    }

}
