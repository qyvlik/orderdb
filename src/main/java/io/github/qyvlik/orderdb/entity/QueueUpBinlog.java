package io.github.qyvlik.orderdb.entity;

import java.io.Serializable;

public class QueueUpBinlog implements Serializable {
    private Long index;
    private Action action;
    private String group;
    private String key;
    private Object data;

    public QueueUpBinlog() {

    }

    public QueueUpBinlog(Long index,
                         Action action,
                         String group,
                         String key,
                         Object data) {
        this.index = index;
        this.action = action;
        this.group = group;
        this.key = key;
        this.data = data;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
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

    public enum Action {
        append,
        delete
    }
}
