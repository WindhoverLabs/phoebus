/*******************************************************************************
 * Copyright (c) 2017-2019 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.windhoverlabs.display.model.widgets;

import java.util.Collection;
import java.util.List;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.spi.WidgetsService;

/**
 * SPI for the Windhover Labs based widgets
 *
 * @author lgomez
 */
public class WHBaseWidgetsService implements WidgetsService {
  @Override
  public Collection<WidgetDescriptor> getWidgetDescriptors() {
    return List.of(
        WHTextUpdateWidget.WIDGET_DESCRIPTOR,
        CommanderCommandActionButtonWidget.WIDGET_DESCRIPTOR,
        CommanderVideoWidget.WIDGET_DESCRIPTOR,
        WaypointModel.WIDGET_DESCRIPTOR);
  }
}
