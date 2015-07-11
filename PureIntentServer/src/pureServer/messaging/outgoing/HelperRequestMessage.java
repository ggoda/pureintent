package pureServer.messaging.outgoing;

import pureServer.HelpRequest;
import pureServer.Helper;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelperRequestMessage extends Message {
	
	/* The format for a help request message will be
	 * HR; String rep of Latitude; String rep of Longitude; android id; target id
	 * 
	 */
	
	private final String typeString = "HR";
	String target, id;
	String lat, lon;
	
	public HelperRequestMessage(Helper h, HelpRequest req){
		target = h.getID();
		id = req.getID();
		lat = req.getLocation().getLatitudeString();
		lon = req.getLocation().getLongitudeString();
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
