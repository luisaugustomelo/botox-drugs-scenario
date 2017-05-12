package co.pextra.botox;

public class Data {
	private double temperature;
	private int sensor_id;
	
	public Data(double temperature, int sensor_id){
		this.temperature = temperature;
		this.sensor_id = sensor_id;
	}
	
	public double getTemperature() {
		return temperature;
	}
	
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	
	public int getSensorId() {
		return sensor_id;
	}
	
	public void setSensorId(int sensor_id) {
		this.sensor_id = sensor_id;
	}
	
	

}
