package org.windhover.pv.yamcs;

import org.phoebus.pv.PV;

public interface Datasource {

    boolean supportsPVName(String pvName);

    boolean isConnected(PV pv);

    boolean isWriteAllowed(PV pv);

    org.epics.vtype.VType getValue(PV pv);

    void writeValue(PV pv, Object value, WriteCallback callback);

    void onStarted(PV pv);

    void onStopped(PV pv);
}
