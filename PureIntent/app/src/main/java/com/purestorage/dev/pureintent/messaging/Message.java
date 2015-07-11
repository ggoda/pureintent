package com.purestorage.dev.pureintent.messaging;

import com.purestorage.dev.pureintent.messaging.incoming.HelpReceivedMessage;
import com.purestorage.dev.pureintent.messaging.incoming.HelpRequestMessage;
import com.purestorage.dev.pureintent.messaging.incoming.HelpUpdateMessage;
import com.purestorage.dev.pureintent.messaging.incoming.OMWMessage;
import com.purestorage.dev.pureintent.messaging.incoming.RegistrationMessage;
import com.purestorage.dev.pureintent.messaging.incoming.SettingsUpdateMessage;
import com.purestorage.dev.pureintent.messaging.outgoing.HelperRequestMessage;
import com.purestorage.dev.pureintent.messaging.outgoing.HelperUpdateMessage;

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
		}else if(fields[0].equals("HR")){
			returnMessage = new HelperRequestMessage(s);
		}else if(fields[0].equals("HRU")){
			returnMessage = new HelperUpdateMessage(s);
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
