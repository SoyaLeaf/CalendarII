package top.soyask.calendarii.entity;

import java.io.Serializable;

/**
 * Created by mxf on 2017/10/29.
 */

public class Birthday implements Serializable {

    private int id;
    private String who;
    private String when;
    private boolean isLunar;


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public boolean isLunar() {
        return isLunar;
    }

    public void setLunar(boolean lunar) {
        isLunar = lunar;
    }

    @Override
    public String toString() {
        return "Birthday{" +
                "id=" + id +
                ", who='" + who + '\'' +
                ", when='" + when + '\'' +
                ", isLunar=" + isLunar +
                '}';
    }
}
