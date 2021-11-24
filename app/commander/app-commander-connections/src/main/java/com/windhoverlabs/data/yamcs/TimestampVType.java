package com.windhoverlabs.data.yamcs;

import java.time.Instant;
import java.util.Date;

import org.yamcs.protobuf.Pvalue.ParameterValue;

import com.windhoverlabs.yamcs.studio.data.vtype.VTimestamp;

public class TimestampVType extends YamcsVType implements VTimestamp {

    public TimestampVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Date getValue() {
        String stringValue = value.getStringValue();
        return Date.from(Instant.parse(stringValue));
    }

    @Override
    public String toString() {
        String stringValue = value.getStringValue();
        Instant instant = Instant.parse(stringValue);
        return com.windhoverlabs.pv.yamcs.YamcsPlugin.getDefault().formatInstant(instant);
    }
}
