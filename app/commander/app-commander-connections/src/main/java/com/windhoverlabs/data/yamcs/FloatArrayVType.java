package com.windhoverlabs.data.yamcs;

import java.util.List;

import org.yamcs.protobuf.Pvalue.ParameterValue;

import com.windhoverlabs.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import com.windhoverlabs.yamcs.studio.data.vtype.ArrayFloat;
import com.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import com.windhoverlabs.yamcs.studio.data.vtype.ListFloat;
import com.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import com.windhoverlabs.yamcs.studio.data.vtype.VFloatArray;
import com.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import com.windhoverlabs.yamcs.studio.data.vtype.ValueUtil;

public class FloatArrayVType extends YamcsVType implements VFloatArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ListFloat data;

    public FloatArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        float[] floatValues = new float[size];
        for (int i = 0; i < floatValues.length; i++) {
            floatValues[i] = value.getArrayValue(i).getFloatValue();
        }
        data = new ArrayFloat(floatValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListFloat getData() {
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
