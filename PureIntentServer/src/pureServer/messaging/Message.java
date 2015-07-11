package pureServer.messaging;

import pureServer.messaging.incoming.HelpReceivedMessage;
import pureServer.messaging.incoming.HelpRequestMessage;
import pureServer.messaging.incoming.HelpUpdateMessage;
import pureServer.messaging.incoming.OMWMessage;
import pureServer.messaging.incoming.RegistrationMessage;
import pureServer.messaging.incoming.SettingsUpdateMessage;

public abstract class Message {
	
	/**
	 * Converts the text of a message that the server receives into a Message object of the proper type
	 * @param s the message text as a String
	 * @return a new Message Object
	 */
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
		}else if(fields[0].equals("HRE")){ //code for registration
			returnMessage = new HelpReceivedMessage(s);
		}
		
		return returnMessage;
	}
	
	/**
	 * 
	 * @return a String serialization of the message and all of its relevant data fields, separated by commas
	 */
	public abstract String serialize();
	
	/**
	 * 
	 * @return the type of a message
	 */
	public abstract MessageType getMessageType();
	
	/**
	 * 
	 * @return the id of the phone that sent the message
	 */
	public abstract String getID();
	
	/**
	 * 
	 * @return the id of the phone that the message is targeting
	 */
	public abstract String getTarget();

}
