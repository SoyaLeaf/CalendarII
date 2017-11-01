package top.soyask.calendarii.global;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Birthday;

/**
 * Created by mxf on 2017/11/1.
 */

public class GlobalData {

    public static final Map<String, List<Birthday>> BIRTHDAY = new HashMap<>();
    public static final List<String> HOLIDAY = new ArrayList<>();

    public synchronized static final void loadBirthday(Context context) {
        BirthdayDao birthdayDao = BirthdayDao.getInstance(context);
        List<Birthday> birthdays = birthdayDao.queryAll();
        BIRTHDAY.clear();
        for (Birthday birthday : birthdays) {
            String when = birthday.getWhen();
            if (BIRTHDAY.containsKey(when)) {
                BIRTHDAY.get(when).add(birthday);
            } else {
                List<Birthday> birthdayList = new ArrayList<>();
                birthdayList.add(birthday);
                BIRTHDAY.put(when, birthdayList);
            }
        }
        context.sendBroadcast(new Intent(EventDao.UPDATE));
    }

    private static final String URL = "http://owvj0u2dq.bkt.clouddn.com/holiday.json";

    public synchronized static final void loadHoliday(Context context){
        SharedPreferences setting = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        Set<String> holiday = setting.getStringSet(Global.SETTING_HOLIDAY, new HashSet<String>());
        GlobalData.HOLIDAY.addAll(holiday);
    }

    public synchronized static final void synHoliday(final LoadCallBack callBack) {
        new Thread() {
            @Override
            public void run() {
                try {
                    loadData(URL);
                    callBack.onSuccess();
                } catch (Exception e) {
                    callBack.onFail();
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public interface LoadCallBack {
        void onSuccess();
        void onFail();
    }

    private static void loadData(String url) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        String json = response.body().string();
        Gson gson = new Gson();
        List<String> list = gson.fromJson(json, new TypeToken<ArrayList<String>>() {
        }.getType());
        HOLIDAY.addAll(list);
    }
}
