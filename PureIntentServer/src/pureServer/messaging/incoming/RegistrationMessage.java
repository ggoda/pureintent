package pureServer.messaging.incoming;

import pureServer.Coordinate;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class RegistrationMessage extends Message {
	
	String id, lat, lon;
	double nLat, nLon;
	int threshhold;
	Coordinate loc;
	
	/*
	 * The registration message will have the format:
	 * R;string latitude; string longitude; number latitude; number longitude; threshhold; android id
	 */
	

	public RegistrationMessage(String s) {
		String[] fields = s.split(";");
		id = fields[6];
		lat = fields[1];
		lon = fields[2];
		nLat = Double.parseDouble(fields[3]);
		nLon = Double.parseDouble(fields[4]);
		threshhold = Integer.parseInt(fields[5]);
		
		loc = new Coordinate(lat, lon, nLat, nLon);
	}
	
	public Coordinate getLoc(){
		return loc;
	}
	
	public int getThresshold(){
		return threshhold;
	}

	@Override
	public String serialize() {
		return null;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.REGISTRATION_MESSAGE;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getTarget() {
		return id;
	}

}
