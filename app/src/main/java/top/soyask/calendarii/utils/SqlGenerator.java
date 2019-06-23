package top.soyask.calendarii.utils;

import android.content.ContentValues;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class SqlGenerator {
    private static final Pattern PATTERN = Pattern.compile("(?=[A-Z])");

    public static String createTableSQL(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(convertWordWithUnderline(clazz.getSimpleName()));
        sb.append("(");
        for (Field field : fields) {
            field.setAccessible(true);
            Ignore ignore = field.getAnnotation(Ignore.class);
            if (ignore != null) {
                continue;
            }
            String fieldName = field.getName();
            sb.append(convertWordWithUnderline(fieldName));
            sb.append(" ");
            sb.append(type(field));
            if (fieldName.toUpperCase().equals("ID")) {
                sb.append(" PRIMARY key AUTOINCREMENT ");
            }
            sb.append(',');
        }
        if (fields.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(");");
        return sb.toString();
    }

    private static String type(Field field) {
        String ct = getColumnType(field);
        if (ct != null) {
            return ct;
        }
        Class<?> type = field.getType();
        switch (type.getSimpleName()) {
            case "String":
                return "VARCHAR(255)";
            case "int":
            case "Integer":
                return "INTEGER";
            case "long":
            case "Long":
                return "LONG";
            case "Boolean":
            case "boolean":
                return "TINYINT(1)";
            case "Double":
            case "double":
                return "DOUBLE";
            case "Float":
            case "float":
                return "FLOAT";
        }
        throw new RuntimeException("未知类型");
    }

    private static String getColumnType(Field field) {
        ColumnType columnType = field.getAnnotation(ColumnType.class);
        if (columnType != null) {
            String ct = columnType.value();
            if (!"".equals(ct)) {
                return ct;
            }
        }
        return null;
    }

    public static String convertWordWithUnderline(String fieldName) {
        String[] split = PATTERN.split(fieldName);
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : split) {
            if (s != null && !s.isEmpty()) {
                stringBuilder.append(s);
                stringBuilder.append("_");
            }
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static ContentValues getContentValues(Object obj) {
        try {
            return getContentValuesThrowException(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private static ContentValues getContentValuesThrowException(Object obj)
            throws IllegalAccessException {
        ContentValues values = new ContentValues();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            String fieldName = field.getName();
            if ("id".equals(fieldName) && "0".equals(String.valueOf(value))) {
                continue;
            }
            String key = convertWordWithUnderline(fieldName);
            if (Boolean.TRUE.equals(value) || Boolean.FALSE.equals(value)) {
                value = (Boolean) value ? 1 : 0;
            }
            values.put(key, String.valueOf(value));
        }
        return values;
    }


    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Id {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ignore {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ColumnType {
        String value();
    }
}
