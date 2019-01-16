package io.github.qyvlik.orderdb.entity.param;

import com.alibaba.fastjson.JSONObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParam;

public class JSONObjectParam extends RpcParam {
    public JSONObjectParam(String paramName) {
        this.setTypeName("object");
        this.setParamName(paramName);
    }

    @Override
    protected boolean canConvertInternal(Object param) {
        if (param == null) {
            return false;
        }
        return param instanceof JSONObject;
    }
}
