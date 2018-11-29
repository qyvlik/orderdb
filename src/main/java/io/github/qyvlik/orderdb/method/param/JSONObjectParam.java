package io.github.qyvlik.orderdb.method.param;

import com.alibaba.fastjson.JSONObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParam;

import java.util.Map;

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
