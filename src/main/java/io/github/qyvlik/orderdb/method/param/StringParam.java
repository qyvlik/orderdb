package io.github.qyvlik.orderdb.method.param;


import io.github.qyvlik.jsonrpclite.core.jsonrpc.method.RpcParam;

public class StringParam extends RpcParam {

    public StringParam(String paramName) {
        this.setTypeName("string");
        this.setParamName(paramName);
    }

    @Override
    protected boolean canConvertInternal(Object param) {
        if (param == null) {
            return false;
        }
        if (!(param instanceof String)) {
            return false;
        }
        return true;
    }
}
