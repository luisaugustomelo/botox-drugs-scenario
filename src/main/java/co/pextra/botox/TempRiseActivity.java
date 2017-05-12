package co.pextra.botox;

import java.util.Date;
import java.util.List;

import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("executionTime")
public class TempRiseActivity {
    private  Data data;
    private List<TemperatureEvent> transactionEvents;
    private  String description;
    private Date executionTime;
    
    public TempRiseActivity(Data data, List<TemperatureEvent> transactionEvents, String description) {
        this.data = data;
        this.transactionEvents = transactionEvents;
        this.description = description;
        this.executionTime = new Date();
    }

    @Override
    public String toString() {
        return "Temp:" + data.getTemperature() + ", Transactions Amount:" + transactionEvents.size() + ", description: " + description;
    }

    public Data getData() {
        return data;
    }

    public void setClient(Data data) {
        this.data = data;
    }

    public List<TemperatureEvent> getTransactionEvents() {
        return transactionEvents;
    }

    public void setTransactionEvents(List<TemperatureEvent> transactionEvents) {
        this.transactionEvents = transactionEvents;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }
}
