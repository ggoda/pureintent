package pureServer.messaging.incoming;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class OMWMessage extends Message {
	
	/* The format for an on my way message will be
	 * OMW; android id; target id
	 * 
	 */
	
	String id;
	String target;

	public OMWMessage(String s) {
		String[] fields = s.split(";");
		id = fields[1];
		target = fields[2];
	}

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return "OMW";
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.ON_MY_WAY_MESSAGE;
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
