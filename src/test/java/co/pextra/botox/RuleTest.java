package co.pextra.botox;
/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import co.pextra.botox.TemperatureEvent;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//import org.drools.core.time.SessionPseudoClock;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.drools.core.impl.InternalKnowledgeBase;
//import org.kie.api.runtime.KieSessionConfiguration;
//import org.kie.api.runtime.conf.ClockTypeOption;
//import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuleTest extends CamelTestSupport {
    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);
    protected CommandExecutor exec;
    
	static final String rules = "package co.pextra.botox;\r\n" + 
			"\r\n" + 
			"import java.util.List;\r\n" + 
			"\r\n" + 
			"import br.ufes.inf.lprm.scene.model.impl.Situation;\r\n" + 
			"import br.ufes.inf.lprm.scene.util.SituationHelper;\r\n" + 
			"import br.ufes.inf.lprm.situation.annotations.part;\r\n" + 
			"import br.ufes.inf.lprm.situation.model.events.*;\r\n" + 
			"import org.kie.api.runtime.process.ProcessInstance;\r\n" + 
			"\r\n" + 
			"declare TemperatureSituation extends Situation\r\n" + 
			"    sensor: Sensor @part\r\n" + 
			"    transactions: List @part\r\n" + 
			"end\r\n" + 
			"\r\n" + 
			"/*\r\n" + 
			"Verifica se em uma lista de 3 ou mais temperaturas existem temperaturas acima de -3 graus em uma janela de tempo de 2 minutos. \r\n" + 
			"*/\r\n" + 
			"rule \"Ocorreram 3 mudanças de temperatura com o mesmo sensor em um intervalo de 2 minutos\"\r\n" + 
			"@role(situation)\r\n" + 
			"@type(TemperatureSituation)\r\n" + 
			"    when\r\n" + 
			"       sensor : Sensor();\r\n" + 
			"       transactions: List(size > 5) from accumulate(\r\n" + 
			"          transaction: TemperatureEvent(sensorId == sensor.sensorId) over window:time (2m),\r\n" + 
			"          collectList(transaction)\r\n" + 
			"       )\r\n" + 
			"     then\r\n" + 
			"       SituationHelper.situationDetected(drools);\r\n" + 
			"end\r\n" + 
			"\r\n" + 
			"rule \"More than 5 high temperatures in two minutes for a sensor of temperature\"\r\n" + 
			"@role(situation)\r\n" + 
			"@type(TemperatureSituation)\r\n" + 
			"    when\r\n" + 
			"       sensor : Sensor();\r\n" + 
			"       transactions: List(size > 3) from accumulate(\r\n" + 
			"          transaction: TemperatureEvent(sensorId == sensor.sensorId, temperature > -3) over window:time (2m),\r\n" + 
			"          collectList(transaction)\r\n" + 
			"       )\r\n" + 
			"     then\r\n" + 
			"       SituationHelper.situationDetected(drools);\r\n" + 
			"end\r\n" + 
			"\r\n" + 
			"rule \"More than 10 high temperatures in two minutes for a sensor of temperature\"\r\n" + 
			"@role(situation)\r\n" + 
			"@type(TemperatureSituation)\r\n" + 
			"    when\r\n" + 
			"       sensor : Sensor();\r\n" + 
			"       transactions: List(size > 6) from accumulate(\r\n" + 
			"          transaction: TemperatureEvent(sensorId == sensor.sensorId, temperature > 0) over window:time (2m),\r\n" + 
			"          collectList(transaction)\r\n" + 
			"       )\r\n" + 
			"     then\r\n" + 
			"       SituationHelper.situationDetected(drools);\r\n" + 
			"end\r\n" + 
			"\r\n" + 
			"/*\r\n" + 
			"Se houver mais que 3 situações de elevação de temperatura, em uma janela de tempo de 2 minutos, \r\n" + 
			"uma ação de notificação de elevação de temperatura é disparada!\r\n" + 
			"\r\n" + 
			"## No exemplo exposto são disparados 4 situações de temperaturas acima de -3 graus. vide RuleTest.java##\r\n" + 
			"*/\r\n" + 
			"\r\n" + 
			"rule \"start botox situation when have a one case suspicious\"\r\n" + 
			"	when\r\n" + 
			"       temperatureSituation: List(size > 2) from accumulate (\r\n" + 
			"         Activation($sit: situation, $sit isA TemperatureSituation.class) over window:time (2m);\r\n" + 
			"         collectList($sit)\r\n" + 
			"       )\r\n" + 
			"	then\r\n" + 
			"		//ProcessInstance processInstance = kcontext.getKieRuntime().startProcess(\"co.pextra.botox.notification\");\r\n" + 
			"		System.out.println(\"uma situação de temperatura ocorreu\");\r\n" + 
			"end\r\n" + 
			"\r\n" + 
			"/* Criar uma regra que verifica se existem 5 SENSORES (e não ocorrencia de temperatura) em uma janela de tempo de 2 minutos com temperatura elevada.\r\n" + 
			"*\r\n" + 
			"* (...)\r\n" + 
			"*\r\n" + 
			"*\r\n" + 
			"*/";
	
    static final String process = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
    		"<!-- origin at X=0.0 Y=0.0 -->\r\n" + 
    		"<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:java=\"http://www.java.com/javaTypes\" xmlns:tns=\"http://www.jboss.org/drools\" xmlns=\"http://www.jboss.org/drools\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd http://www.jboss.org/drools drools.xsd http://www.bpsim.org/schemas/1.0 bpsim.xsd\" id=\"Definition\" exporter=\"org.eclipse.bpmn2.modeler.core\" exporterVersion=\"1.3.0.Final-v20160602-2145-B47\" expressionLanguage=\"http://www.mvel.org/2.0\" targetNamespace=\"http://www.jboss.org/drools\" typeLanguage=\"http://www.java.com/javaTypes\">\r\n" + 
    		"  <bpmn2:process id=\"co.pextra.botox.notification\" tns:packageName=\"defaultPackage\" name=\"Notification\" isExecutable=\"true\" processType=\"Private\">\r\n" + 
    		"    <bpmn2:startEvent id=\"_1\">\r\n" + 
    		"      <bpmn2:extensionElements>\r\n" + 
    		"        <tns:metaData name=\"elementname\">\r\n" + 
    		"          <tns:metaValue><![CDATA[]]></tns:metaValue>\r\n" + 
    		"        </tns:metaData>\r\n" + 
    		"      </bpmn2:extensionElements>\r\n" + 
    		"      <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>\r\n" + 
    		"    </bpmn2:startEvent>\r\n" + 
    		"    <bpmn2:endEvent id=\"EndEvent_1\">\r\n" + 
    		"      <bpmn2:extensionElements>\r\n" + 
    		"        <tns:metaData name=\"elementname\">\r\n" + 
    		"          <tns:metaValue><![CDATA[]]></tns:metaValue>\r\n" + 
    		"        </tns:metaData>\r\n" + 
    		"      </bpmn2:extensionElements>\r\n" + 
    		"      <bpmn2:incoming>SequenceFlow_4</bpmn2:incoming>\r\n" + 
    		"    </bpmn2:endEvent>\r\n" + 
    		"    <bpmn2:scriptTask id=\"ScriptTask_2\" name=\"Notifica via E-mail\" scriptFormat=\"http://www.java.com/java\">\r\n" + 
    		"      <bpmn2:extensionElements>\r\n" + 
    		"        <tns:metaData name=\"elementname\">\r\n" + 
    		"          <tns:metaValue><![CDATA[Notifica via E-mail]]></tns:metaValue>\r\n" + 
    		"        </tns:metaData>\r\n" + 
    		"      </bpmn2:extensionElements>\r\n" + 
    		"      <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>\r\n" + 
    		"      <bpmn2:outgoing>SequenceFlow_4</bpmn2:outgoing>\r\n" + 
    		"      <bpmn2:script>System.out.println(&quot;Elevação de temperatura! - BP&quot;);</bpmn2:script>\r\n" + 
    		"    </bpmn2:scriptTask>\r\n" + 
    		"    <bpmn2:sequenceFlow id=\"SequenceFlow_4\" tns:priority=\"1\" sourceRef=\"ScriptTask_2\" targetRef=\"EndEvent_1\"/>\r\n" + 
    		"    <bpmn2:sequenceFlow id=\"SequenceFlow_2\" tns:priority=\"1\" sourceRef=\"_1\" targetRef=\"ScriptTask_2\"/>\r\n" + 
    		"  </bpmn2:process>\r\n" + 
    		"  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\r\n" + 
    		"    <bpmndi:BPMNPlane id=\"BPMNPlane_Process_1\" bpmnElement=\"co.pextra.botox.notification\">\r\n" + 
    		"      <bpmndi:BPMNShape id=\"BPMNShape_StartEvent_1\" bpmnElement=\"_1\">\r\n" + 
    		"        <dc:Bounds height=\"36.0\" width=\"36.0\" x=\"70.0\" y=\"262.0\"/>\r\n" + 
    		"        <bpmndi:BPMNLabel id=\"BPMNLabel_1\"/>\r\n" + 
    		"      </bpmndi:BPMNShape>\r\n" + 
    		"      <bpmndi:BPMNShape id=\"BPMNShape_EndEvent_1\" bpmnElement=\"EndEvent_1\">\r\n" + 
    		"        <dc:Bounds height=\"36.0\" width=\"36.0\" x=\"476.0\" y=\"262.0\"/>\r\n" + 
    		"        <bpmndi:BPMNLabel id=\"BPMNLabel_2\"/>\r\n" + 
    		"      </bpmndi:BPMNShape>\r\n" + 
    		"      <bpmndi:BPMNShape id=\"BPMNShape_ScriptTask_2\" bpmnElement=\"ScriptTask_2\" isExpanded=\"true\">\r\n" + 
    		"        <dc:Bounds height=\"50.0\" width=\"110.0\" x=\"250.0\" y=\"255.0\"/>\r\n" + 
    		"        <bpmndi:BPMNLabel id=\"BPMNLabel_4\">\r\n" + 
    		"          <dc:Bounds height=\"15.0\" width=\"98.0\" x=\"256.0\" y=\"272.0\"/>\r\n" + 
    		"        </bpmndi:BPMNLabel>\r\n" + 
    		"      </bpmndi:BPMNShape>\r\n" + 
    		"      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_4\" bpmnElement=\"SequenceFlow_4\" sourceElement=\"BPMNShape_ScriptTask_2\" targetElement=\"BPMNShape_EndEvent_1\">\r\n" + 
    		"        <di:waypoint xsi:type=\"dc:Point\" x=\"360.0\" y=\"280.0\"/>\r\n" + 
    		"        <di:waypoint xsi:type=\"dc:Point\" x=\"418.0\" y=\"280.0\"/>\r\n" + 
    		"        <di:waypoint xsi:type=\"dc:Point\" x=\"476.0\" y=\"280.0\"/>\r\n" + 
    		"        <bpmndi:BPMNLabel id=\"BPMNLabel_6\"/>\r\n" + 
    		"      </bpmndi:BPMNEdge>\r\n" + 
    		"      <bpmndi:BPMNEdge id=\"BPMNEdge_SequenceFlow_2\" bpmnElement=\"SequenceFlow_2\" sourceElement=\"BPMNShape_StartEvent_1\" targetElement=\"BPMNShape_ScriptTask_2\">\r\n" + 
    		"        <di:waypoint xsi:type=\"dc:Point\" x=\"106.0\" y=\"280.0\"/>\r\n" + 
    		"        <di:waypoint xsi:type=\"dc:Point\" x=\"178.0\" y=\"280.0\"/>\r\n" + 
    		"        <di:waypoint xsi:type=\"dc:Point\" x=\"250.0\" y=\"280.0\"/>\r\n" + 
    		"        <bpmndi:BPMNLabel/>\r\n" + 
    		"      </bpmndi:BPMNEdge>\r\n" + 
    		"    </bpmndi:BPMNPlane>\r\n" + 
    		"  </bpmndi:BPMNDiagram>\r\n" + 
    		"</bpmn2:definitions>";
    
    public static void insertTemperatures(Sensor s, int temp_start, int temp_end, int temp_mid, int temp_invert, KieSession session){
    	
    	for(int temp = temp_start, invert = 0; (temp <= temp_mid && invert == 0) || (temp >= temp_end && invert == 1); ){
        	if(temp > temp_invert){
        		invert = 1;
        	}
        	
        	if (temp <= temp_invert && invert == 0)
        		temp++;
        	else
        		temp--;
            session.insert(new TemperatureEvent((double)temp, s.getSensorId()));
        }
    	
    }
    
    private StatelessKieSession getStatelessKieSession(Resource resource) {
        return getStatelessKieSessionFromResource(resource.setSourcePath("src/main/resources/rule.drl").setResourceType(ResourceType.DRL));
    }
    
    private static StatelessKieSession getStatelessKieSessionFromResource(Resource resource) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(resource);

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newStatelessKieSession();
    }
    
    private static KieSession getKieSession(Resource resource) {
        return getKieSessionFromResource(resource.setSourcePath("src/main/resources/my-process.bpmn").setResourceType(ResourceType.BPMN2));
    }
    
    private static KieSession getKieSessionFromResource(Resource resource) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
