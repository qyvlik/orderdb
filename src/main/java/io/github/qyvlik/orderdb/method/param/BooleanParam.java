package io.github.qyvlik.orderdb.method.param;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParam;

public class BooleanParam extends RpcParam {

    public BooleanParam(String paramName) {
        this.setTypeName("bool");
        this.setParamName(paramName);
    }

    @Override
    protected boolean canConvertInternal(Object param) {
        if (param == null) {
            return false;
        }
        if (!(param instanceof Boolean)) {
            return false;
        }
        return true;
    }
}
