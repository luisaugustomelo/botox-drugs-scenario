package co.pextra.botox;

public class Sensor {
	private int sensor_id;
	private String description;
	
	public Sensor(int sensor_id, String description){
		this.sensor_id = sensor_id;
		this.description = description;
	}
	
	public int getSensorId() {
		return sensor_id;
	}
	
	public void setSensorId(int sensor_id) {
		this.sensor_id = sensor_id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	

}
