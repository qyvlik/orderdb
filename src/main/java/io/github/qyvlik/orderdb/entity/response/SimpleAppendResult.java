package io.github.qyvlik.orderdb.entity.response;

import java.io.Serializable;

public class SimpleAppendResult implements Serializable {
    private String group;
    private String key;
    private Long index;

    public SimpleAppendResult(String group, String key, Long index) {
        this.group = group;
        this.key = key;
        this.index = index;
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
}
