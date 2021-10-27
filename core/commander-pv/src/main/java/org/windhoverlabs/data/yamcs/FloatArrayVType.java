package org.windhoverlabs.data.yamcs;

import java.util.List;

import org.windhoverlabs.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayFloat;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import org.windhoverlabs.yamcs.studio.data.vtype.ListFloat;
import org.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import org.windhoverlabs.yamcs.studio.data.vtype.VFloatArray;
import org.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import org.windhoverlabs.yamcs.studio.data.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

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
