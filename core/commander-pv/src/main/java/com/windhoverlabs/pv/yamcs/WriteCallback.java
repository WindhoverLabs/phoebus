package com.windhoverlabs.pv.yamcs;

@FunctionalInterface
public interface WriteCallback {

    /**
     * Called when a write is completed. If completed without error, the argument is null.
     */
    void dataWritten(Exception exception);
}
