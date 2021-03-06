package io.github.qyvlik.orderdb.entity.request;

import java.io.Serializable;

public class AppendRequest implements Serializable {
    private String scope;
    private String key;
    private Object data;

    public AppendRequest() {

    }

    public AppendRequest(String scope, String key, Object data) {
        this.scope = scope;
        this.key = key;
        this.data = data;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
