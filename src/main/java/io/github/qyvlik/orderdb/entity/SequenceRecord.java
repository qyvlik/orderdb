package io.github.qyvlik.orderdb.entity;

import java.io.Serializable;

public class SequenceRecord implements Serializable {
    private String group;
    private Long sequenceId;
    private String uniqueKey;
    private Object data;

    public SequenceRecord() {

    }

    public SequenceRecord(String group, Long sequenceId, String uniqueKey, Object data) {
        this.group = group;
        this.sequenceId = sequenceId;
        this.uniqueKey = uniqueKey;
        this.data = data;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
