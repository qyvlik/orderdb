package io.github.qyvlik.orderdb.entity;

import java.io.Serializable;
import java.util.List;

public class AppendListRequest implements Serializable {
    private String group;
    private Boolean ignoreExist;
    private List<AppendRequest> list;

    public AppendListRequest() {

    }

    public AppendListRequest(String group, Boolean ignoreExist, List<AppendRequest> list) {
        this.group = group;
        this.ignoreExist = ignoreExist;
        this.list = list;
    }

    public Boolean getIgnoreExist() {
        return ignoreExist;
    }

    public void setIgnoreExist(Boolean ignoreExist) {
        this.ignoreExist = ignoreExist;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<AppendRequest> getList() {
        return list;
    }

    public void setList(List<AppendRequest> list) {
        this.list = list;
    }
}
