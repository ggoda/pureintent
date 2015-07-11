package pureServer.messaging.incoming;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class SettingsUpdateMessage extends Message {

	public SettingsUpdateMessage(String s) {
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
		return MessageType.UPDATE_SETTINGS_MESSAGE;
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
