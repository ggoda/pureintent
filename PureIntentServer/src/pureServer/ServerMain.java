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
	
    /**
     * Creates a new instance of the PureIntent main server
     * @throws IOException throws an IOException if the port is already bound on
     */
    ServerMain() throws IOException{
		this.serverSocket = new ServerSocket(DEFAULT_PORT);
		this.clientSockets = Collections.synchronizedMap(new HashMap<>());
		this.requestThreads = Collections.synchronizedMap(new HashMap<>());
		this.messageQueueOut = new LinkedBlockingQueue<>();
		allHelpers = new ArrayList<Helper>();
		helperMap = Collections.synchronizedMap(new HashMap<>());
		//add method for populating list of helpers from database
	}
    
    /**
     * Creates a new instance of the PureIntent main server
     * @param port the port on which to listen for connections
     * @throws IOException throws an IOException if the port is already bound on
     */
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
	}
	
    /**
     * This method takes outgoing messages out of the blocking queue into which they were written and sends them to their intended targets
     * @throws UnknownHostException an UnknownHostException is thrown if the server is trying to message a non-existent client
     * @throws IOException an IOException is thrown if a socket connection is closed during writing
     */
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

            System.out.println(targetID);
            System.out.println(clientSockets);
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
				e.printStackTrace();
			} catch (IOException e) {
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
            
            if(firstClientMessage.getMessageType() == MessageType.NEED_HELP_MESSAGE){ //asking for aid
            	RequestThreadHandler helpRequest = new RequestThreadHandler((HelpRequestMessage)firstClientMessage, messageQueueOut, client, in);
            	// Add the client to our map and then start the handler
            	clientSockets.put(firstClientMessage.getID(), client);
            	requestThreads.put(helpRequest.getID(), helpRequest);
            	helpRequest.start();
            }else if(firstClientMessage.getMessageType() == MessageType.REGISTRATION_MESSAGE){ //register app
            	RegistrationMessage rm = (RegistrationMessage) firstClientMessage;
            	
            	//make a new helper
            	Helper newHelper = new Helper(rm.getID(), new Coordinate[] {rm.getLoc()}, rm.getThresshold(), rm.getIP());
            	System.out.println(newHelper);
            	clientSockets.put(rm.getID(), client); //add client socket to list of sockets
            	allHelpers.add(newHelper); //add to list of helpers
            	helperMap.put(rm.getID(), newHelper); //add to map of helpers
                System.out.println(clientSockets);
                
                //start a new listening thread
                new Thread(new Runnable(){ //this will handle any incoming messages after registration
                	public void run(){
                		try {
							for (String line = in.readLine(); line != null; line = in.readLine()) {
								handleNewMessageOnExistingClient(line, client, in);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                }).start();
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
            	
            }else if(firstClientMessage.getMessageType() == MessageType.HELP_RECEIVED_MESSAGE){
            	//message indicating that client has received help
            	HelpReceivedMessage hrm = (HelpReceivedMessage) firstClientMessage;
            	System.out.println("message received in server");
            	requestThreads.get(hrm.getID()).helpReceived();
            }
        }
    }
    
    /**
     * This method handles new messages that come in over an existing client connection
     * @param line the serialized message that has come in
     * @param client the socket over which the message arrived
     * @param in the buffered reader for receiving client input
     */
    public void handleNewMessageOnExistingClient(String line, Socket client, BufferedReader in){
    	Message clientMessage = Message.deserialize(line);
    	if(clientMessage.getMessageType() == MessageType.NEED_HELP_MESSAGE){
        	RequestThreadHandler helpRequest = new RequestThreadHandler((HelpRequestMessage)clientMessage, messageQueueOut, client, in);
        	// Add the client to our map and then start the handler
        	requestThreads.put(helpRequest.getID(), helpRequest);
        	helpRequest.start();
        }else if(clientMessage.getMessageType() == MessageType.ON_MY_WAY_MESSAGE){
        	//send help incoming to associated client
        	OMWMessage omw = (OMWMessage) clientMessage;
        	//add the responder to the list of active responders, as they get updates first
        	requestThreads.get(omw.getTarget()).helperResponding(helperMap.get(omw.getID()));
        	try {
				messageQueueOut.put(omw); //forward the on my way message to the person who needs help
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        }else if(clientMessage.getMessageType() == MessageType.UPDATE_SETTINGS_MESSAGE){
        	//add methods for updating a user's settings
        }else if(clientMessage.getMessageType() == MessageType.HELP_RECEIVED_MESSAGE){
        	//message indicating that client has received help
        	HelpReceivedMessage hrm = (HelpReceivedMessage) clientMessage;
        	System.out.println("message received in server");
        	requestThreads.get(hrm.getID()).helpReceived();
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
