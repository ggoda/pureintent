package com.purestorage.dev.pureintent.messaging.outgoing;


import com.purestorage.dev.pureintent.messaging.Message;
import com.purestorage.dev.pureintent.messaging.MessageType;

public class HelperRequestMessage extends Message {
	
	/* The format for a help request message will be
	 * HR; String rep of Latitude; String rep of Longitude; android id; target id
	 * 
	 */
	
	private final String typeString = "HR";
	String target, id;
	String lat, lon;
	
	public HelperRequestMessage(String s){
		String[] fields = s.split(";");
		lat = fields[1];
		lon = fields[2];
		id = fields[3];
		target = fields[4];
	}
	
	public String getLatitude(){
		return lat;
	}
	
	public String getLongitude(){
		return lon;
	}

	@Override
	public String serialize() {
		return typeString + ";" + lat + ";" + lon + ";" + id + ";" + target;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.GET_HELPER_MESSAGE;
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
