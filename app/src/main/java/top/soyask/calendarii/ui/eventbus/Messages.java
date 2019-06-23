package top.soyask.calendarii.ui.eventbus;

public class Messages {
    public static class SelectedMessage {
        public final int year;
        public final int month;

        SelectedMessage(int year, int month) {
            this.year = year;
            this.month = month;
        }
    }

    public static class SkipMessage {
        public final int year;
        public final int month;
        public final int day;

        SkipMessage(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

    public static class UpdateDataMessage {
    }

    public static class UpdateUIMessage {
    }

    public static class WeekSettingMessage {

    }

    public static SelectedMessage createSelectedMessage(int year, int month, int day) {
        return new SelectedMessage(year, month);
    }

    public static SkipMessage createSkipMessage(int year, int month, int day) {
        return new SkipMessage(year, month, day);
    }

    public static UpdateDataMessage createUpdateDataMessage() {
        return new UpdateDataMessage();
    }

    public static UpdateUIMessage createUpdateUIMessage() {
        return new UpdateUIMessage();
    }

    public static WeekSettingMessage createWeekSettingMessage() {
        return new WeekSettingMessage();
    }
}
