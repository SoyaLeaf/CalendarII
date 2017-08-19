package top.soyask.calendarii.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import top.soyask.calendarii.database.dao.EventDao;

/**
 * Created by mxf on 2017/8/16.
 */
public class DBUtils extends SQLiteOpenHelper {


    private static DBUtils dbUtils;

    public DBUtils(Context context) {
        super(context, "db", null, 1);
    }

    public static DBUtils getInstance(Context context){
        if(dbUtils == null){
            dbUtils = new DBUtils(context);
        }
        return dbUtils;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = new StringBuffer()
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
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
