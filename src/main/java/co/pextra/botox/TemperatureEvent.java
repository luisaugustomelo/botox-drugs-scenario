package co.pextra.botox;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("executionTime")
@Expires("2m")
public class TemperatureEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    private Date executionTime;
    private Double temperature;
    private int sensor_id;

    public TemperatureEvent(Double temperature, int sensor_id) {
        super();
        this.executionTime = new Date();
        this.temperature = temperature;
        this.sensor_id = sensor_id;
    }

    public TemperatureEvent(Double temperature, int sensor_id, Date executionTime) {
        super();
        this.executionTime = executionTime;
        this.temperature = temperature;
        this.sensor_id = sensor_id;
    }


    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temp) {
        this.temperature = temp;
    }

    public int getSensorId() {
        return sensor_id;
    }

    public void setSensorId(int sensor_id) {
        this.sensor_id = sensor_id;
    }
}