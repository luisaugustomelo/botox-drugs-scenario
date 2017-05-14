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



//import static org.junit.Assert.*;

//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;

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


//import co.pextra.botox.*;

public class RuleTest<T> {
    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);

    @Test
    public void test() throws InterruptedException, ExecutionException {
    	
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

        Data temp1 = new Data(-5, 1);
        session.insert(temp1);
        Data temp2 = new Data(-2.5, 1);
        session.insert(temp2);
        Data temp3 = new Data(-2.2, 1);
        session.insert(temp3);
        Data temp4 = new Data(1, 1);
        session.insert(temp4);
        Data temp5 = new Data(2, 1);
        session.insert(temp5);
        
        session.insert(new TemperatureEvent(temp1.getTemperature(), temp1.getSensorId()));
        session.insert(new TemperatureEvent(temp2.getTemperature(), temp2.getSensorId()));
        session.insert(new TemperatureEvent(temp3.getTemperature(), temp3.getSensorId()));
        session.insert(new TemperatureEvent(temp4.getTemperature(), temp4.getSensorId()));
        session.insert(new TemperatureEvent(temp5.getTemperature(), temp5.getSensorId()));
        

        LOG.info("Final checks");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Task());
        
        //Deixa a thread principal rodando por 30 segundos, após esse tempo, mata ela!
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