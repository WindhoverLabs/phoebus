package com.windhoverlabs.data.yamcs;

import com.windhoverlabs.yamcs.studio.data.vtype.VDouble;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class DoubleVType extends YamcsVType implements VDouble {

  public DoubleVType(ParameterValue pval, boolean raw) {
    super(pval, raw);
  }

  @Override
  public Double getValue() {
    return value.getDoubleValue();
  }

  @Override
  public String toString() {
    return Double.toString(value.getDoubleValue());
  }
}
