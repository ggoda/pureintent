package com.purestorage.dev.pureintent.messaging.outgoing;


import com.purestorage.dev.pureintent.messaging.Message;
import com.purestorage.dev.pureintent.messaging.MessageType;

public class RequestClosedMessage extends Message {

	//The help received message indicates that users have received help
	//the format is: "RC; latitude; longitude; target id"
	
	private final String typeString = "RC";
	String target, id, lat, lon;
	
	public RequestClosedMessage(String s){
		String[] fields = s.split(";");
		target = fields[3];
		lat = fields[1];
		lon = fields[2];
	}
	
	@Override
	public String serialize() {
		return typeString + ";" + lat + ";" + lon + ";" + target;
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
