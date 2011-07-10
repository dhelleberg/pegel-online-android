package org.cirrus.mobi.pegel.data;

public class MeasureEntry {
	String messung;
	String tendenz;
	String zeit;
	String lat;
	String lon;
	public MeasureEntry(String messung, String tendenz, String zeit,
			String lat, String lon) {
		super();
		this.messung = messung;
		this.tendenz = tendenz;
		this.zeit = zeit;
		this.lat = lat;
		this.lon = lon;
	}
	public String getMessung() {
		return messung;
	}
	public String getTendenz() {
		return tendenz;
	}
	public String getZeit() {
		return zeit;
	}
	public String getLat() {
		return lat;
	}
	public String getLon() {
		return lon;
	}
	
	

}
