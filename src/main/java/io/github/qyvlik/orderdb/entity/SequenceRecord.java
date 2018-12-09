package io.github.qyvlik.orderdb.entity;

import java.io.Serializable;

public class SequenceRecord implements Serializable {
    private String group;
    private Long seq;
    private String key;
    private Object data;

    public SequenceRecord() {

    }

    public SequenceRecord(String group, Long seq, String key, Object data) {
        this.group = group;
        this.seq = seq;
        this.key = key;
        this.data = data;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
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
