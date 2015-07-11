package pureServer.messaging.incoming;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelpUpdateMessage extends Message {

	public HelpUpdateMessage(String s) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageType getMessageType() {
		// TODO Auto-generated method stub
		return MessageType.UPDATE_HELP_MESSAGE;
	}

}
