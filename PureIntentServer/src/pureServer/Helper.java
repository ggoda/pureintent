package pureServer;

public class Helper{
	
	private String id;
	private Coordinate[] locations;
	int threshhold;
	
	public Helper(String helperID, Coordinate[] normal, int t){
		id = helperID;
		locations = normal;
		threshhold = t;
	}
	
	public String getID(){
		return id;
	}
	
	public int getThreshhold(){
		return threshhold;
	}
	
	public boolean nearLoc(Coordinate loc){
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		
		for(Coordinate c : locations){
			double cLat = c.getLatitude();
			double cLon = c.getLongitude();
			double latDiff = Math.pow(68*(lat - cLat), 2);
			double lonDiff = Math.pow(55*(lon - cLon), 2);
			
			double dist = Math.sqrt(latDiff + lonDiff);
			if(dist < threshhold){
				return true;
			}
		}
		
		return false;
	}

}
