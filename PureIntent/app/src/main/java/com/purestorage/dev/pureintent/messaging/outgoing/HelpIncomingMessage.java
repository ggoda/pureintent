package com.purestorage.dev.pureintent.messaging.outgoing;


import com.purestorage.dev.pureintent.messaging.Message;
import com.purestorage.dev.pureintent.messaging.MessageType;

public class HelpIncomingMessage extends Message {
	
	/* The format for a help incoming message will be
	 * HI; android id; target id
	 * 
	 * the target is the one who sent the help request
	 * the id is the id of the helper
	 */
	
	private final String typeString = "HI";
	String target, id;
	
	public HelpIncomingMessage(String s){
		String[] fields = s.split(";");
		target = fields[2];
		id = fields[1];
	}

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return typeString + ";" + id + ";" + target;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.HELP_INCOMING_MESSAGE;
	}

}
