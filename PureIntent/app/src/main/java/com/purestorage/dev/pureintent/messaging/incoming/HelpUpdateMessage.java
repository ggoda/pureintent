package com.purestorage.dev.pureintent.messaging.incoming;

import com.purestorage.dev.pureintent.messaging.Message;
import com.purestorage.dev.pureintent.messaging.MessageType;

public class HelpUpdateMessage extends Message {
	
	/* The format of a help update:
	 * "HU;message;android id; target id"
	 * 
	 */
	
	
	private String id;
	private String target;
	private String message;
	
	public HelpUpdateMessage(String s) {
		String[] fields = s.split(";");
		
		message = fields[1].trim();
		id = fields[2].trim();
		target = fields[3].trim();
	}
	
	public String getMessage(){
		return message;
	}

	@Override
	public String serialize() {
		return null;
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.UPDATE_HELP_MESSAGE;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public String getTarget() {
		// TODO Auto-generated method stub
		return target;
	}
	
	

}
