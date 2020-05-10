package org.cirrus.mobi.pegel.data;

public class MeasureEntry {
	public static final int STATUS_OK = 1;
	public static final int STATUS_NOT_FOUND = 2;
	String messung;
	String tendenz;
	String zeit;
	String lat;
	String lon;
	int status;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
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
