package top.soyask.calendarii;

import android.content.ContentValues;

import org.junit.Test;

import top.soyask.calendarii.entity.Event;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.utils.SqlGenerator;

public class SqlGeneratorTest {

    @Test
    public void testGenerate() {
        String sql = SqlGenerator.createTableSQL(Thing.class);
        System.out.println(sql);
        sql = SqlGenerator.createTableSQL(MemorialDay.class);
        System.out.println(sql);

    }
}
