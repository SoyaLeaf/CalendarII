package top.soyask.calendarii.utils;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class SqlGenerator {

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
                sb.append(" PRIMARY KEY AUTOINCREMENT ");
            }
            sb.append(',');
        }
        if (fields.length > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(");");
        return sb.toString();
    }

    public static ContentValues getContentValues(Object obj) {
        try {
            return getContentValuesThrowException(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private static ContentValues getContentValuesThrowException(Object obj) throws IllegalAccessException {
        ContentValues values = new ContentValues();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("id".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(obj);
            String key = convertWordWithUnderline(field.getName());
            values.put(key, String.valueOf(value));
        }
        return values;
    }

    public static <T> T cursor2Object(Class<T> clazz, Cursor cursor) {
        try {
            return cursor2ObjectThrowException(clazz, cursor);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T> T cursor2ObjectThrowException(Class<T> clazz, Cursor cursor)
            throws InstantiationException, IllegalAccessException {
        T t = clazz.newInstance();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String columnName = convertWordWithUnderline(field.getName());
            field.setAccessible(true);
            int index = cursor.getColumnIndex(columnName);
            switch (field.getType().getSimpleName()) {
                case "String":
                    field.set(t, cursor.getString(index));
                    break;
                case "Integer":
                case "int":
                    field.set(t, cursor.getInt(index));
                    break;
                case "Boolean":
                case "boolean":
                    field.set(t, cursor.getInt(index) == 1);
                    break;
                case "Float":
                case "float":
                    field.set(t, cursor.getFloat(index));
                    break;
                case "Double":
                case "double":
                    field.set(t, cursor.getFloat(index));
            }
        }
        return t;
    }


    private static String convertWordWithUnderline(String fieldName) {
        String[] split = fieldName.split("(?=[A-Z])");
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : split) {
            stringBuilder.append(s);
            stringBuilder.append("_");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString().toUpperCase();
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
