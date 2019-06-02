package top.soyask.calendarii.entity;

import java.io.Serializable;

public class MemorialDay implements Serializable {
    
    private int id;
    private String who;
    private String name;
    private String details;
    private int year;
    private int month;
    private int day;
    private String lunar;
    private boolean isLunar;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean isLunar() {
        return isLunar;
    }

    public void setLunar(boolean isLunar) {
        this.isLunar = isLunar;
    }

    public String getLunar() {
        return lunar;
    }

    public void setLunar(String lunar) {
        this.lunar = lunar;
    }

    @Override
    public String toString() {
        return "MemorialDay{" +
                "id=" + id +
                ", who='" + who + '\'' +
                ", name='" + name + '\'' +
                ", details='" + details + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", lunar='" + lunar + '\'' +
                ", isLunar=" + isLunar +
                '}';
    }
}
