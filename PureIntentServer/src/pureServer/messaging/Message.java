package pureServer.messaging;

import pureServer.messaging.incoming.HelpRequestMessage;
import pureServer.messaging.incoming.HelpUpdateMessage;
import pureServer.messaging.incoming.OMWMessage;
import pureServer.messaging.incoming.RegistrationMessage;
import pureServer.messaging.incoming.SettingsUpdateMessage;

public abstract class Message {
	
	public static Message deserialize(String s){
		String[] fields = s.split(";");
		Message returnMessage = null;
		if(fields[0].equals("H")){ //code for HelpRequest
			returnMessage = new HelpRequestMessage(s);
		}else if(fields[0].equals("HU")){ //code for HelpUpdate
			returnMessage = new HelpUpdateMessage(s);
		}else if(fields[0].equals("OMW")){ //code for on my way
			returnMessage = new OMWMessage(s);
		}else if(fields[0].equals("S")){ //code for settings
			returnMessage = new SettingsUpdateMessage(s);
		}else if(fields[0].equals("R")){ //code for registration
			returnMessage = new RegistrationMessage(s);
		}
		
		return returnMessage;
	}
	
	public abstract String serialize();
	
	public abstract MessageType getMessageType();

}
