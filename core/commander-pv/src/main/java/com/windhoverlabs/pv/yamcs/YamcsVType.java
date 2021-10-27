package com.windhoverlabs.pv.yamcs;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.List;

import org.yamcs.protobuf.Mdb.AlarmLevelType;
import org.yamcs.protobuf.Mdb.AlarmRange;
import org.yamcs.protobuf.Pvalue.AcquisitionStatus;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;

import com.windhoverlabs.commander.core.MissionDatabase;
import com.windhoverlabs.data.yamcs.AggregateArrayVType;
import com.windhoverlabs.data.yamcs.AggregateVType;
import com.windhoverlabs.data.yamcs.ArrayArrayVType;
import com.windhoverlabs.data.yamcs.BinaryVType;
import com.windhoverlabs.data.yamcs.BooleanArrayVType;
import com.windhoverlabs.data.yamcs.BooleanVType;
import com.windhoverlabs.data.yamcs.DoubleArrayVType;
import com.windhoverlabs.data.yamcs.DoubleVType;
import com.windhoverlabs.data.yamcs.EnumeratedArrayVType;
import com.windhoverlabs.data.yamcs.EnumeratedVType;
import com.windhoverlabs.data.yamcs.FloatArrayVType;
import com.windhoverlabs.data.yamcs.FloatVType;
import com.windhoverlabs.data.yamcs.Sint32ArrayVType;
import com.windhoverlabs.data.yamcs.Sint32VType;
import com.windhoverlabs.data.yamcs.Sint64ArrayVType;
import com.windhoverlabs.data.yamcs.Sint64VType;
import com.windhoverlabs.data.yamcs.StringArrayVType;
import com.windhoverlabs.data.yamcs.StringVType;
import com.windhoverlabs.data.yamcs.TimestampVType;
import com.windhoverlabs.data.yamcs.Uint32ArrayVType;
import com.windhoverlabs.data.yamcs.Uint32VType;
import com.windhoverlabs.data.yamcs.Uint64ArrayVType;
import com.windhoverlabs.data.yamcs.Uint64VType;
import com.windhoverlabs.pv.yamcs.YamcsPlugin;
import com.windhoverlabs.yamcs.studio.data.vtype.Alarm;
import com.windhoverlabs.yamcs.studio.data.vtype.AlarmSeverity;
import com.windhoverlabs.yamcs.studio.data.vtype.Display;
import com.windhoverlabs.yamcs.studio.data.vtype.NumberFormats;
import com.windhoverlabs.yamcs.studio.data.vtype.Time;

import org.epics.vtype.VType;
public class YamcsVType extends VType implements Alarm, Time, Display {

    private ParameterValue pval;
    protected Value value;

    public YamcsVType(ParameterValue pval, boolean raw) {
        this.pval = pval;
        value = raw ? pval.getRawValue() : pval.getEngValue();
    }

    public NamedObjectId getId() {
        return pval.getId();
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        if (pval == null) {
            return AlarmSeverity.NONE;
        }

        if (pval.getAcquisitionStatus() == AcquisitionStatus.EXPIRED
                || pval.getAcquisitionStatus() == AcquisitionStatus.NOT_RECEIVED
                || pval.getAcquisitionStatus() == AcquisitionStatus.INVALID) {
            return AlarmSeverity.INVALID; // Workaround to display LOS in the displays, should be 'Expired'
        }

        if (!pval.hasMonitoringResult()) {
            return AlarmSeverity.NONE;
        }

        switch (pval.getMonitoringResult()) {
        case IN_LIMITS:
            return AlarmSeverity.NONE;
        case DISABLED:
            return AlarmSeverity.NONE;
        case WATCH:
        case WARNING:
        case DISTRESS:
            return AlarmSeverity.MINOR;
        case CRITICAL:
        case SEVERE:
            return AlarmSeverity.MAJOR;
        default:
            throw new IllegalStateException("Unexpected alarm severity " + pval.getMonitoringResult());
        }
    }

    @Override
    public String getAlarmName() {
        return "";
    }

    @Override
    public Instant getTimestamp() {
        if (pval != null && pval.hasGenerationTime()) {
            return Instant.ofEpochSecond(pval.getGenerationTime().getSeconds(), pval.getGenerationTime().getNanos());
        } else {
            return null;
        }
    }

    @Override
    public Integer getTimeUserTag() {
        return null;
    }

    @Override
    public boolean isTimeValid() {
        return true;
    }

