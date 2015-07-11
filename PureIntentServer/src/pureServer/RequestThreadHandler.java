package pureServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;
import pureServer.messaging.incoming.HelpRequestMessage;
import pureServer.messaging.incoming.HelpUpdateMessage;
import pureServer.messaging.outgoing.HelperRequestMessage;
import pureServer.messaging.outgoing.HelperUpdateMessage;
import pureServer.messaging.outgoing.RequestClosedMessage;

public class RequestThreadHandler extends Thread{
	
	private HelpRequest request;
	ArrayBlockingQueue<Helper> helpersAsked;
	LinkedBlockingQueue<Helper> helpersResponding;
	BlockingQueue<Message> out;
	private String id;
	private Socket needsHelp;
	private BufferedReader in;
	private Object closeLock;
	
	/**
	 * Creates a new instance of the RequestThreadHandler class, to handle all incoming communications for a help request
	 * @param h
	 * @param messageQueueOut
	 * @param client
	 * @param inStream
	 */
	public RequestThreadHandler(HelpRequestMessage h, BlockingQueue<Message> messageQueueOut, Socket client, BufferedReader inStream){
		helpersAsked = new ArrayBlockingQueue<Helper>(5000); //list of all helpers asked for help
		helpersResponding = new LinkedBlockingQueue<Helper>(); //list of helpers who said they're responding
		request = new HelpRequest(h, helpersAsked, helpersResponding); //the help request object
		id = request.getID(); //id of the phone requesting help
		out = messageQueueOut; //the blocking queue for sending out messages
		client = needsHelp; //the client socket
		in = inStream;
		closeLock = new Object();
	}
	
	/**
	 * This method marks the request as closed, as help was received
	 */
	public void helpReceived(){
		request.helpReceived();
	}
	
	/**
	 * 
	 * @return the Android ID of the person requesting help
	 */
	public String getID(){
		return id;
	}
	
	/**
	 * Adds a volunteer who has said they are on their way to the list of active responders
	 * @param h the volunteer
	 */
	public void helperResponding(Helper h){
		request.helperResponding(h);
	}
	
	public void run(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					synchronized(closeLock){
			            for (String line = in.readLine(); line != null; line = in.readLine()) {
			            	Message m = Message.deserialize(line);
			            	if(m.getMessageType() == MessageType.UPDATE_HELP_MESSAGE){ //update information for responders
			            		HelpUpdateMessage hum = (HelpUpdateMessage) m;
			            		request.updateReceived(hum.getMessage()); //add update message to help request
			            		for(Helper responder : helpersResponding){ //forward update to active responders first
			            			HelperUpdateMessage hrum = new HelperUpdateMessage(responder, request);
			            			try {
			            				out.put(hrum); //put update message in out queue 
			            			} catch (InterruptedException e) {
			            				e.printStackTrace();
			            			}
			            		}
			            		for(Helper asked : helpersAsked){ //forward update to everyone alerted after
			            			HelperUpdateMessage hrum = new HelperUpdateMessage(asked, request);
			            			try {
			            				out.put(hrum); //put update message in out queue
			            			} catch (InterruptedException e) {
			            				e.printStackTrace();
			            			}
			            		}
			            	}else if(m.getMessageType() == MessageType.HELP_RECEIVED_MESSAGE){ //message saying help was received
			                	System.out.println("message received in rth");
			            		request.helpReceived(); //mark request as closed
			                	in.close(); //close buffer
			                	break;
			            	}
			            }
					}
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						in.close(); //close buffer when nothing is coming in
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}).start();
		
		while(request.helpNeeded()){
			Coordinate loc = request.getLocation();
			for(Helper h : ServerMain.allHelpers){ //look through all helpers
				if(h.nearLoc(loc) && h.getID()!=id){ //if they're close enough
					try {
						HelperRequestMessage m = new HelperRequestMessage(h, request); //create a request message
						out.put(m); //put it in the queue for the server to write out
						helpersAsked.put(h); //note that they're in range
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//help is no longer needed
		for(Helper responder : helpersResponding){ //close out and notify active responders first
			RequestClosedMessage rcm = new RequestClosedMessage(responder, request);
			try {
				out.put(rcm);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(Helper asked : helpersAsked){ //close out and notify everyone who got an alert in general after
			RequestClosedMessage rcm = new RequestClosedMessage(asked, request);
			try {
				out.put(rcm);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			synchronized(closeLock){
				in.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
