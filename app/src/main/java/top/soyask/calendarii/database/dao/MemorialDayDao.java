package top.soyask.calendarii.database.dao;

import android.content.ContentValues;
import android.content.Context;

import java.util.List;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.ui.eventbus.Messages;
import top.soyask.calendarii.utils.EventBusDefault;
import top.soyask.calendarii.utils.SqlGenerator;

public class MemorialDayDao {

    private static final String TABLE = SqlGenerator.convertWordWithUnderline(MemorialDay.class.getSimpleName());

    private DBUtils mDbUtils;

    private MemorialDayDao(Context context) {
        this.mDbUtils = DBUtils.getInstance(context);
    }

    public static MemorialDayDao getInstance(Context context) {
        return new MemorialDayDao(context);
    }

    public List<MemorialDay> list() {
        DBUtils.Query query = new DBUtils.Query();
        return mDbUtils.query(TABLE, MemorialDay.class, query);
    }

    public void insert(MemorialDay memorialDay) {
        ContentValues values = SqlGenerator.getContentValues(memorialDay);
        mDbUtils.insert(TABLE, null, values);
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public void update(MemorialDay memorialDay) {
        ContentValues values = SqlGenerator.getContentValues(memorialDay);
        mDbUtils.update(TABLE, values, "id = ?", memorialDay.getId());
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public void delete(int id) {
        mDbUtils.delete(TABLE, "id = ?", id);
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public void delete(MemorialDay memorialDay) {
        mDbUtils.delete(TABLE, "id = ?", memorialDay.getId());
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public int count() {
        return mDbUtils.count(TABLE);
    }

    public List<MemorialDay> findMemorialDays(String lunar) {
        DBUtils.Query query = new DBUtils.Query().setSelection("IS_LUNAR == 1 AND LUNAR = ?", lunar);
        return mDbUtils.query(TABLE, MemorialDay.class, query);
    }

    public List<MemorialDay> findMemorialDays(int month, int day) {
        DBUtils.Query query = new DBUtils.Query()
                .setSelection("IS_LUNAR == 0 AND MONTH = ? AND DAY = ? ", month, day);
        return mDbUtils.query(TABLE, MemorialDay.class, query);
    }
}
