package com.windhoverlabs.data.yamcs;

import com.windhoverlabs.yamcs.studio.data.vtype.ArrayInt;
import com.windhoverlabs.yamcs.studio.data.vtype.ListInt;
import com.windhoverlabs.yamcs.studio.data.vtype.VStringArray;
import com.windhoverlabs.yamcs.studio.data.vtype.VTypeToString;
import java.util.ArrayList;
import java.util.List;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;

public class AggregateArrayVType extends YamcsVType implements VStringArray {

  private ListInt sizes;

  private List<String> data;

  public AggregateArrayVType(ParameterValue pval, boolean raw) {
    super(pval, raw);

    int size = value.getArrayValueCount();
    sizes = new ArrayInt(size);

    data = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Value aggregateValue = value.getArrayValue(i);
      data.add(StringConverter.toString(aggregateValue));
    }
  }

  @Override
  public ListInt getSizes() {
    return sizes;
  }

  @Override
  public List<String> getData() {
    return data;
  }

  @Override
  public String toString() {
    return VTypeToString.toString(this);
  }
}