package org.windhoverlabs.data.yamcs;

import org.windhoverlabs.yamcs.studio.data.vtype.VString;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class AggregateVType extends YamcsVType implements VString {

    public AggregateVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public String getValue() {
        return StringConverter.toString(value);
    }

    @Override
    public String toString() {
        if (value.hasAggregateValue() || value.getAggregateValue() == null) {
            return "null";
        } else {
            return getValue();
        }
    }
}
