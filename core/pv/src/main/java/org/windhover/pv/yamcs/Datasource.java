package org.windhover.pv.yamcs;

import org.windhover.pv.yamcs.VType;

public interface Datasource {

    boolean supportsPVName(String pvName);

    boolean isConnected(IPV pv);

    boolean isWriteAllowed(IPV pv);

    VType getValue(IPV pv);

    void writeValue(IPV pv, Object value, WriteCallback callback);

    void onStarted(IPV pv);

    void onStopped(IPV pv);
}
