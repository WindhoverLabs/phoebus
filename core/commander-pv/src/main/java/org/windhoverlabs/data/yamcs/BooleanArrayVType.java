package org.windhoverlabs.data.yamcs;

import org.windhoverlabs.yamcs.studio.data.vtype.ArrayBoolean;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import org.windhoverlabs.yamcs.studio.data.vtype.ListBoolean;
import org.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import org.windhoverlabs.yamcs.studio.data.vtype.VBooleanArray;
import org.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class BooleanArrayVType extends YamcsVType implements VBooleanArray {

    private ListInt sizes;

    private ArrayBoolean data;

    public BooleanArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);

        boolean[] booleanValues = new boolean[size];
        for (int i = 0; i < booleanValues.length; i++) {
            booleanValues[i] = value.getArrayValue(i).getBooleanValue();
        }
        data = new ArrayBoolean(booleanValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListBoolean getData() {
        return data;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
