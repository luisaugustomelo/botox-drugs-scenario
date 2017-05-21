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
    private int sensorId;

    public TemperatureEvent(Double temperature, int sensorId) {
        super();
        this.executionTime = new Date();
        this.temperature = temperature;
        this.sensorId = sensorId;
    }

    public TemperatureEvent(Double temperature, int sensorId, Date executionTime) {
        super();
        this.executionTime = executionTime;
        this.temperature = temperature;
        this.sensorId = sensorId;
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
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }
}