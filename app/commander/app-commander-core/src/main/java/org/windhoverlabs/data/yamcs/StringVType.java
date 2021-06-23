package org.windhoverlabs.data.yamcs;

import org.windhoverlabs.yamcs.studio.data.vtype.VString;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class StringVType extends YamcsVType implements VString {

    public StringVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public String getValue() {
        return value.getStringValue();
    }

    @Override
    public String toString() {
        // Use String.valueOf, because it formats a nice "null" string
        // in case it is null
        return String.valueOf(value.getStringValue());
    }
}
