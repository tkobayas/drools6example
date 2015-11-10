package com.sample;


import junit.framework.TestCase;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class CallKjarTest extends TestCase {

    @Test
	public void testProcess() {
                
        String groupId = "com.sample";
        String artifactId = "drools-mvn-kjar";
        String version = "1.0.0-SNAPSHOT";
        
		KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(groupId, artifactId, version);
        KieContainer kcontainer = ks.newKieContainer(releaseId);
        
        KieSession ksession = kcontainer.newKieSession();
        ksession.insert(new String("John")); // This project intentionally doesn't have dependency on MyPojo class (drools-mvn-kjar) for test purpose
        ksession.fireAllRules();
        
        ksession.dispose();
	}

}