package pureServer.messaging.incoming;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class RegistrationMessage extends Message {

	public RegistrationMessage(String s) {
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
		return MessageType.REGISTRATION_MESSAGE;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

}
