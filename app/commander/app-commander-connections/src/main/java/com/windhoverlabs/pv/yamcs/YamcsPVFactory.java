/*******************************************************************************
 * Copyright (c) 2014-2020 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.pv.yamcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Logger;

import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVFactory;
import org.phoebus.pv.loc.LocalPV;
import org.yamcs.client.ClientException;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.YamcsInstance;

import com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance;
import com.windhoverlabs.commander.core.OLD_CMDR_YamcsInstance;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.commander.core.YamcsServerContext;

import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;

import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Factory for creating {@link YamcsPV}s
 * 
 * @author Lorenzo Gomez
 */
@SuppressWarnings("nls")
public class YamcsPVFactory implements PVFactory {
	final public static String TYPE = "yamcs";

	private Map<NamedObjectId, Set<PV>> pvsById = new LinkedHashMap<>();
	private static final Logger log = Logger.getLogger(YamcsPVFactory.class.getName());

	List<YamcsServer> allServers = new ArrayList<YamcsServer>();

	/** Map of local PVs */
	private static final Map<String, YamcsPV> yamcs_pvs = new HashMap<>();

	public YamcsPVFactory() {
	}

	/**
	 * Async adds a Yamcs PV for receiving updates.
	 * 
	 * @return TODO
	 */
	public boolean register(PV pv) {
		String serverPath = extractServerNameFromPVName(pv);
		String instanceName = extractInstanceNameFromPVName(pv);
		YamcsServer pvServer = ConnectionsManagerInstance.getServerTree().getServerFromName(serverPath);
		if (pvServer == null) {
			log.warning("Server not found");
			return false;
		}
		pvServer.subscribePV((YamcsPV) pv, instanceName);

		return true;
	}

	private String extractServerNameFromPVName(PV pv) {
		String serverPath = pv.getName().split("://")[1];
		serverPath = serverPath.split(":")[0];
		return serverPath;
	}

	private String extractInstanceNameFromPVName(PV pv) {
		String InstancePath = pv.getName().split("://")[1];
		InstancePath = InstancePath.split(":")[1].split("/")[0];
		return InstancePath;
	}

	private Set<NamedObjectId> getRequestedIdentifiers() {
		return pvsById.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getCoreName(final String name) {
		int sep = name.indexOf('<');
		if (sep > 0)
			return name.substring(0, sep);
		sep = name.indexOf('(');
		if (sep > 0)
			return name.substring(0, sep);
		return name;
	}

	@Override
	public PV createPV(final String name, final String base_name) throws Exception {

		String actual_name = name;

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

	public static Class<? extends VType> determineValueType(final List<String> items) throws Exception {
		if (items == null)
			return VDouble.class;

		if (ValueHelper.haveInitialStrings(items)) {
			if (items.size() == 1)
				return VString.class;
			else
				return VStringArray.class;
		} else {
			if (items.size() == 1)
				return VDouble.class;
			else
				return VDoubleArray.class;
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
