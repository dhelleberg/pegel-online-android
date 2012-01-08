package org.cirrus.mobi.pegel.data;

public class MeasureStationDetails {

	String name;
	String description;
	String unit;
	String value;
	
	public MeasureStationDetails(String name, String description, String unit,
			String value) {
		super();
		this.name = name;
		this.description = description;
		this.unit = unit;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUnit() {
		return unit;
	}

	public String getValue() {
		return value;
	}
	
	public String[] toStringArray() {
		String[] values = new String[4];
		values[0] = name;
		values[1] = description;
		values[2] = unit;
		values[3] = value;
		return values;
	}
	
}
