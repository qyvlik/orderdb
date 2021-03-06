package io.github.qyvlik.orderdb.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class QueueUpBinlog implements Serializable {
    @JSONField(name = "bi")
    private Long binlogIndex;

    @JSONField(name = "a")
    private Action action;

    @JSONField(name = "s")
    private String scope;

    @JSONField(name = "k")
    private String key;

    @JSONField(name = "ki")
    private Long keyIndex;

    @JSONField(name = "d")
    private Object data;

    public QueueUpBinlog() {

    }

    public QueueUpBinlog(Long binlogIndex,
                         Action action,
                         String scope,
                         String key,
                         Long keyIndex,
                         Object data) {
        this.binlogIndex = binlogIndex;
        this.action = action;
        this.scope = scope;
        this.key = key;
        this.keyIndex = keyIndex;
        this.data = data;
    }

    public Long getBinlogIndex() {
        return binlogIndex;
    }

    public void setBinlogIndex(Long binlogIndex) {
        this.binlogIndex = binlogIndex;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(Long keyIndex) {
        this.keyIndex = keyIndex;
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
