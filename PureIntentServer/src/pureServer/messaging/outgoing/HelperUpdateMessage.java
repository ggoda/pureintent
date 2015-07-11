package pureServer.messaging.outgoing;

import pureServer.HelpRequest;
import pureServer.Helper;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelperUpdateMessage extends Message {
	
	/* The format for a help request message will be
	 * HR; update message; android id; target id
	 * 
	 */
	
	String target, id, message;
	private final String typeString = "HRU";
	
	public HelperUpdateMessage(Helper h, HelpRequest hu){
		target = h.getID();
		id = hu.getID();
		message = hu.updateMessage();
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
