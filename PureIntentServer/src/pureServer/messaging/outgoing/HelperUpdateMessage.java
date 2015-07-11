package pureServer.messaging.outgoing;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelperUpdateMessage extends Message {
	
	private final String typeString = "HRU";

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.UPDATE_HELPER_MESSAGE;
	}

}
