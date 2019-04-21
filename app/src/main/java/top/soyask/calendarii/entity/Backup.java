package top.soyask.calendarii.entity;

import java.util.List;

public class Backup {

    private int version;
    private List<Birthday> birthdays; // todo 等纪念日等完成之后再处理备份问题
    private List<Event> events;

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setBirthdays(List<Birthday> birthdays) {
        this.birthdays = birthdays;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Birthday> getBirthdays() {
        return birthdays;
    }

    public List<Event> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        return "Backup{" +
                "version=" + version +
                ", birthdays=" + birthdays +
                ", events=" + events +
                '}';
    }
}
