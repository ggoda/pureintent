package com.purestorage.dev.pureintent.messaging.incoming;

import com.purestorage.dev.pureintent.Coordinate;
import com.purestorage.dev.pureintent.messaging.Message;
import com.purestorage.dev.pureintent.messaging.MessageType;

public class RegistrationMessage extends Message {
	
	String id, lat, lon;
	double nLat, nLon;
	int threshhold;
	String ip;
	Coordinate loc;
	
	/*
	 * The registration message will have the format:
	 * R;string latitude; string longitude; number latitude; number longitude; threshhold; ip; android id
	 */
	
	public RegistrationMessage(String sLat, String sLon, double dLat, double dLon, int t, String i, String uid){
		lat = sLat;
		lon = sLon;
		id = uid;
		nLat = dLat;
		nLon = dLon;
		ip = i;
		threshhold = t;
		
		loc = new Coordinate(lat, lon, nLat, nLon);
	}

	public RegistrationMessage(String s) {
		String[] fields = s.split(";");
		id = fields[7];
		ip = fields[6];
		lat = fields[1];
		lon = fields[2];
		nLat = Double.parseDouble(fields[3]);
		nLon = Double.parseDouble(fields[4]);
		threshhold = Integer.parseInt(fields[5]);
		
		loc = new Coordinate(lat, lon, nLat, nLon);
	}
	
	public String getIP(){
		return ip;
	}
	
	public Coordinate getLoc(){
		return loc;
	}
	
	public int getThresshold(){
		return threshhold;
	}

	@Override
	public String serialize() {
		return "R;" + lat + ";" + lon + ";" + nLat + ";" + nLon + ";" + threshhold + ";" + ip + ";" + id ;
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
