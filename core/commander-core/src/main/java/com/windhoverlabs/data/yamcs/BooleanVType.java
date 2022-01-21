package com.windhoverlabs.data.yamcs;

import com.windhoverlabs.yamcs.studio.data.vtype.VBoolean;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class BooleanVType extends YamcsVType implements VBoolean {

  public BooleanVType(ParameterValue pval, boolean raw) {
    super(pval, raw);
  }

  @Override
  public Boolean getValue() {
    return value.getBooleanValue();
  }

  @Override
  public String toString() {
    return Boolean.toString(value.getBooleanValue());
  }
}
