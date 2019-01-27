package io.github.qyvlik.orderdb.entity.request;

import java.io.Serializable;
import java.util.List;

public class AppendListRequest implements Serializable {
    private String scope;
    private Boolean ignoreExist;
    private List<AppendRequest> list;

    public AppendListRequest() {

    }

    public AppendListRequest(String scope, Boolean ignoreExist, List<AppendRequest> list) {
        this.scope = scope;
        this.ignoreExist = ignoreExist;
        this.list = list;
    }

    public Boolean getIgnoreExist() {
        return ignoreExist;
    }

    public void setIgnoreExist(Boolean ignoreExist) {
        this.ignoreExist = ignoreExist;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<AppendRequest> getList() {
        return list;
    }

    public void setList(List<AppendRequest> list) {
        this.list = list;
    }
}
