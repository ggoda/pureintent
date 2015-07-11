package pureServer.messaging.incoming;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelpReceivedMessage extends Message {

	//The help received message indicates that users have received help
	//the format is: "HRE; android id"
	
	String id, target;
	public HelpReceivedMessage(String s){
		String[] fields = s.split(";");
		id = fields[1];
		target = fields[1];
	}
	
	@Override
	public String serialize() {
		return "HRE" + ";" + id;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.HELP_RECEIVED_MESSAGE;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getTarget() {
		return target;
	}

}
