/*******************************************************************************
 * Copyright (c) 2014-2020 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.windhover.pv.yamcs;

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
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;
import org.yamcs.protobuf.SubscribeParametersRequest.Builder;

import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Factory for creating {@link YamcsPV}s
 * 
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class YamcsPVFactory implements PVFactory {
	final public static String TYPE = "yamcs";

	private YamcsClient yamcsClient = null;

	private ParameterSubscription yamcsSubscription = null;

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private AtomicBoolean subscriptionDirty = new AtomicBoolean(false);

	private Map<NamedObjectId, Set<PV>> pvsById = new LinkedHashMap<>();

	private static final Logger log = Logger.getLogger(YamcsPVFactory.class.getName());

	private YamcsSubscriptionService subscriptionService;
	
	private YamcsPlugin yamcsPlugin = YamcsPlugin.getPlugin();

	private ArrayList<NamedObjectId> ids = new ArrayList<NamedObjectId>();

	/** Map of local PVs */
	private static final Map<String, YamcsPV> yamcs_pvs = new HashMap<>();

	public YamcsPVFactory() throws ClientException {
		System.out.println("YAMCS Init");
		
		yamcsPlugin.init("127.0.0.1", 8090);
		

		yamcsClient = YamcsPlugin.getYamcsClient();
		
		System.out.println("YAMCS Init2");

		if (yamcsClient != null) {
			yamcsSubscription = yamcsClient.createParameterSubscription();
		}

		System.out.println("YAMCS Init3");

		yamcsClient.listInstances().whenComplete((response, exc) -> {

			if (exc == null) {
				List<ProcessorInfo> processors = new ArrayList<>();

				for (YamcsInstance instance : response) {
					System.out.println("instance name:" + instance);
				}
			}
		});

		// Periodically check if the subscription needs a refresh
		// (PVs send individual events, so this bundles them)
		executor.scheduleWithFixedDelay(() -> {
			System.out.println();
			if (subscriptionDirty.getAndSet(false) && yamcsSubscription != null) {
				Set<NamedObjectId> ids = getRequestedIdentifiers();
				log.fine(String.format("Modifying subscription to %s", ids));
//				yamcsSubscription.sendMessage(
//						SubscribeParametersRequest.newBuilder().setAction(Action.REPLACE).setSendFromCache(true)
//								.setAbortOnInvalid(false).setUpdateOnExpiration(true).addAllId(ids).build());

				System.out.println("Modifying subscription to:" + ids);
			}

			System.out.println("modifying subscription from factory");
		}, 500, 500, TimeUnit.MILLISECONDS);

		System.out.println("client status" + yamcsClient.listInstances());
		initProcessor();

//		subscriptionService = new YamcsSubscriptionService();
	}

	private void initProcessor() {
		System.out.println("initProcessor1");
		Set<NamedObjectId> ids = getRequestedIdentifiers();
		System.out.println("initProcessor2");
		log.fine(String.format("Subscribing to %s [%s/%s]", ids, "yamcs-cfs", "realtime"));
		System.out.println("initProcessor3");
		SubscribeParametersRequest request = SubscribeParametersRequest.newBuilder().setInstance("yamcs-cfs")
				.setProcessor("realtime").setSendFromCache(true).setAbortOnInvalid(false).setUpdateOnExpiration(true)
				.addAllId(ids).build();
		System.out.println("initProcessor4");

		Builder builder = SubscribeParametersRequest.newBuilder();

		builder = builder.setInstance("yamcs-cfs");
		System.out.println("initProcessor5");
		builder = builder.setProcessor("realtime");
		System.out.println("initProcessor6");
		builder = builder.setSendFromCache(true);
		System.out.println("initProcessor7");
		builder = builder.setAbortOnInvalid(false);
		System.out.println("initProcessor8");
		builder = builder.setUpdateOnExpiration(true);

		System.out.println("initProcessor9");

		System.out.println("ids:" + ids);

//		builder = builder.addId(ids.);

		// Something's wrong here. I think, for some reason, the instance/processor is
		// null.
//		yamcsSubscription.sendMessage(SubscribeParametersRequest.newBuilder().setInstance("yamcs-cfs")
//				.setProcessor("realtime").setSendFromCache(true).setAbortOnInvalid(false).setUpdateOnExpiration(true)
//				.addAllId(ids).build());
	}

	/**
	 * Async adds a Yamcs PV for receiving updates.
	 */
	public void register(PV pv) {
		System.out.println("register pv:" + pv);
		NamedObjectId id = YamcsSubscriptionService.identityOf(YamcsSubscriptionService.getYamcsPvName(pv.getName()));
		executor.execute(() -> {
			Set<PV> pvs = pvsById.computeIfAbsent(id, x -> new HashSet<>());

			System.out.println("pvs in register callback:" + pvs);

			pvs.add(pv);
			subscriptionDirty.set(true);
		});

		ids.add(id);

		System.out.println("ids on register:" + ids);

		Builder builder = SubscribeParametersRequest.newBuilder().setInstance("yamcs-cfs");

		System.out.println("builder1:" + builder);

		builder = builder.setProcessor("realtime");

		System.out.println("builder2:" + builder);

		builder = builder.setSendFromCache(true);

		System.out.println("builder3:" + builder);

		builder = builder.setAbortOnInvalid(false);

		System.out.println("builder4:" + builder);

		builder = builder.setUpdateOnExpiration(true);

		System.out.println("builder5:" + builder);

		System.out.println("ids.get(0)----->" + ids.get(0));

		System.out.println("yamcs value-->" + yamcsSubscription.get(id));

		System.out.println("build:");

//		builder.build();

//		yamcsSubscription.getValue("");

		try {
			yamcsSubscription.sendMessage(SubscribeParametersRequest.newBuilder().setInstance("yamcs-cfs")
					.setProcessor("realtime").setSendFromCache(true).setAbortOnInvalid(false)
					.setUpdateOnExpiration(true).addId(ids.get(0)).setAction(Action.ADD).build());
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
//		final String[] ntv = ValueHelper.parseName(base_name);

		// Actual name: loc://the_pv without <type> or (initial value)
//		final String actual_name = YamcsPVFactory.TYPE + PVPool.SEPARATOR + ntv[0];

		String actual_name = name;

		// Info for initial value, null if nothing provided
//		final List<String> initial_value = ValueHelper.splitInitialItems(ntv[2]);

		// Determine type from initial value or use given type
//		final Class<? extends VType> type = ntv[1] == null ? determineValueType(initial_value) : parseType(ntv[1]);
		final Class<? extends VType> type = parseType("");

		YamcsPV pv = new YamcsPV(actual_name, type, yamcsSubscription);

		yamcsSubscription.addListener(pv);

		register(pv);
		// TODO Use ConcurrentHashMap, computeIfAbsent
//		synchronized (yamcs_pvs) {
//			pv = yamcs_pvs.get(actual_name);
//			if (pv == null) {
//				pv = new YamcsPV(actual_name, type);
//				yamcs_pvs.put(actual_name, pv);
//			} else
//				pv.checkInitializer(type, null);
//		}
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

	public static Class<? extends VType> parseType(final String type) throws Exception { // Lenient check, ignore case

// and allow partial match

		return YamcsVType.class;
//		final String lower = type.toLowerCase();
//		if (lower.contains("doublearray"))
//			return VDoubleArray.class;
//		if (lower.contains("double")) // 'VDouble', 'vdouble', 'double'
//			return VDouble.class;
//		if (lower.contains("stringarray"))
//			return VStringArray.class;
//		if (lower.contains("string"))
//			return VString.class;
//		if (lower.contains("enum"))
//			return VEnum.class;
//		if (lower.contains("long"))
//			return VLong.class;
//		if (lower.contains("int"))
//			return VInt.class;
//		if (lower.contains("boolean"))
//			return VBoolean.class;
//		if (lower.contains("table"))
//			return VTable.class;
//		throw new Exception("Local PV cannot handle type '" + type + "'");
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

}
