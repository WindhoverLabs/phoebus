package org.windhoverlabs.display.builder.runtime;

import static java.util.Map.entry;

import java.util.Map;
import java.util.function.Supplier;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.spi.WidgetRuntimesService;
import org.windhoverlabs.display.model.widgets.CommanderCommandActionButtonWidget;

public class CommanderWidgetRuntimes implements WidgetRuntimesService {

	@Override
	public Map<String, Supplier<WidgetRuntime<? extends Widget>>> getWidgetRuntimeFactories() {
		return Map.ofEntries(entry(CommanderCommandActionButtonWidget.WIDGET_DESCRIPTOR.getType(),() -> new CommanderCommandActionButtonWidgetRuntime()));
	}

}
