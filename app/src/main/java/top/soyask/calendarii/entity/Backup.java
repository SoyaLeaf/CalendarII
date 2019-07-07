package top.soyask.calendarii.entity;

import java.util.List;

public class Backup {

    private int version;
    private List<Birthday> birthdays;
    private List<Event> events;
    private List<MemorialDay> memorialDays;
    private List<Thing> things;

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

    public void setThings(List<Thing> things) {
        this.things = things;
    }

    public List<Thing> getThings() {
        return things;
    }

    public void setMemorialDays(List<MemorialDay> memorialDays) {
        this.memorialDays = memorialDays;
    }

    public List<MemorialDay> getMemorialDays() {
        return memorialDays;
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
