package io.github.qyvlik.orderdb.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class QueueUpRecord implements Serializable {
    @JSONField(name = "g")
    private String group;

    @JSONField(name = "k")
    private String key;

    @JSONField(name = "ki")
    private Long index;

    @JSONField(name = "d")
    private Object data;

    public QueueUpRecord() {

    }

    public QueueUpRecord(String group, String key, Long index, Object data) {
        this.group = group;
        this.key = key;
        this.index = index;
        this.data = data;
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

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "QueueUpRecord{" +
                "group='" + group + '\'' +
                ", key='" + key + '\'' +
                ", index=" + index +
                ", data=" + data +
                '}';
    }
}
