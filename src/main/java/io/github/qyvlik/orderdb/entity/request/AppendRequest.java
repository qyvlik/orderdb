package io.github.qyvlik.orderdb.entity.request;

import java.io.Serializable;

public class AppendRequest implements Serializable {
    private String group;
    private String key;
    private Object data;

    public AppendRequest() {

    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
