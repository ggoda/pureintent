package pureServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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

public class ServerMain {
	
	/* This class will handle incoming messages and create threads for each help request
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
	}
	
	public void handleMessagesInQueue() {
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
            Socket targetSocket = clientSockets.get(nextMessage.getTarget());
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
               handleMessagesInQueue();
           }
        }).start();
        
        // Now handle messages from clients
        while (true) {
            Socket client = serverSocket.accept();

            // Get the handshake message from the client. We cast to make sure that the client actually sent a handshake.
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            Message firstClientMessage = Message.deserialize(in.readLine());
            
            if(firstClientMessage.getMessageType() == MessageType.NEED_HELP_MESSAGE){
            	RequestThreadHandler helpRequest = new RequestThreadHandler((HelpRequestMessage)firstClientMessage, messageQueueOut, client, in);
            	// Add the client to our map and then start the handler
            	clientSockets.put(firstClientMessage.getID(), client);
            	helpRequest.start();
            }else if(firstClientMessage.getMessageType() == MessageType.REGISTRATION_MESSAGE){
            	//make method to register user
            	
            }else if(firstClientMessage.getMessageType() == MessageType.ON_MY_WAY_MESSAGE){
            	//send help incoming to associated client
            	OMWMessage omw = (OMWMessage) firstClientMessage;
            	requestThreads.get(omw.getTarget()).helperResponding(helperMap.get(omw.getID()));
            	try {
					messageQueueOut.put(omw);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	
            	clientSockets.put(omw.getID(), client);
            	
            }else if(firstClientMessage.getMessageType() == MessageType.UPDATE_SETTINGS_MESSAGE){
            	//add methods for updating a user's settings
            }else if(firstClientMessage.getMessageType() == MessageType.HELP_RECEIVED_MESSAGE){
            	//message indicating that client has received help
            	HelpReceivedMessage hrm = (HelpReceivedMessage) firstClientMessage;
            	requestThreads.get(hrm.getID()).helpReceived();
            }
            in.close();
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
