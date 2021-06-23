package org.windhover.pv.yamcs;

/**
 * A singleton service managed by {@link YamcsPlugin}
 */
public interface PluginService {

    /**
     * Performs and necessary cleanup.
     */
    void dispose();
}
