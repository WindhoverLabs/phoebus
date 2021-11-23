package com.windhoverlabs.data.yamcs;

import java.util.List;

import org.yamcs.protobuf.Pvalue.ParameterValue;

import com.windhoverlabs.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import com.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import com.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import com.windhoverlabs.yamcs.studio.data.vtype.VIntArray;
import com.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import com.windhoverlabs.yamcs.studio.data.vtype.ValueUtil;

public class Sint32ArrayVType extends YamcsVType implements VIntArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ArrayInt data;

    public Sint32ArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        int[] intValues = new int[size];
        for (int i = 0; i < intValues.length; i++) {
            intValues[i] = value.getArrayValue(i).getSint32Value();
        }
        data = new ArrayInt(intValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListInt getData() {
        return data;
    }

    @Override
    public List<ArrayDimensionDisplay> getDimensionDisplay() {
        return dimensionDisplay;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
