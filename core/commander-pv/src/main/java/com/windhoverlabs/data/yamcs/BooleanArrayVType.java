package com.windhoverlabs.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;

import com.windhoverlabs.yamcs.studio.data.vtype.ArrayBoolean;
import com.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import com.windhoverlabs.yamcs.studio.data.vtype.ListBoolean;
import com.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import com.windhoverlabs.yamcs.studio.data.vtype.VBooleanArray;
import com.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;

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
