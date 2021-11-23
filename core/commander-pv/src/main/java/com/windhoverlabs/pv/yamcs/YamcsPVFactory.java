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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.epics.vtype.VBoolean;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VEnum;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VTable;
import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVFactory;
import org.phoebus.pv.PVPool;
import org.yamcs.client.ClientException;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.YamcsInstance;

import com.windhoverlabs.commander.core.CMDR_YamcsInstance;

import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;
import org.yamcs.protobuf.SubscribeParametersRequest.Builder;

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

	// TODO:Need one of these per each server.
	private YamcsClient yamcsClient = null;
	private ParameterSubscription yamcsSubscription = null;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private AtomicBoolean subscriptionDirty = new AtomicBoolean(false);
	private Map<NamedObjectId, Set<PV>> pvsById = new LinkedHashMap<>();
	private static final Logger log = Logger.getLogger(YamcsPVFactory.class.getName());
	private ArrayList<NamedObjectId> ids = new ArrayList<NamedObjectId>();

	/** Map of local PVs */
	private static final Map<String, YamcsPV> yamcs_pvs = new HashMap<>();

	public YamcsPVFactory() throws ClientException {

		yamcsClient = YamcsPlugin.getYamcsClient();

		if (yamcsClient != null) {
			yamcsSubscription = yamcsClient.createParameterSubscription();
		}

		yamcsClient.listInstances().whenComplete((response, exc) -> {

			if (exc == null) {
				for (YamcsInstance instance : response) {
					System.out.println("instance name:" + instance);
				}
			}
		});

		// Periodically check if the subscription needs a refresh
		// (PVs send individual events, so this bundles them)
		executor.scheduleWithFixedDelay(() -> {
			if (subscriptionDirty.getAndSet(false) && yamcsSubscription != null) {
				Set<NamedObjectId> ids = getRequestedIdentifiers();
				log.fine(String.format("Modifying subscription to %s", ids));
//				yamcsSubscription.sendMessage(
//						SubscribeParametersRequest.newBuilder().setAction(Action.REPLACE).setSendFromCache(true)
//								.setAbortOnInvalid(false).setUpdateOnExpiration(true).addAllId(ids).build());

			}

		}, 500, 500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Async adds a Yamcs PV for receiving updates.
	 */
	public void register(PV pv, String instance) {
		NamedObjectId id = YamcsSubscriptionService.identityOf(YamcsSubscriptionService.getYamcsPvName(pv.getName()));
		executor.execute(() -> {
			Set<PV> pvs = pvsById.computeIfAbsent(id, x -> new HashSet<>());
			pvs.add(pv);
			subscriptionDirty.set(true);
		});

		ids.add(id);

		try {
			yamcsSubscription.sendMessage(SubscribeParametersRequest.newBuilder().setInstance("yamcs-cfs")
					.setProcessor("realtime").setSendFromCache(true).setAbortOnInvalid(false)
					.setUpdateOnExpiration(true).addAllId(ids).setAction(Action.ADD).build());
		} catch (Exception e) {
			System.out.println("e:" + e);
		}
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

		YamcsPV pv = new YamcsPV(actual_name, type, yamcsSubscription);

		String instanceName = "yamcs-cfs";

		yamcsSubscription.addListener(pv);

		register(pv, instanceName);
		// TODO Use ConcurrentHashMap, computeIfAbsent
		synchronized (yamcs_pvs) {
			pv = yamcs_pvs.get(actual_name);
			if (pv == null) {
				pv = new YamcsPV(actual_name, type);
				yamcs_pvs.put(actual_name, pv);
			} else
				pv.checkInitializer(type, null);
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
	static void releasePV(final YamcsPV pv) {
		synchronized (yamcs_pvs) {
			yamcs_pvs.remove(pv.getName());
		}
	}

	// For unit test
	public static Collection<YamcsPV> getLocalPVs() {
		synchronized (yamcs_pvs) {
			return yamcs_pvs.values();
		}
	}

	private String generateExamplePV() {
		return "Server_A:yamcs-cfs://cfs/CPD/amc/AMC_HkTlm_t.usCmdCnt";
	}

	public static String extractServerName(String pvName) {
		return pvName.split(":")[0];
	}

	public static CMDR_YamcsInstance getCMDR_YamcsInstanceFromPVname(String PVName) {
		
		return null;
	}

}
