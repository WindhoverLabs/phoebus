package com.windhoverlabs.yamcs.commanding;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.yamcs.protobuf.IssueCommandRequest.Assignment;

import com.windhoverlabs.yamcs.script.Yamcs;

/**
 * Hand-written ugly command parser. Follows some very simple logic:
 *
 * <ul>
 * <li>removes all whitespace
 * <li>puts everything in one MetaCommand xtce
 * </ul>
 */

public class CommandParser {
	public static Logger log = Logger.getLogger(CommandParser.class.getName());

	public static ParseResult parseCommand(String commandString) {
		if (commandString == null) {
			return null;
		}

		commandString = commandString.trim();

		String serverName = null;
		String instanceName = null;
		try {
			//TODO: Clean this up a bit
			serverName = commandString.split(":")[0];
			instanceName = commandString.split(":")[1].split("/")[0];
		} catch (Exception e) {
			log.warning(
					"Error on expression \"" + commandString + "\". Verify that the instance and server names exist.");
			return null;
		}

		commandString = commandString.split(":")[1].substring(commandString.split(":")[1].indexOf('/'));

		int lparen = commandString.indexOf('(');
		ParseResult result = new ParseResult();
		result.server = serverName;
		result.instance = instanceName;

		String commandName = commandString.substring(0, lparen).trim();
		result.qualifiedName = commandName.trim();

		String argString = commandString.substring(lparen + 1, commandString.length() - 1);
		String[] args = argString.split(",");
		for (String arg : args) {
			arg = arg.trim();
			if (!arg.isEmpty()) {
				String[] kvp = arg.split(":");
				String name = kvp[0].trim();
				String value = kvp[1].trim();
				if (value.length() >= 2) {
					if ((value.startsWith("'") && value.endsWith("'"))
							|| value.startsWith("\"") && value.endsWith("\"")) {
						value = value.substring(1, value.length() - 1);
						value = value.replace("\\\"", "\"").replace("\\'", "'");
					}
				}
				result.assignments.add(Assignment.newBuilder().setName(name).setValue(value).build());
			}
		}

		return result;
	}

	public static class ParseResult {
		private String qualifiedName;

		public String getServer() {
			return server;
		}

		public String getInstance() {
			return instance;
		}

		private String server;
		private String instance;
		private List<Assignment> assignments = new ArrayList<>();

		public String getQualifiedName() {
			return qualifiedName;
		}

		public List<Assignment> getAssignments() {
			return assignments;
		}
	}
}
