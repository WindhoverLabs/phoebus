/*******************************************************************************
 * Copyright (c) 2018-2020 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.phoebus.applications.alarm.ui.tree;

import static org.phoebus.applications.alarm.AlarmSystem.logger;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.phoebus.applications.alarm.AlarmSystem;
import org.phoebus.applications.alarm.client.AlarmClient;
import org.phoebus.applications.alarm.ui.AlarmConfigSelector;
import org.phoebus.applications.alarm.ui.AlarmURI;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItemWithInput;
import org.phoebus.ui.docking.DockPane;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;

/** Alarm tree application instance (singleton)
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
class AlarmTreeInstance implements AppInstance
{
    private final AlarmTreeApplication app;

    private String server = null, config_name = null;
    private AlarmClient client = null;
    private final DockItemWithInput tab;

    /** @param app Application info
     *  @param input Input to instance
     *  @throws Exception on error
     */
    public AlarmTreeInstance(final AlarmTreeApplication app, final URI input) throws Exception
    {
        this.app = app;

        tab = new DockItemWithInput(this, create(input), input, null, null);
        Platform.runLater(() -> tab.setLabel(config_name + " " + app.getDisplayName()));
        tab.addCloseCheck(() ->
        {
            dispose();
            return CompletableFuture.completedFuture(true);
        });
        DockPane.getActiveDockPane().addTab(tab);
    }

    @Override
    public AppDescriptor getAppDescriptor()
    {
        return app;
    }

    /** Create UI for input, starts alarm client
     *
     *  @param input Alarm URI, will be parsed into `server` and `config_name`
     *  @return Alarm UI
     *  @throws Exception
     */
    private Node create(final URI input) throws Exception
    {
        final String[] parsed = AlarmURI.parseAlarmURI(input);
        server = parsed[0];
        config_name = parsed[1];

        try
        {
            client = new AlarmClient(server, config_name);
            final AlarmTreeView tree_view = new AlarmTreeView(client);
            client.start();

            if (AlarmSystem.config_names.length > 0)
            {
                final AlarmConfigSelector configs = new AlarmConfigSelector(config_name, this::changeConfig);
                tree_view.getToolbar().getItems().add(0, configs);
            }

            return tree_view;
        }
        catch (final Exception ex)
        {
            logger.log(Level.WARNING, "Cannot create alarm tree for " + input, ex);
            return new Label("Cannot create alarm tree for " + input);
        }
    }

    private void changeConfig(final String new_config_name)
    {
        // Dispose existing setup
        dispose();

        try
        {
            // Use same server name, but new config_name
            final URI new_input = AlarmURI.createURI(server, new_config_name);
            tab.setContent(create(new_input));
            tab.setInput(new_input);
            Platform.runLater(() -> tab.setLabel(config_name + " " + app.getDisplayName()));
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot switch alarm tree to " + config_name, ex);
        }
    }

    private void dispose()
    {
        if (client != null)
        {
            client.shutdown();
            client = null;
        }
    }
}
