package mockupClient;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import pureServer.messaging.Message;
import pureServer.messaging.MessageType;
import pureServer.messaging.incoming.OMWMessage;
import pureServer.messaging.incoming.RegistrationMessage;
import pureServer.messaging.outgoing.HelperRequestMessage;
import pureServer.messaging.outgoing.HelperUpdateMessage;

public class PhoneMockup extends JFrame{
	
	private static final long serialVersionUID = 1L; // required by Serializable
	private int thresh = 10;
	String id, lat, lon;
	double nLat, nLon;
	Socket connection;
	PrintWriter messageWriter;
	
	public PhoneMockup(String uid, String latString, String lonString, double lat, double lon){
		id = uid;
		this.lat = latString;
		this.lon = lonString;
		nLat = lat;
		nLon = lon;
		
		setLayout(new BorderLayout());
	    setContentPane(new JLabel(new ImageIcon("/Users/azaria.zornberg/phoneScreen.png")));
		
		setSize(300, 550);
		
		try {
			connection = new Socket("127.0.0.1", 10987);
			
			messageWriter = new PrintWriter(connection.getOutputStream());
	        
	        RegistrationMessage rm = new RegistrationMessage(this.lat, this.lon, nLat, nLon, thresh, "127.0.0.1", id);
			messageWriter.println(rm.serialize());
			messageWriter.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			new Thread(new Runnable(){
				public void run(){
					try {
						for (String line = in.readLine(); line != null; line = in.readLine()) {
							handleServerMessages(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleServerMessages(String line){
		System.out.println(line);
		Message m = Message.deserialize(line);
		if(m.getMessageType() == MessageType.ON_MY_WAY_MESSAGE){
			JOptionPane.showMessageDialog(this, "Help is on the way!");
		}else if(m.getMessageType() == MessageType.GET_HELPER_MESSAGE){
			//display dialog
			String requestString = "Emergency Situation At: ";
			HelperRequestMessage hr = (HelperRequestMessage) m;
			requestString += hr.getLatitude() + ", " + hr.getLongitude() + ". Can you assist?";
			int dialogButton = JOptionPane.YES_NO_OPTION;
            int response = JOptionPane.showConfirmDialog (null, requestString,"Emergency Help Needed",dialogButton);
			
            if(response == JOptionPane.YES_OPTION){
            	dispatchOMW(hr.getID());
            }
			
		}else if(m.getMessageType() == MessageType.UPDATE_HELPER_MESSAGE){
			HelperUpdateMessage hum = (HelperUpdateMessage) m;
			String updateString = "Update at " + hum.getLat() +", " + hum.getLon() + ": " + hum.getMessage();
			JOptionPane.showMessageDialog(this, updateString);
			
		}else if(m.getMessageType() == MessageType.REQUEST_CLOSED_MESSAGE){
			
		}
	}
	
	public void dispatchOMW(String target){
		System.out.println("omw reached");
		OMWMessage omw = new OMWMessage(target, id);
        messageWriter.println(omw.serialize());
        messageWriter.flush();
        System.out.println(omw.serialize());
	}
	
	public static void main(String[] args){
		PhoneMockup mockup = new PhoneMockup("mockupPhone", "N37 23 16", "W122 04 57", 37.387953, -122.082736);
		mockup.setVisible(true);
	}

}
