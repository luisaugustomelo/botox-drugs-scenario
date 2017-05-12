package co.pextra.botox;


import org.kie.api.runtime.KieSession;


public class RuleEngineThread extends Thread {
    private KieSession ksession;
    public RuleEngineThread(KieSession ksession) {
        this.ksession = ksession;
    }
    public void run() {
        this.ksession.fireUntilHalt();
    }
}