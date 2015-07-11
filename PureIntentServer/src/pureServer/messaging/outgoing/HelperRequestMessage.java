package pureServer.messaging.outgoing;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelperRequestMessage extends Message {
	
	private final String typeString = "HR";

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.GET_HELPER_MESSAGE;
	}

}
