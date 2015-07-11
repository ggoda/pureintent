package pureServer.messaging.outgoing;

import pureServer.HelpRequest;
import pureServer.Helper;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class RequestClosedMessage extends Message {

	//The help received message indicates that users have received help
	//the format is: "RC; latitude; longitude; target id"
	
	private final String typeString = "RC";
	String target, id, lat, lon;
	
	public RequestClosedMessage(Helper h, HelpRequest hu){
		id = hu.getID();
		target = h.getID();
		lat = hu.getLocation().getLatitudeString();
		lon = hu.getLocation().getLongitudeString();
	}
	
	@Override
	public String serialize() {
		return typeString + ";" + lat + ";" + lon + ";" + id;
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
