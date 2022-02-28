/*******************************************************************************
 * Copyright (c) 2014-2020 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.pv.yamcs;

import com.windhoverlabs.yamcs.core.CMDR_YamcsInstance;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVFactory;
import org.phoebus.pv.PVPool;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * Factory for creating {@link YamcsPV}s
 *
 * @author Lorenzo Gomez
 */
@SuppressWarnings("nls")
public class YamcsPVFactory implements PVFactory {
  public static final String TYPE = "yamcs";
  // Special string that may refer to default server or instance. This does NOT
  // refer default data source.
  public static final String DEFAULT = "default";

  private Map<NamedObjectId, Set<PV>> pvsById = new LinkedHashMap<>();
  private static final Logger log = Logger.getLogger(YamcsPVFactory.class.getName());

  List<YamcsServer> allServers = new ArrayList<YamcsServer>();

  /** Map of local PVs */
  private static final Map<String, YamcsPV> yamcs_pvs = new HashMap<>();

  public YamcsPVFactory() {}

  /**
   * Async adds a Yamcs PV for receiving updates.
   *
   * @return TODO
   */
  public boolean register(PV pv) {
    CMDR_YamcsInstance pvInstance = null;
    String serverPath = extractServerNameFromPVName(pv.getName());
    String instanceName = extractInstanceNameFromPVName(pv.getName());
    if (!instanceName.isEmpty()) {
      pvInstance = YamcsObjectManager.getInstanceFromName(serverPath, instanceName);
    } else {
      YamcsServer pvServer = YamcsObjectManager.getServerFromName(serverPath);

      if (pvServer != null) {
        pvInstance = YamcsObjectManager.getServerFromName(serverPath).getDefaultInstance();
      } else {
        pvInstance = null;
      }
    }
    if (pvInstance == null) {
      log.warning(String.format("Instance not found for \"%s\" server", serverPath));
      return false;
    }

    pvInstance.subscribePV((YamcsPV) pv);

    return true;
  }

  private static boolean isDefaultDataSource(String pv) {
    boolean isDefault = false;
    if (!pv.contains(PVPool.SEPARATOR)) {
      isDefault = true;
    }
    return isDefault;
  }

  private String extractServerNameFromPVName(String pv) {
    String serverPath = pv;
    serverPath = serverPath.substring(1);

    if (serverPath.contains(":")) {
      serverPath = serverPath.split(":")[0];
    } else {
      serverPath = serverPath.split("/")[0];
    }
    return serverPath;
  }

  /**
   * If the default instance is being used on the PV, then an empty string is returned.
   *
   * @param pv
   * @return
   */
  private String extractInstanceNameFromPVName(String pv) {
    String InstancePath = pv;
    InstancePath = InstancePath.substring(2);
    if (InstancePath.contains(":")) {
      InstancePath = InstancePath.split(":")[1].split("/")[0];
    } else {
      // No instance name means the user is using the default instance.
      InstancePath = "";
    }

    return InstancePath;
  }

  private Set<NamedObjectId> getRequestedIdentifiers() {
    return pvsById.entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getCoreName(final String name) {
    String actual_name = sanitizePVName(name);
    return actual_name;
  }

  @Override
  public PV createPV(final String name, final String base_name) throws Exception {
    String actual_name = sanitizePVName(name);

    final Class<? extends VType> type = parseType("");

    YamcsPV pv;
    // TODO Use ConcurrentHashMap, computeIfAbsent
    synchronized (yamcs_pvs) {
      pv = yamcs_pvs.get(actual_name);
      List<String> initial_value = null;
      if (pv == null) {
        pv = new YamcsPV(actual_name, type, initial_value);
        if (register(pv)) {
          yamcs_pvs.put(actual_name, pv);
        }
      } else {
        pv.checkInitializer(type, initial_value);
      }
    }

    return pv;
  }

  public static String sanitizePVName(final String name) {
    String actual_name = name;
    if (isDefaultDataSource(actual_name)) {
      actual_name = name;
    } else {
      actual_name = actual_name.substring("yamcs:/".length());
    }

    // Perhaps the GUI should enforce this...
    if (!actual_name.contains(":")) {
      // This means default instance
      CMDR_YamcsInstance defaultInstance = YamcsObjectManager.getDefaultInstance();

      if (defaultInstance == null) {
        log.warning(
            "No default server found.\n" + "You may set a default server in the Connections app.");
      } else {
        String serverName = YamcsObjectManager.getDefaultServer().getName();
        String instanceName = defaultInstance.getName();
        actual_name = "/" + serverName + ":" + instanceName + actual_name;
      }
    }

    return actual_name;
  }

  public static Class<? extends VType> determineValueType(final List<String> items)
      throws Exception {
    if (items == null) return VDouble.class;

    if (ValueHelper.haveInitialStrings(items)) {
      if (items.size() == 1) return VString.class;
      else return VStringArray.class;
    } else {
      if (items.size() == 1) return VDouble.class;
      else return VDoubleArray.class;
    }
  }

  public static Class<? extends VType> parseType(final String type) throws Exception {
    return YamcsVType.class;
  }

  /**
   * Remove local PV from pool To be called by LocalPV when closed
   *
   * @param pv {@link YamcsPV}
   */

  // For unit test
  public static Collection<YamcsPV> getLocalPVs() {
    synchronized (yamcs_pvs) {
      return yamcs_pvs.values();
    }
  }

  public static String extractServerName(String pvName) {
    return pvName.split(":")[0];
  }
}