    @Override
    public Double getLowerWarningLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.WATCH
                        || range.getLevel() == AlarmLevelType.WARNING
                        || range.getLevel() == AlarmLevelType.DISTRESS) {
                    if (range.hasMinInclusive()) {
                        return range.getMinInclusive();
                    } else if (range.hasMinExclusive()) {
                        return range.getMinExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Highest value before the warning region
     */
    @Override
    public Double getUpperWarningLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.WATCH
                        || range.getLevel() == AlarmLevelType.WARNING
                        || range.getLevel() == AlarmLevelType.DISTRESS) {
                    if (range.hasMaxInclusive()) {
                        return range.getMaxInclusive();
                    } else if (range.hasMaxExclusive()) {
                        return range.getMaxExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    @Override
    public Double getLowerAlarmLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.CRITICAL
                        || range.getLevel() == AlarmLevelType.SEVERE) {
                    if (range.hasMinInclusive()) {
                        return range.getMinInclusive();
                    } else if (range.hasMinExclusive()) {
                        return range.getMinExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Highest value before the alarm region
     */
    @Override
    public Double getUpperAlarmLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.CRITICAL
                        || range.getLevel() == AlarmLevelType.SEVERE) {
                    if (range.hasMaxInclusive()) {
                        return range.getMaxInclusive();
                    } else if (range.hasMaxExclusive()) {
                        return range.getMaxExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    @Override
    public Double getLowerDisplayLimit() {
        Double loLimit = getLowerAlarmLimit();
        if (loLimit == Double.NaN) {
            loLimit = getLowerWarningLimit();
        }

        return loLimit;
    }

    @Override
    public Double getUpperDisplayLimit() {
        Double hiLimit = getUpperAlarmLimit();
        if (hiLimit == Double.NaN) {
            hiLimit = getUpperWarningLimit();
        }

        return hiLimit;
    }

    @Override
    public Double getLowerCtrlLimit() {
        return Double.NaN;
    }

    @Override
    public Double getUpperCtrlLimit() {
        return Double.NaN;
    }

    @Override
    public String getUnits() {
        if (pval != null) {
            MissionDatabase mdb = YamcsPlugin.getMissionDatabase();
            if (mdb != null) {
                String unit = mdb.getCombinedUnit(pval.getId());
                return (unit == null) ? "" : unit;
            }
        }
        return "";
    }

    @Override
    public NumberFormat getFormat() {
        return NumberFormats.toStringFormat();
    }

    /**
     * Converts a yamcs ParameterValue to a VType.
     */
    public static VType fromYamcs(ParameterValue pval, boolean raw) {
        Value value;
        if (raw) {
            if (!pval.hasRawValue()) {
                return null;
            }
            value = pval.getRawValue();
        } else {
            if (!pval.hasEngValue()) {
                return null;
            }
            value = pval.getEngValue();
        }

        switch (value.getType()) {
        case UINT32:
            return new Uint32VType(pval, raw);
        case SINT32:
            return new Sint32VType(pval, raw);
        case UINT64:
            return new Uint64VType(pval, raw);
        case SINT64:
            return new Sint64VType(pval, raw);
        case FLOAT:
            return new FloatVType(pval, raw);
        case DOUBLE:
            return new DoubleVType(pval, raw);
        case BOOLEAN:
            return new BooleanVType(pval, raw);
        case STRING:
            return new StringVType(pval, raw);
        case BINARY:
            return new BinaryVType(pval, raw);
        case TIMESTAMP:
            return new TimestampVType(pval, raw);
        case ENUMERATED:
            return new EnumeratedVType(pval, raw);
        case AGGREGATE:
            return new AggregateVType(pval, raw);
        case ARRAY:
            List<Value> arrayValues = value.getArrayValueList();
            if (arrayValues.isEmpty()) {
                return null; // TODO
            } else {
                switch (arrayValues.get(0).getType()) {
                case UINT32:
                    return new Uint32ArrayVType(pval, raw);
                case SINT32:
                    return new Sint32ArrayVType(pval, raw);
                case UINT64:
                    return new Uint64ArrayVType(pval, raw);
                case SINT64:
                    return new Sint64ArrayVType(pval, raw);
                case FLOAT:
                    return new FloatArrayVType(pval, raw);
                case DOUBLE:
                    return new DoubleArrayVType(pval, raw);
                case BOOLEAN:
                    return new BooleanArrayVType(pval, raw);
                case STRING:
                    return new StringArrayVType(pval, raw);
                case ENUMERATED:
                    return new EnumeratedArrayVType(pval, raw);
                case AGGREGATE:
                    return new AggregateArrayVType(pval, raw);
                case ARRAY:
                    return new ArrayArrayVType(pval, raw);
                default:
                    throw new IllegalStateException(
                            "Unexpected type for parameter array value. Got: " + arrayValues.get(0).getType());
                }
            }
        default:
            throw new IllegalStateException(
                    "Unexpected type for parameter value. Got: " + value.getType());
        }
    }
}
