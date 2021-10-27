package org.windhoverlabs.data.yamcs;

import org.windhoverlabs.yamcs.studio.data.vtype.VLong;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Sint64VType extends YamcsVType implements VLong {

    public Sint64VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Long getValue() {
        return value.getSint64Value();
    }

    @Override
    public String toString() {
        return String.valueOf(value.getSint64Value());
    }
}
