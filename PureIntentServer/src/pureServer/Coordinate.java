package pureServer;

public class Coordinate {
	
	private String lat, lon;
	private double latNum, longNum;
	
	public Coordinate(String latitude, String longitude, double dLat, double dLong){
		lat = latitude;
		lon = longitude;
		longNum = dLong;
		latNum = dLat;
	}
	
	public String getLatitudeString(){
		return lat;
	}
	
	public String getLongitudeString(){
		return lon;
	}
	
	public double getLatitude(){
		return latNum;
	}
	
	public double getLongitude(){
		return longNum;
	}

}
