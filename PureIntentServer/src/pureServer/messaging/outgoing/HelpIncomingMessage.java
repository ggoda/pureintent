package pureServer.messaging.outgoing;

import pureServer.HelpRequest;
import pureServer.Helper;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelpIncomingMessage extends Message {
	
	/* The format for a help incoming message will be
	 * HR; update message; android id; target id
	 * 
	 * the target is the one who sent the help request
	 * the id is the id of the helper
	 */
	
	private final String typeString = "HI";
	String target, id;
	
	public HelpIncomingMessage(Helper h, HelpRequest hu){
		target = hu.getID();
		id = h.getID();
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
