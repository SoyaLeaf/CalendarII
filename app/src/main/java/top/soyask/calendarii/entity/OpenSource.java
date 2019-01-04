package top.soyask.calendarii.entity;

import java.io.Serializable;

/**
 * Created by mxf on 2017/10/29.
 */

public class OpenSource implements Serializable {

    private String title;
    private String detail;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
