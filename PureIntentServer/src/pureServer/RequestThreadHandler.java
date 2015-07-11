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
	
	public RequestThreadHandler(HelpRequestMessage h, BlockingQueue<Message> messageQueueOut, Socket client, BufferedReader inStream){
		helpersAsked = new ArrayBlockingQueue<Helper>(5000); //list of all helpers asked for help
		helpersResponding = new LinkedBlockingQueue<Helper>(); //list of helpers who said they're responding
		request = new HelpRequest(h, helpersAsked, helpersResponding); //the help request object
		id = request.getID(); //id of the phone requesting help
		out = messageQueueOut; //the blocking queue for sending out messages
		client = needsHelp; //the client socket
		in = inStream;
	}
	
	public void helpReceived(){
		request.helpReceived();
	}
	
	public String getID(){
		return id;
	}
	
	public void helperResponding(Helper h){
		request.helperResponding(h);
	}
	
	public void run(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
		            for (String line = in.readLine(); line != null; line = in.readLine()) {
		            	Message m = Message.deserialize(line);
		            	if(m.getMessageType() == MessageType.UPDATE_HELP_MESSAGE){
		            		HelpUpdateMessage hum = (HelpUpdateMessage) m;
		            		request.updateReceived(hum.getMessage());
		            		for(Helper responder : helpersResponding){
		            			HelperUpdateMessage hrum = new HelperUpdateMessage(responder, request);
		            			try {
		            				out.put(hrum);
		            			} catch (InterruptedException e) {
		            				e.printStackTrace();
		            			}
		            		}
		            		for(Helper asked : helpersAsked){
		            			HelperUpdateMessage hrum = new HelperUpdateMessage(asked, request);
		            			try {
		            				out.put(hrum);
		            			} catch (InterruptedException e) {
		            				e.printStackTrace();
		            			}
		            		}
		            	}else if(m.getMessageType() == MessageType.HELP_RECEIVED_MESSAGE){
		                	request.helpReceived();
		                	in.close();
		            	}
		            }
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						in.close();
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
		for(Helper responder : helpersResponding){
			RequestClosedMessage rcm = new RequestClosedMessage(responder, request);
			try {
				out.put(rcm);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(Helper asked : helpersAsked){
			RequestClosedMessage rcm = new RequestClosedMessage(asked, request);
			try {
				out.put(rcm);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			needsHelp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
