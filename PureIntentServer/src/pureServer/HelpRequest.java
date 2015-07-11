package pureServer;

import java.util.concurrent.*;

import pureServer.messaging.incoming.HelpRequestMessage;

/**
 * This class serves to represent a request for help made. It will contain the relevant information
 * of the person who requested help, as well as the information of those who are responding or being
 * asked to respond
 */
public class HelpRequest {
	
	private Coordinate loc;
	private String updateMessage;
	ArrayBlockingQueue<Helper> helpersAsked;
	LinkedBlockingQueue<Helper> helpersResponding;
	boolean helpNeeded = true;
	String id;
	
	public HelpRequest(HelpRequestMessage h, ArrayBlockingQueue<Helper> possible, LinkedBlockingQueue<Helper> accepted){
		loc = h.getCoordinates();
		helpersAsked = possible;
		helpersResponding = accepted;
		id = h.getID();
	}
	
	public String getID(){
		return id;
	}
	
	public void updateReceived(String m){
		updateMessage = m;
	}
	
	public String updateMessage(){
		return updateMessage;
	}
	
	public Coordinate getLocation(){
		return loc;
	}
	
	public void helperResponding(Helper responder){
		helpersAsked.remove(responder);
		helpersResponding.add(responder);
	}
	
	public ArrayBlockingQueue<Helper> getHelpersAsked(){
		return helpersAsked;
	}
	
	public LinkedBlockingQueue<Helper> getResponders(){
		return helpersResponding;
	}
	
	public void helpReceived(){
		helpNeeded = false;
	}
	
	public boolean helpNeeded(){
		return helpNeeded;
	}
	
	
}
