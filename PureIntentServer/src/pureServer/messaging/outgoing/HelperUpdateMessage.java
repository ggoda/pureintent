package pureServer.messaging.outgoing;

import pureServer.HelpRequest;
import pureServer.Helper;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelperUpdateMessage extends Message {
	
	/* The format for a help request message will be
	 * HRU; update message; latitude; longitude; android id; target id
	 * 
	 */
	
	String target, id, message, lat, lon;
	private final String typeString = "HRU";
	
	public HelperUpdateMessage(String s){
		String[] fields = s.split(";");
		message = fields[1];
		lat = fields[2];
		lon = fields[3];
		id = fields[4];
		target = fields[5];
	}
	
	public HelperUpdateMessage(Helper h, HelpRequest hu){
		target = h.getID();
		id = hu.getID();
		message = hu.updateMessage();
		lat = hu.getLocation().getLatitudeString();
		lon = hu.getLocation().getLongitudeString();
	}
	
	public String getLat(){
		return lat;
	}
	
	public String getLon(){
		return lon;
	}
	
	public String getMessage(){
		return message;
	}

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return typeString + ";" + message + ";" + id + ";" + target;
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.UPDATE_HELPER_MESSAGE;
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
