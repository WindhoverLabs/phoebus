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
import java.util.logging.Level;
import org.epics.vtype.VInt;
import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.loc.ValueHelper;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * Yamcs Process Variable Syntax: "ServerName:instance-name://path/to/pv/item"
 * 
 * @author lgomez, based on similar code in org.csstudio.utility.pv
 */
@SuppressWarnings("nls")
public class YamcsPV extends PV {
	private volatile Class<? extends VType> type;
	private final List<String> initial_value;

	protected YamcsPV(final String actual_name, final Class<? extends VType> type, final List<String> initial_value) {
		super(actual_name);
		this.type = type;
		this.initial_value = initial_value;

		notifyListenersOfPermissions(true);
	}

	protected YamcsPV(final String actual_name, final Class<? extends VType> type) {
		super(actual_name);
		this.type = type;

		initial_value = new ArrayList<String>();
		notifyListenersOfPermissions(true);
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
	protected void close() {
		super.close();
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
}
