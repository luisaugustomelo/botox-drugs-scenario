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
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
//import org.kie.api.runtime.KieSessionConfiguration;
//import org.kie.api.runtime.conf.ClockTypeOption;
//import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuleTest<T> {
    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);

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
    
    @Test
    public static final void main(String[] args) throws InterruptedException, ExecutionException {
    	
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

        SceneApplication app = new SceneApplication("botox-drugs-scenario", session);

        session.addEventListener(new SCENESessionListener());

        final RuleEngineThread ruleEngineThread = new RuleEngineThread(session);
        ruleEngineThread.start();

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
}

class Task implements Callable<String> {
    @Override
    public String call() throws Exception {
    	while (!Thread.interrupted()) {
    	}
		return "Ready";
    }
}