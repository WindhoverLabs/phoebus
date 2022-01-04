package com.windhoverlabs.data.yamcs;

import com.windhoverlabs.yamcs.studio.data.vtype.VFloat;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class FloatVType extends YamcsVType implements VFloat {

  public FloatVType(ParameterValue pval, boolean raw) {
    super(pval, raw);
  }

  @Override
  public Float getValue() {
    return value.getFloatValue();
  }

  @Override
  public String toString() {
    return Float.toString(value.getFloatValue());
  }
}
