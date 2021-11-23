/*******************************************************************************
 * Copyright (c) 2014-2018 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.pv.yamcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.epics.vtype.Time;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VInt;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.epics.vtype.VUInt;
import org.phoebus.core.vtypes.VTypeHelper;
import org.phoebus.pv.PV;
import org.phoebus.pv.loc.ValueHelper;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * Yamcs Process Variable
 * Syntax:
 * "ServerName:instance-name://path/to/pv/item"
 * 
 * @author lgomez, based on similar code in org.csstudio.utility.pv
 */
@SuppressWarnings("nls")
public class YamcsPV extends PV implements ParameterSubscription.Listener {
	private volatile Class<? extends VType> type;
	private final List<String> initial_value;

	ParameterSubscription yamcsSubscription = null;
	/** Timer for periodic updates */
	private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, target -> {
		final Thread thread = new Thread(target, "YamcsPv");
		thread.setDaemon(true);
		return thread;
	});

	private ScheduledFuture<?> task;

	protected YamcsPV(final String actual_name, final Class<? extends VType> type, final List<String> initial_value)
			throws Exception {
		super(actual_name);
		this.type = type;
		this.initial_value = initial_value;

		// Set initial value
		notifyListenersOfValue(ValueHelper.getInitialValue(initial_value, type));

	}

	protected YamcsPV(final String actual_name, final Class<? extends VType> type) throws Exception {
		super(actual_name);
		this.type = type;

		initial_value = new ArrayList<String>();


	}

	protected YamcsPV(final String actual_name, final Class<? extends VType> type,
			ParameterSubscription newYamcsSubscription) throws Exception {
		super(actual_name);
		this.type = type;

		initial_value = new ArrayList<String>();


		yamcsSubscription = newYamcsSubscription;		

		double update_seconds = 1;
		// Limit rate to 100 Hz
		final long milli = Math.round(Math.max(update_seconds, 0.01) * 1000);
//		task = executor.scheduleAtFixedRate(() -> {
//			try {
//				updateValue();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}, milli, milli, TimeUnit.MILLISECONDS);

	}

	protected void checkInitializer(final Class<? extends VType> type, final List<String> initial_value) {
		if (type != this.type || !Objects.equals(initial_value, this.initial_value))
			logger.log(Level.WARNING,
					"PV " + getName() + " was initialized as " + formatInit(this.type, this.initial_value)
							+ " and is now requested as " + formatInit(type, initial_value));
	}

	private String formatInit(final Class<? extends VType> type, final List<String> value) {
		final StringBuilder buf = new StringBuilder();
		buf.append('<').append(type.getSimpleName()).append('>');
		if (value != null) {
			buf.append('(');
			for (int i = 0; i < value.size(); ++i) {
				if (i > 0)
					buf.append(",");
				buf.append(value.get(i));
			}
			buf.append(')');
		}
		return buf.toString();
	}

	@Override
	public void write(final Object new_value) throws Exception {
//		if (new_value == null)
//			throw new Exception(getName() + " got null");
//
//		try {
//			final VType last_value = read();
//			final boolean change_from_double = initial_value == null && last_value instanceof VDouble
//					&& ((VDouble) last_value).getAlarm().getSeverity() == ValueHelper.UDF.getSeverity();
//			final VType value = ValueHelper.adapt(new_value, type, last_value, change_from_double);
//			if (change_from_double && !type.isInstance(value)) {
//				final Class<? extends VType> new_type;
//				if (value instanceof VDoubleArray)
//					new_type = VDoubleArray.class;
//				else if (value instanceof VStringArray)
//					new_type = VStringArray.class;
//				else
//					new_type = VString.class;
//				logger.log(Level.WARNING, "PV " + getName() + " changed from " + type.getSimpleName() + " to "
//						+ new_type.getSimpleName());
//				type = new_type;
//			}
//			notifyListenersOfValue(value);
//		} catch (Exception ex) {
//			if (new_value != null && new_value.getClass().isArray())
//				throw new Exception("Failed to write " + new_value.getClass().getSimpleName() + " to " + getName(), ex);
//			throw new Exception("Failed to write '" + new_value + "' to " + this, ex);
//		}
	}

	public VType getValue(String pvName) {
		pvName = YamcsSubscriptionService.getYamcsPvName(pvName);
		System.out.println("getValue YAMCSPV1:" + pvName);
		NamedObjectId id = YamcsSubscriptionService.identityOf(pvName);

		System.out.println(" NameObjectID:" + id.getName());
		System.out.println("getValue YAMCSPV2");
		if (yamcsSubscription != null) {
			System.out.println("getValue YAMCSPV3");
			System.out.println("");
			ParameterValue pval = yamcsSubscription.get(id);
			if (pval != null) {
				System.out.println("getValue YAMCSPV4");
				boolean raw = pvName.startsWith("raw://");
				System.out.println("getValue YAMCSPV5");
//				return YamcsVType.fromYamcs(pval, raw);
			}
		}
		System.out.println("getValue YAMCSPV6");
		return null;
	}

	public VType read() {
		System.out.println("read-->YamcsPV");

		return getValue(getName());
//		return dataSource.getValue(this);
	}

	@Override
	protected void close() {
		super.close();
		YamcsPVFactory.releasePV(this);
	}

	@Override
	public void onData(List<ParameterValue> values) {		
		ArrayList<String> yamcsValues = new ArrayList<String>();
		yamcsValues.add(Integer.toString(values.get(0).getEngValue().getUint32Value()));
		VType value = null;
		try {
			value = ValueHelper.getInitialValue(yamcsValues, VInt.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.notifyListenersOfValue(value);

	}

	public void updateValue() throws Exception {
		ArrayList<String> values = new ArrayList<String>();	

		VType value = ValueHelper.getInitialValue(values, VInt.class);
		
		this.notifyListenersOfValue(value);
	}

	public void updateValue(VType value) throws Exception {
		this.notifyListenersOfValue(value);
	}
	
	public void onInvalidIdentification(NamedObjectId id) {
		System.out.println("onInvalidIdentification:" + id);
	}

	/** Called by periodic timer */
//    @Override
//    protected void update()
//    {
//        final double value = compute();
//        // Creates vtype with alarm according to display warning/alarm ranges
//        final VType vtype = VDouble.of(value, display.newAlarmFor(value), Time.now(), display);
//        notifyListenersOfValue(vtype);
//    }
	
//    public VType getValue(String pvName) {
//        NamedObjectId id =YamcsSubscriptionService.identityOf(pvName);
//        System.out.println("pvName on getValue1:" + pvName);
//        if (yamcsSubscription != null) {
//            System.out.println("pvName on getValue2:" + pvName);
//
//            ParameterValue pval = yamcsSubscription.get(id);
//            System.out.println("pvName on getValue3:" + pvName);
//            if (pval != null) {
//                System.out.println("pvName on getValue4:" + pvName);
//                boolean raw = pvName.startsWith("raw://");
//                System.out.println("pval:" +  pval.getRawValue());
////                return YamcsVType.fromYamcs(pval, raw);
//            }
//            System.out.println("pvName on getValue5:" + pvName);
//        }
//        System.out.println("pvName on getValue6:" + pvName);
//
//        return null;
//    }
}
