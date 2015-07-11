package pureServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;
import pureServer.messaging.incoming.HelpReceivedMessage;
import pureServer.messaging.incoming.HelpRequestMessage;
import pureServer.messaging.incoming.OMWMessage;
import pureServer.messaging.incoming.RegistrationMessage;

public class ServerMain {
	
	/* This class will handle incoming messages and create threads for each help request
	 * 
	 * The message encoders and types are:
	 * 
	 * incoming messages:
	 * H ---- Help Request
	 * HU --- Help Request Update
	 * HRE -- Help Received
	 * OMW -- On my way to assist
	 * R ---- Registration Message
	 * S ---- Settings Update
	 * 
	 * outgoing messages:
	 * HR --- Request for a specific volunteer's assistance
	 * HRU -- Update for specific volunteers on the person in need of assistance
	 * HI --- Help Incoming Message
	 * RC --- Request Closed, help was received and no more assistance is necessary
	 */
	private static final int DEFAULT_PORT = 10987;
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    
    private final ServerSocket serverSocket;
    private final java.util.Map<String, Socket> clientSockets;
    private final java.util.Map<String, RequestThreadHandler> requestThreads;
    private final BlockingQueue<Message> messageQueueOut;
    
    public static java.util.Map<String, Helper> helperMap;
    public static ArrayList<Helper> allHelpers;
	
    ServerMain() throws IOException{
		this.serverSocket = new ServerSocket(DEFAULT_PORT);
		this.clientSockets = Collections.synchronizedMap(new HashMap<>());
		this.requestThreads = Collections.synchronizedMap(new HashMap<>());
		this.messageQueueOut = new LinkedBlockingQueue<>();
		allHelpers = new ArrayList<Helper>();
		helperMap = Collections.synchronizedMap(new HashMap<>());
		//add method for populating list of helpers from database
		makeDummyHelpers();
	}
    
    ServerMain(int port) throws IOException{
    	if(port <= MAX_PORT && port >= MIN_PORT){
    		this.serverSocket = new ServerSocket(port);
    	}else{
    		this.serverSocket = new ServerSocket(DEFAULT_PORT);
    	}
		this.clientSockets = Collections.synchronizedMap(new HashMap<>());
		this.requestThreads = Collections.synchronizedMap(new HashMap<>());
		this.messageQueueOut = new LinkedBlockingQueue<>();
		allHelpers = new ArrayList<Helper>();
		helperMap = Collections.synchronizedMap(new HashMap<>());
		//add method for populating list of helpers from database
		makeDummyHelpers();
	}
    
    private void makeDummyHelpers(){
    	Coordinate pureStorage = new Coordinate("55N", "48E", 37.387953, -122.082736);
    	Coordinate c2 = new Coordinate("56N", "49E", 38.387953, -122.082736);
    	Helper h1 = new Helper("Android00", new Coordinate[] {pureStorage}, 50, "10.202.100.150");
    	Helper h2 = new Helper("Android01", new Coordinate[] {c2}, 0, "127.0.0.1");
    	allHelpers.add(h1);
    	allHelpers.add(h2);
    	helperMap.put("Android00", h1);
    	helperMap.put("Android01", h2);
    }
	
	public void handleMessagesInQueue() throws UnknownHostException, IOException {
        while (true) {
            
            // Grab a message from the queue
            Message nextMessage;
            try {
                nextMessage = messageQueueOut.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            
            // Write the message to the corresponding socket
            Socket targetSocket;
            String targetID = nextMessage.getTarget();
            if(clientSockets.containsKey(nextMessage.getTarget())){
            	targetSocket = clientSockets.get(targetID);
            }else{
            	Helper h = helperMap.get(targetID);
            	targetSocket = new Socket(h.getIP(), DEFAULT_PORT);
            }
            PrintWriter messageWriter;

            try {
                messageWriter = new PrintWriter(targetSocket.getOutputStream());
            } catch (IOException ioe) {
                // Try again if there was an error
                ioe.printStackTrace();
                try {
                	messageQueueOut.put(nextMessage);
                } catch (InterruptedException e) {
                    // If we can't put the message back in the queue, then we give up on the message and it gets dropped
                    e.printStackTrace();
                }
                continue;
            }
            
            messageWriter.println(nextMessage.serialize());
            messageWriter.flush();
        }
    }
	
	/**
     * Runs the server by listening for and handling client connections. Blocks the thread it is called on until the server terminates.
     */
    public void serve() throws IOException {
        // Start the outgoing message queue listener
        new Thread(new Runnable() {
           public void run() {
               try {
				handleMessagesInQueue();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           }
        }).start();
        
        // Now handle messages from clients
        while (true) {
            Socket client = serverSocket.accept();

            // Get the handshake message from the client. We cast to make sure that the client actually sent a handshake.
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String input = in.readLine();
            System.out.println(input);
            Message firstClientMessage = Message.deserialize(input);
            
            if(firstClientMessage.getMessageType() == MessageType.NEED_HELP_MESSAGE){
            	RequestThreadHandler helpRequest = new RequestThreadHandler((HelpRequestMessage)firstClientMessage, messageQueueOut, client, in);
            	// Add the client to our map and then start the handler
            	clientSockets.put(firstClientMessage.getID(), client);
            	requestThreads.put(helpRequest.getID(), helpRequest);
            	helpRequest.start();
            }else if(firstClientMessage.getMessageType() == MessageType.REGISTRATION_MESSAGE){
            	RegistrationMessage rm = (RegistrationMessage) firstClientMessage;
            	Helper newHelper = new Helper(rm.getID(), null, rm.getThresshold(), rm.getIP());
            	
            	allHelpers.add(newHelper);
            	helperMap.put(rm.getID(), newHelper);
            }else if(firstClientMessage.getMessageType() == MessageType.ON_MY_WAY_MESSAGE){
            	//send help incoming to associated client
            	OMWMessage omw = (OMWMessage) firstClientMessage;
            	//add the responder to the list of active responders, as they get updates first
            	System.out.println("target:" + omw.getTarget());
            	System.out.println("id:" + omw.getID());
            	System.out.println(requestThreads);
            	System.out.println(requestThreads.get(omw.getTarget()));
            	requestThreads.get(omw.getTarget()).helperResponding(helperMap.get(omw.getID()));
            	try {
					messageQueueOut.put(omw); //forward the on my way message to the person who needs help
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	
            	clientSockets.put(omw.getID(), client);
            	
            }else if(firstClientMessage.getMessageType() == MessageType.UPDATE_SETTINGS_MESSAGE){
            	//add methods for updating a user's settings
            }else if(firstClientMessage.getMessageType() == MessageType.HELP_RECEIVED_MESSAGE){
            	//message indicating that client has received help
            	HelpReceivedMessage hrm = (HelpReceivedMessage) firstClientMessage;
            	System.out.println("message received in rth");
            	requestThreads.get(hrm.getID()).helpReceived();
            }
        }
    }
    
    public static void main(String[] args){
    	try {
			ServerMain server = new ServerMain();
			server.serve();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
