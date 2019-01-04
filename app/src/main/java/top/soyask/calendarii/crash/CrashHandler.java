package top.soyask.calendarii.crash;

import android.content.Context;
import android.os.Build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Arrays;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final CrashHandler HANDLER = new CrashHandler();

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private File mCache;

    public static CrashHandler getInstance() {
        return HANDLER;
    }

    public void init(Context context) {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mCache = context.getCacheDir();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringBuilder result = new StringBuilder();
        StackTraceElement[] trace = e.getStackTrace();
        result.append(t.getName()).append("\n");
        result.append(e.getMessage()).append("\n");
        for (StackTraceElement element : trace) {
            result.append(element.toString())
                    .append("\n");
        }
        result.append(getPhoneInfo());
        String info = result.toString();

        String filename = mCache.getPath() + File.separator + "crash";
        try (FileOutputStream fos = new FileOutputStream(filename);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))
        ) {
            writer.write(info);
            writer.flush();
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        }
    }

    private String getPhoneInfo() {
        StringBuilder builder = new StringBuilder("\n\n设备信息:\n");
        Field[] fields = Build.VERSION.class.getDeclaredFields();
        getFiledValue(builder, fields);
        return builder.toString();
    }

    private void getFiledValue(StringBuilder builder, Field[] fields) {
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object o = field.get(null);
                String val = o instanceof String[] ? Arrays.toString((String[]) o) : o.toString();
                builder.append(field.getName())
                        .append(":")
                        .append(val)
                        .append("\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
