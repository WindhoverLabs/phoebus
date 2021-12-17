package com.windhoverlabs.yamcs.script;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.processor.ProcessorClient.CommandBuilder;
import org.yamcs.protobuf.IssueCommandRequest.Assignment;

import com.windhoverlabs.commander.core.CMDR_YamcsInstance;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.pv.yamcs.YamcsPlugin;
import com.windhoverlabs.yamcs.commanding.CommandParser;
import com.windhoverlabs.yamcs.commanding.CommandParser.ParseResult;

public class Yamcs {

	public static Logger log = Logger.getLogger(Yamcs.class.getName());

	private static CMDR_YamcsInstance getInstance(String serverName, String instanceName) {
		YamcsServer server = YamcsObjectManager.getServerFromName(serverName);

		if (server == null) {
			log.warning("Server \"" + serverName + "\" not found.");
			return null;
		}
		CMDR_YamcsInstance instance = YamcsObjectManager.getServerFromName(serverName)
				.getInstance(instanceName);

		if (instance == null) {
			log.warning("Instance \"" + instanceName + "\" not found.");
			return null;
		}

		return YamcsObjectManager.getServerFromName(serverName).getInstance(instanceName);
	}

	/**
	 * Sample use:
	 *
	 * Yamcs.issueCommand('sitl:yamcs-cfs/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
	 */
	public static void issueCommand(String commandText) {
		ParseResult parsed = CommandParser.parseCommand(commandText);

		ProcessorClient processor = getInstance(parsed.getServer(), parsed.getInstance()).getYamcsProcessor();

		if (processor == null) {
			log.warning("No active processor");
			return;
		}

		CommandBuilder builder = processor.prepareCommand(parsed.getQualifiedName())
				.withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
		for (Assignment arg : parsed.getAssignments()) {
			builder.withArgument(arg.getName(), arg.getValue());
		}
		builder.issue();
	}

	/**
	 * Sample use:
	 *
	 * Yamcs.issueCommand('sitl:yamcs-cfs/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {"voltage_num": 1});
	 */
	public static void issueCommand(String command, Map<String, Object> args) {
		
		
		if (command == null) {
			return ;
		}

		command = command.trim();

		String serverName = null;
		String instanceName = null;
		try {
			//TODO: Clean this up a bit
			serverName = command.split(":")[0];
			instanceName = command.split(":")[1].split("/")[0];
		} catch (Exception e) {
			log.warning(
					"Error on expression \"" + command + "\". Verify that the instance and server names exist.");
			return ;
		}

		command = command.split(":")[1].substring(command.split(":")[1].indexOf('/'));
		ProcessorClient processor = getInstance(serverName, instanceName).getYamcsProcessor();

		if (processor == null) {
			log.warning("No active processor");
			return;
		}

		CommandBuilder builder = processor.prepareCommand(command)
				.withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
		if (args != null) {
			for (Entry<String, Object> arg : args.entrySet()) {
				builder.withArgument(arg.getKey(), String.valueOf(arg.getValue()));
			}
		}
		builder.issue();
	}
}
