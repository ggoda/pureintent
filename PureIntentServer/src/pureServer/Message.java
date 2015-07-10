package pureServer;

public abstract class Message {
	
	public static Message deserialize(String s){
		
		return null;
	}
	
	public abstract String serialize();
	
	public abstract MessageType getMessageType();

}
