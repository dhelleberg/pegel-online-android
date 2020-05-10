package org.cirrus.mobi.pegel.data;

public class MeasurePointDataDetails {
	
	public String dataType;
	public String value;
	public String time;
	
	
	public MeasurePointDataDetails(String dataType, String value, String time) {
		super();
		this.dataType = dataType;
		this.value = value;
		this.time = time;
	}
	
	public String getDataType() {
		return dataType;
	}
	public String getValue() {
		return value;
	}
	public String getTime() {
		return time;
	}
	public String[] toStringArray() {
		String[] values = new String[3];
		values[0] = dataType;
		values[1] = value;
		values[2] = time;
		return values;
	}
}
