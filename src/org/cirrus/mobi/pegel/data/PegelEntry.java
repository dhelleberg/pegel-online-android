package org.cirrus.mobi.pegel.data;

public class PegelEntry {
	
	String pegelname;
	String pegelnummer;
	
	
	public PegelEntry(String pegelname, String pegelnummer) {
		super();
		this.pegelname = pegelname;
		this.pegelnummer = pegelnummer;
	}
	public String getPegelname() {
		return pegelname;
	}
	public String getPegelnummer() {
		return pegelnummer;
	}

}
