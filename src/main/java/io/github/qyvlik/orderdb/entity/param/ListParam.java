package io.github.qyvlik.orderdb.entity.param;

import com.alibaba.fastjson.JSONArray;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParam;

public class ListParam extends RpcParam {
    public ListParam(String paramName) {
        this.setTypeName("list");
        this.setParamName(paramName);
    }

    @Override
    protected boolean canConvertInternal(Object param) {
        if (param == null) {
            return false;
        }
        return param instanceof JSONArray;
    }
}
