/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.windhoverlabs.yamcs.applications.connections;

import org.phoebus.framework.nls.NLS;

/**
 * Eclipse string externalization
 *
 * @author Lorenzo Gomez
 */
public class Messages {
  // ---
  // --- Keep alphabetically sorted and 'in sync' with messages.properties!
  // ---
  public static String AddConnection;
  public static String Connect;
  public static String ConnectAll;
  public static String Disconnect;
  public static String DisconnectAll;
  public static String DisplayName;
  public static String EditConnection;
  public static String MenuPath;
  public static String RemoveConnection;
  public static String SetDefault;
  public static String SwitchProcessor;
  // ---
  // --- Keep alphabetically sorted and 'in sync' with messages.properties!
  // ---

  static {
    NLS.initializeMessages(Messages.class);
  }

  private Messages() {
    // Prevent instantiation
  }
}
