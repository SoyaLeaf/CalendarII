package top.soyask.calendarii.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Backup;
import top.soyask.calendarii.domain.Birthday;
import top.soyask.calendarii.domain.Event;

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
        Log.i("xxx", "save: " + filename);
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

    public static Backup load(String file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)
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
        EventDao eventDao = EventDao.getInstance(context);
        List<Event> events = backup.getEvents();
        if (events != null) {
            for (Event event : events) {
                eventDao.add(event);
            }
        }
        BirthdayDao birthdayDao = BirthdayDao.getInstance(context);
        List<Birthday> birthdays = backup.getBirthdays();
        if (birthdays != null) {
            for (Birthday birthday : birthdays) {
                birthdayDao.add(birthday);
            }
        }
    }

}
