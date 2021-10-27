package org.windhoverlabs.data.yamcs;

import java.util.List;

import org.windhoverlabs.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayDouble;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import org.windhoverlabs.yamcs.studio.data.vtype.ListDouble;
import org.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import org.windhoverlabs.yamcs.studio.data.vtype.VDoubleArray;
import org.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import org.windhoverlabs.yamcs.studio.data.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class DoubleArrayVType extends YamcsVType implements VDoubleArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ArrayDouble data;

    public DoubleArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        double[] doubleValues = new double[size];
        for (int i = 0; i < doubleValues.length; i++) {
            doubleValues[i] = value.getArrayValue(i).getDoubleValue();
        }
        data = new ArrayDouble(doubleValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListDouble getData() {
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
