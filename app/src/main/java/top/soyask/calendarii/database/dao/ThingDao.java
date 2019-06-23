package top.soyask.calendarii.database.dao;

import android.content.ContentValues;
import android.content.Context;

import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.ui.eventbus.Messages;
import top.soyask.calendarii.utils.EventBusDefault;
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

    public List<Thing> listByDate(long date) {
        DBUtils.Query query = new DBUtils.Query()
                .setSelection("TARGET_TIME = ?", date)
                .setOrderBy("UPDATE_TIME desc");
        return mDbUtils.query(TABLE, Thing.class, query);
    }

    public List<Thing> list(int page) {
        int limit = (page - 1) * 25;
        DBUtils.Query query = new DBUtils.Query()
                .setLimit(String.format(Locale.getDefault(), "%d,25", limit))
                .setOrderBy("TARGET_TIME desc");
        return mDbUtils.query(TABLE, Thing.class, query);
    }

    public void insert(Thing thing) {
        ContentValues values = SqlGenerator.getContentValues(thing);
        mDbUtils.insert(TABLE, null, values);
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public void update(Thing thing) {
        ContentValues values = SqlGenerator.getContentValues(thing);
        mDbUtils.update(TABLE, values, "id = ?", thing.getId());
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public void delete(int id) {
        mDbUtils.delete(TABLE, "id = ?", id);
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public void delete(Thing thing) {
        mDbUtils.delete(TABLE, "id = ?", thing.getId());
        EventBusDefault.post(Messages.createUpdateDataMessage());
    }

    public int count() {
        return mDbUtils.count(TABLE);
    }
}
