package top.soyask.calendarii.entity;

import java.io.Serializable;

public class Thing implements Serializable {

    private int id;
    private String detail;
    private int type;
    private boolean done;
    private long createTime;
    private long updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Thing{" +
                "id=" + id +
                ", detail='" + detail + '\'' +
                ", type=" + type +
                ", done=" + done +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
