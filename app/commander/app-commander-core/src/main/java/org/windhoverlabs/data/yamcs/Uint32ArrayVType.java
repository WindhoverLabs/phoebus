package org.windhoverlabs.data.yamcs;

import java.util.List;

import org.windhoverlabs.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import org.windhoverlabs.yamcs.studio.data.vtype.ArrayLong;
import org.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import org.windhoverlabs.yamcs.studio.data.vtype.ListLong;
import org.windhoverlabs.yamcs.studio.data.vtype.VLongArray;
import org.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import org.windhoverlabs.yamcs.studio.data.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Uint32ArrayVType extends YamcsVType implements VLongArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ListLong data;

    public Uint32ArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        long[] longValues = new long[size];
        for (int i = 0; i < longValues.length; i++) {
            longValues[i] = value.getArrayValue(i).getUint32Value() & 0xFFFFFFFFL;
        }
        data = new ArrayLong(longValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListLong getData() {
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
