package com.windhoverlabs.yamcs.script;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.processor.ProcessorClient.CommandBuilder;
import org.yamcs.protobuf.IssueCommandRequest.Assignment;

import com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance;
import com.windhoverlabs.commander.applications.connections.Tree;
import com.windhoverlabs.commander.core.CMDR_YamcsInstance;
import com.windhoverlabs.pv.yamcs.YamcsPlugin;
import com.windhoverlabs.yamcs.commanding.CommandParser;
import com.windhoverlabs.yamcs.commanding.CommandParser.ParseResult;

public class Yamcs {

	public static Logger log = Logger.getLogger(Yamcs.class.getName());

	private static CMDR_YamcsInstance getInstance(String server, String instance) {
		return ConnectionsManagerInstance.getServerTree().getServerFromName(server).getInstance(instance);
	}

	/**
	 * Sample use:
	 *
	 * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
	 */
	public static void issueCommand(String commandText, String server, String instance) {
		
		ProcessorClient processor = getInstance(server, instance).getYamcsProcessor();
		ParseResult parsed = CommandParser.parseCommand(commandText);

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
	 * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {"voltage_num": 1});
	 */
	public static void issueCommand(String command,  String server, String instance, Map<String, Object> args) {
		ProcessorClient processor = YamcsPlugin.getProcessorClient();
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
