package top.soyask.calendarii.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.database.DBUtils;
import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.database.dao.MemorialDayDao;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Backup;
import top.soyask.calendarii.entity.Birthday;
import top.soyask.calendarii.entity.Event;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.ui.fragment.backup.BackupFragment;

public class BackupUtils {

    private static final Gson GSON = new GsonBuilder().create();

    public static String save(Backup backup, Context context) {
        String json = GSON.toJson(backup);
        byte[] bytes = new byte[0];
        try {
            bytes = json.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] data = Base64.encode(bytes, Base64.DEFAULT);
        File parent = Environment.getExternalStorageDirectory();
        if (parent == null) {
            parent = context.getCacheDir();
        }
        String filename = parent.getPath() + File.separatorChar + System.currentTimeMillis() + ".cdt";
        File file = new File(filename);
        try (FileOutputStream outputStream = new FileOutputStream(file);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
            bufferedOutputStream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return filename;
    }

    public static Backup load(ContentResolver resolver, Uri uri) {

        try (InputStream in = resolver.openInputStream(uri);
             BufferedInputStream bis = new BufferedInputStream(in)
        ) {
            byte[] data = new byte[bis.available()];
            bis.read(data);
            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            String json = new String(bytes, "gbk");
            return GSON.fromJson(json, Backup.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void insertBackupData(Backup backup, Context context) {
        if (backup.getVersion() == BackupFragment.BACKUP_VERSION) {
            List<Thing> things = backup.getThings();
            if(things != null){
                ThingDao thingDao = ThingDao.getInstance(context);
                for (Thing thing : things) {
                    thingDao.insert(thing);
                }
            }
            List<MemorialDay> memorialDays = backup.getMemorialDays();
            if(memorialDays != null){
                MemorialDayDao memorialDayDao = MemorialDayDao.getInstance(context);
                for (MemorialDay day : memorialDays) {
                    memorialDayDao.insert(day);
                }
            }
        } else {
            List<Event> events = backup.getEvents();
            if (events != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
                ThingDao thingDao = ThingDao.getInstance(context);
                for (Event event : events) {
                    Thing thing = new Thing();
                    thing.setType(event.getType());
                    thing.setDetail(event.getDetail());
                    thing.setDone(event.isComplete());
                    thing.setUpdateTime(System.currentTimeMillis());
                    String title = event.getTitle();
                    thing.setTargetTime(DBUtils.title2Time(format, title));
                    thingDao.insert(thing);
                }
            }
        }
    }

}
