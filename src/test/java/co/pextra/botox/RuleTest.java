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

//import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;

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
import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//import co.pextra.botox.*;

public class RuleTest {
    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);

    @Test
    public void test() {
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

        //SceneApplication app = new SceneApplication("fraud-scenario", session);

        session.addEventListener(new SCENESessionListener());

        final RuleEngineThread ruleEngineThread = new RuleEngineThread(session);
        ruleEngineThread.start();

        LOG.info("Now running data");

        Data temp1 = new Data(-3.2, 1);
        session.insert(temp1);
        for (int i = 0; i < 15; i++) {
            session.insert(new TemperatureEvent(temp1.getTemperature(), temp1.getSensorId()));
        }

        Data temp2 = new Data(-8, 1);
        session.insert(temp2);
        for (int i = 0; i < 2; i++) {
//            session.insert(new TransactionEvent(client2.getId(), 2001.0));
            session.insert(new TemperatureEvent(temp2.getTemperature(), temp2.getSensorId()));
        }

        Data temp3 = new Data(-4, 1);
        session.insert(temp3);

        EntryPoint lowTemp = session.getEntryPoint("low temperature");
        for (int i = 0; i < 11; i++) {
            //smallClients.insert(new TransactionEvent(client3.getId(), 20.0));
        	lowTemp.insert(new TemperatureEvent(temp3.getTemperature(), temp3.getSensorId()));
        }
        
        Data temp4 = new Data(1, 1);
        session.insert(temp4);

        EntryPoint highTemp = session.getEntryPoint("high temperature");
        for (int i = 0; i < 101; i++) {
//            bigClients.insert(new TransactionEvent(client4.getId(), 20.0));
        	highTemp.insert(new TemperatureEvent(temp4.getTemperature(), temp4.getSensorId()));
        }

        LOG.info("Final checks");

        while(true);

//        assertEquals("Size of object in Working Memory is 3", 3, session.getObjects().size());
//        assertTrue("contains red", check.contains("red"));
//        assertTrue("contains green", check.contains("green"));
//        assertTrue("contains blue", check.contains("blue"));

    }
}