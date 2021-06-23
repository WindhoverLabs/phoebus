package org.windhoverlabs.data.yamcs;

import org.windhoverlabs.yamcs.studio.data.vtype.VInt;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Sint32VType extends YamcsVType implements VInt {

    public Sint32VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Integer getValue() {
        return value.getSint32Value();
    }

    @Override
    public String toString() {
        return String.valueOf(value.getSint32Value());
    }
}
