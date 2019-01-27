package io.github.qyvlik.orderdb.entity.response;

import java.io.Serializable;

public class SimpleAppendResult implements Serializable {
    private String scope;
    private String key;
    private Long index;

    public SimpleAppendResult(String scope, String key, Long index) {
        this.scope = scope;
        this.key = key;
        this.index = index;
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

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }
}
