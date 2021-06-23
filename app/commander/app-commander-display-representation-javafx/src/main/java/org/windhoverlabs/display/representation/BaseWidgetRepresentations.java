/*******************************************************************************
 * Copyright (c) 2017-2019 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.windhoverlabs.display.representation;

import static java.util.Map.entry;

import java.util.Map;

import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentationFactory;
import org.csstudio.display.builder.representation.spi.WidgetRepresentationsService;
import org.windhoverlabs.display.model.widgets.CommanderCommandActionButtonWidget;
import org.windhoverlabs.display.model.widgets.WHTextUpdateWidget;

/**
 * SPI for representations of base widgets
 * 
 * @author lgomez
 */
public class BaseWidgetRepresentations implements WidgetRepresentationsService {
	@SuppressWarnings({ "unchecked", "rawtypes", "nls" })
	@Override

	public <TWP, TW> Map<WidgetDescriptor, WidgetRepresentationFactory<TWP, TW>> getWidgetRepresentationFactories() {
		
		System.out.println("BaseWidgetRepresentations");

		return Map.ofEntries(entry(WHTextUpdateWidget.WIDGET_DESCRIPTOR,() -> (WidgetRepresentation) new WHTextUpdateRepresentation()),
							 entry(CommanderCommandActionButtonWidget.WIDGET_DESCRIPTOR,() -> (WidgetRepresentation) new CommanderActionButtonRepresentation()));
	}
}
