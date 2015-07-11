package pureServer.messaging.outgoing;

import pureServer.Helper;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelpIncomingMessage extends Message {
	
	Helper incomingHelp;
	private final String typeString = "HI";

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.HELP_INCOMING_MESSAGE;
	}

}
