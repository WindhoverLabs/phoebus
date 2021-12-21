package com.windhoverlabs.data.yamcs;

import com.windhoverlabs.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import com.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import com.windhoverlabs.yamcs.studio.data.vtype.ArrayLong;
import com.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import com.windhoverlabs.yamcs.studio.data.vtype.ListLong;
import com.windhoverlabs.yamcs.studio.data.vtype.VLongArray;
import com.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import com.windhoverlabs.yamcs.studio.data.vtype.ValueUtil;
import java.util.List;
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
