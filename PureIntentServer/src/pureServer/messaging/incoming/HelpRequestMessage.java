package pureServer.messaging.incoming;

import pureServer.Coordinate;
import pureServer.messaging.Message;
import pureServer.messaging.MessageType;

public class HelpRequestMessage extends Message {
	
	/* The format for a help request message will be
	 * H; String rep of Latitude; String rep of Longitude; Latitude; Longitude; android id 
	 */
	
	private String lat, lon, id;
	private double dLat, dLon;
	private Coordinate loc;

	public HelpRequestMessage(String s) {
		String[] fields = s.split(";");
		
		lat = fields[1].trim();
		lon = fields[2].trim();
		dLat = Double.parseDouble(fields[3].trim());
		dLon = Double.parseDouble(fields[4].trim());
		id = fields[4];
		
		loc = new Coordinate(lat, lon, dLat, dLon);
	}
	
	/**
	 * 
	 * @return the location from which the help request was sent
	 */
	public Coordinate getCoordinates(){
		return loc;
	}
	
	public String getID(){
		return id;
	}

	@Override
	public String serialize() {
		return "H;" + lat + ";" + lon + ";" + dLat + ";" + dLon;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.NEED_HELP_MESSAGE;
	}

	@Override
	public String getTarget() {
		//help messages target themselves. the sender also wants to receive responses
		return id;
	}

}
