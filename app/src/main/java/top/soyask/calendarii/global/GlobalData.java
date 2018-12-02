package top.soyask.calendarii.global;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

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
import top.soyask.calendarii.entity.Birthday;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;

/**
 * Created by mxf on 2017/11/1.
 */

public class GlobalData {

    private static final String URL_HOLIDAY = "http://qiniu.soyask.top/holiday.json";
    private static final String URL_WORKDAY = "http://qiniu.soyask.top/workday.json";

    public static final Map<String, List<Birthday>> BIRTHDAY = new HashMap<>();
    public static final List<String> HOLIDAY = new ArrayList<>();
    /**
     * 调休的日子
     */
    public static final List<String> WORKDAY = new ArrayList<>();
    private static final String TAG = "GlobalData";

    public synchronized static void loadBirthday(Context context) {
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
        context.sendBroadcast(new Intent(MonthFragment.UPDATE_EVENT));
    }

    public synchronized static void loadHoliday(Context context) {
        SharedPreferences setting = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        Set<String> holiday = setting.getStringSet(Global.SETTING_HOLIDAY, new HashSet<>());
        GlobalData.HOLIDAY.addAll(holiday);
    }

    public synchronized static void loadWorkday(Context context) {
        SharedPreferences setting = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        Set<String> workday = setting.getStringSet(Global.SETTING_WORKDAY, new HashSet<>());
        GlobalData.WORKDAY.addAll(workday);
    }

    public synchronized static void synHoliday(final LoadCallBack callBack) {
        new Thread() {
            @Override
            public void run() {
                try {
                    loadData(URL_HOLIDAY, HOLIDAY);
                    loadData(URL_WORKDAY, WORKDAY);
                    callBack.onSuccess();
                } catch (Exception e) {
                    callBack.onFail(e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public interface LoadCallBack {
        void onSuccess();

        void onFail(String error);
    }

    private static void loadData(String url, List<String> container) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
        String json = response.body().string();
        Gson gson = new Gson();
        Log.i(TAG, "loadData: " + json);
        List<String> list = gson.fromJson(json, new TypeToken<ArrayList<String>>() {
        }.getType());
        container.clear();
        container.addAll(list);
    }
}