//        Resource rules = ResourceFactory.newFileResource("src/main/resources/BotoxDrugsScenario.drl");
        
        kfs.write(resource);
        //kfs.write(rules);
        
        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        
        kieBuilder.buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
    }
    
    @Test
    public void TestProcessStringDefinition() {
     
       	KieSession kSession = null;
       	
        try {
        	
        	KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            
            kfs.write(ResourceFactory.newByteArrayResource(process.getBytes("utf-8")).setSourcePath("src/main/resources/my-process.bpmn").setResourceType(ResourceType.BPMN2));
            
            KieBuilder kieBuilder = ks.newKieBuilder(kfs);
            
            kieBuilder.buildAll();

            List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
            if (!errors.isEmpty()) {
                fail("" + errors);
            }

            kSession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
            
            ProcessInstance processInstance = kSession.startProcess("co.pextra.botox.notification");
            
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (kSession != null)
                kSession.dispose();
        }
    }
    
    @Test
    public void TestRuleStringDefinition() {
     String ruleContent =
         "package rules\r\n"+
         "import co.pextra.botox.RuleTest.Message2\r\n" +
             "rule \"myrule\"\r\n" +
            "    \twhen\r\n" +
            "        $id : Message2([\"38196-2\"] contains id)\r\n" +
            "    then\r\n" +
            "\t\tSystem.out.println(\"Works!\");\r\n" +
            "end\r\n";
     
       	KieSession kSession = null;
       	
        try {
        	
        	KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            
            kfs.write(ResourceFactory.newByteArrayResource(ruleContent.getBytes("utf-8")).setSourcePath("src/main/resources/my-rule.drl").setResourceType(ResourceType.DRL));
            //kfs.write(ResourceFactory.newByteArrayResource(ruleContent.getBytes("utf-8")).setSourcePath("src/main/resources/my-process.bpmn").setResourceType(ResourceType.BPMN2));
            
            KieBuilder kieBuilder = ks.newKieBuilder(kfs);
            
            kieBuilder.buildAll();

            List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
            if (!errors.isEmpty()) {
                fail("" + errors);
            }

            kSession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
            
            final Message2 message = new Message2();
            message.setId("38196-2");
            message.setStatus( Message2.HELLO );
            kSession.insert( message );
            
            kSession.fireAllRules();
            
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (kSession != null)
                kSession.dispose();
        }
    }
    
    @Test
    public void Test() throws InterruptedException, ExecutionException {
    	
        KieServices kieServices = KieServices.Factory.get();
       
        KieContainer kContainer = kieServices.getKieClasspathContainer();
        
        Results verifyResults = kContainer.verify();
        for (Message m : verifyResults.getMessages()) {
            LOG.info("{}", m);
        }

        LOG.info("Creating kieBase");

        KieBaseConfiguration config = KieServices.Factory.get().newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        KieBase kieBase = kContainer.newKieBase(config);

        LOG.info("There should be rules: ");
        for ( KiePackage kp : kieBase.getKiePackages() ) {
            for (Rule rule : kp.getRules()) {
                LOG.info("kp " + kp + " rule " + rule.getName());
            }
        }

        LOG.info("Creating kieSession");
        KieSession session = kieBase.newKieSession();

        //SceneApplication app = new SceneApplication("botox-drugs-scenario", session);
        //session.addEventListener(new SCENESessionListener());
        
        StatelessKieSession ksession = getStatelessKieSessionFromResource(ResourceFactory.newByteArrayResource(process.getBytes()).setSourcePath("src/main/resources/rule.rf")
                .setResourceType(ResourceType.DRF));

        //(new RuleTest()).setExec(ksession);
        
        
        //final RuleEngineThread ruleEngineThread = new RuleEngineThread(session);
        //ruleEngineThread.start();
        ProcessInstance processInstance = session.startProcess("co.pextra.botox.notification");

        LOG.info("Now running data");

        //Criar loops para simular dados em 5 sensores diferentes! São necessários 5 sensores.
        Sensor sensor1 = new Sensor(1, "Geladeira 1");
        session.insert(sensor1);
        Sensor sensor2 = new Sensor(2, "Geladeira 1");
        session.insert(sensor2);
        Sensor sensor3 = new Sensor(3, "Geladeira 1");
        session.insert(sensor3);
        Sensor sensor4 = new Sensor(4, "Geladeira 1");
        session.insert(sensor4);
        Sensor sensor5 = new Sensor(5, "Geladeira 1");
        session.insert(sensor5);
        
        //Cada sensor possui uma lista de temperaturas
        insertTemperatures(sensor1, -6, -8, 7, 5, session);
        insertTemperatures(sensor2, -8, -6, 5, 3, session);
        insertTemperatures(sensor3, -7, -9, 7, 5, session);
        insertTemperatures(sensor4, -10, -6, 4, 2, session);
        insertTemperatures(sensor5, -15, -10, 8, 6, session);     
        
        LOG.info("Final checks");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Task());
        
        //Deixa a thread principal rodando por 30 segundos, após esse tempo, ela morre!
        try {
            System.out.println("Started..");
            System.out.println(future.get(30, TimeUnit.SECONDS));
            System.out.println("Finished!");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Terminated!");
        }

        executor.shutdownNow();

//        assertEquals("Size of object in Working Memory is 3", 3, session.getObjects().size());
//        assertTrue("contains red", check.contains("red"));
//        assertTrue("contains green", check.contains("green"));
//        assertTrue("contains blue", check.contains("blue"));

    }
    
    public static class Message2 {
        public static final int HELLO   = 0;
        public static final int GOODBYE = 1;

        private String 			id;
        
        private String          message;

        private int             status;

        public Message2() {

        }

        public String getMessage() {
            return this.message;
        }

        public void setId(final String message) {
            this.id = message;
        }
        
        public void setMessage(final String message) {
            this.message = message;
        }

        public int getStatus() {
            return this.status;
        }
        
        public String getId() {
            return this.id;
        }

        public void setStatus(final int status) {
            this.status = status;
        }

        public static Message doSomething(Message message) {
            return message;
        }

        public boolean isSomething(String msg,
                                   List<Object> list) {
            list.add( this );
            return this.message.equals( msg );
        }
    }
}

class Task implements Callable<String> {
    @Override
    public String call() throws Exception {
    	while (!Thread.interrupted()) {
    	}
		return "Ready";
    }
}