package com.windhoverlabs.yamcs.script;

import com.windhoverlabs.commander.core.CMDR_YamcsInstance;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.pv.yamcs.YamcsPVFactory;
import com.windhoverlabs.pv.yamcs.YamcsPlugin;
import com.windhoverlabs.yamcs.commanding.CommandParser;
import com.windhoverlabs.yamcs.commanding.CommandParser.ParseResult;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.csstudio.display.builder.model.Widget;
import org.phoebus.framework.macros.MacroHandler;
import org.phoebus.framework.macros.Macros;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.processor.ProcessorClient.CommandBuilder;
import org.yamcs.protobuf.IssueCommandRequest.Assignment;

public class Yamcs {

  public static Logger log = Logger.getLogger(Yamcs.class.getName());

  private static CMDR_YamcsInstance getInstance(String serverName, String instanceName) {
    YamcsServer server = YamcsObjectManager.getServerFromName(serverName);

    if (server == null) {
      log.warning("Server \"" + serverName + "\" not found.");
      return null;
    }
    CMDR_YamcsInstance instance =
        YamcsObjectManager.getServerFromName(serverName).getInstance(instanceName);

    if (instance == null) {
      log.warning("Instance \"" + instanceName + "\" not found.");
      return null;
    }

    return YamcsObjectManager.getServerFromName(serverName).getInstance(instanceName);
  }

  /**
   * Sample use:
   *
   * <p>Yamcs.issueCommand('sitl:yamcs-cfs/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
   */
  public static void issueCommand(String commandText) {
    ParseResult parsed = CommandParser.parseCommand(commandText);

    ProcessorClient processor =
        getInstance(parsed.getServer(), parsed.getInstance()).getYamcsProcessor();

    if (processor == null) {
      log.warning("No active processor");
      return;
    }

    CommandBuilder builder =
        processor
            .prepareCommand(parsed.getQualifiedName())
            .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
    for (Assignment arg : parsed.getAssignments()) {
      builder.withArgument(arg.getName(), arg.getValue());
    }
    builder.issue();
  }

  /**
   * Sample use:
   *
   * <p>Yamcs.issueCommand('sitl:yamcs-cfs/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {"voltage_num": 1});
   * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {"voltage_num": 1});
   */
  public static void issueCommand(String command, Map<String, Object> args) {

    if (command == null) {
      return;
    }

    command = command.trim();

    String serverName = null;
    String instanceName = null;
    try {
      // TODO: Clean this up a bit
      serverName = command.split(":")[0];
      if (serverName.contains("/")) {
        serverName = serverName.substring(serverName.indexOf("/") + 1);
      }
      instanceName = command.split(":")[1].split("/")[0];
    } catch (Exception e) {
      log.warning(
          "Error on expression \""
              + command
              + "\". Verify that the instance and server names exist.");
      return;
    }

    command = command.split(":")[1].substring(command.split(":")[1].indexOf('/'));
    ProcessorClient processor = getInstance(serverName, instanceName).getYamcsProcessor();

    if (processor == null) {
      log.warning("No active processor");
      return;
    }

    CommandBuilder builder =
        processor
            .prepareCommand(command)
            .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
    if (args != null) {
      for (Entry<String, Object> arg : args.entrySet()) {
        builder.withArgument(arg.getKey(), String.valueOf(arg.getValue()));
      }
    }
    builder.issue();
  }

  public static void issueCommand(Widget widget, String commandText) {
    /* TODO: Finish this. */
    try {
      Macros macros = widget.getEffectiveMacros();
      String expanded_commandText = MacroHandler.replace(macros, commandText);

      issueCommand(expanded_commandText);
    } catch (Exception e) {
      log.warning("FINISH HIM!");
    }
  }

  /* TODO: Expand the macros too. */
  public static void issueCommand(Widget widget, String commandText, Map<String, Object> args) {
    /* TODO: Finish this. */
    try {
      Macros macros = widget.getEffectiveMacros();
      String expanded_commandText = MacroHandler.replace(macros, commandText);
      expanded_commandText = YamcsPVFactory.sanitizePVName(expanded_commandText);

      issueCommand(expanded_commandText, args);
    } catch (Exception e) {
      // TODO
      log.warning("FINISH HIM!");
    }
  }
}
