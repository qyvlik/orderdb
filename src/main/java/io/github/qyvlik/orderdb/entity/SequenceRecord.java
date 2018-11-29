package io.github.qyvlik.orderdb.entity;

import java.io.Serializable;

public class SequenceRecord implements Serializable {
    private Long sequenceId;
    private String uniqueKey;
    private Object data;

    public SequenceRecord() {

    }

    public SequenceRecord(Long sequenceId, String uniqueKey, Object data) {
        this.sequenceId = sequenceId;
        this.uniqueKey = uniqueKey;
        this.data = data;
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
