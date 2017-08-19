package top.soyask.calendarii.domain;

import java.io.Serializable;

/**
 * Created by mxf on 2017/8/16.
 */
public class Event implements Serializable {

    private int id;
    private String title;
    private String detail;
    private boolean isDelete;
    private boolean isComplete;

    public Event(String title, String detail) {
        this.detail = detail;
        this.title = title;
    }

    public Event() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
