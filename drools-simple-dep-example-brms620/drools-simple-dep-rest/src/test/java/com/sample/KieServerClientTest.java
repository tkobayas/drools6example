package com.sample;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

public class KieServerClientTest extends TestCase {

    private static final String BASE_URL = "http://localhost:8080/kie-server/services/rest/server";
    private static final String USERNAME = "kieserver";
    private static final String PASSWORD = "kieserver1!";

    private static final String GROUP_ID = "com.sample";
    private static final String ARTIFACT_ID = "drools-simple-dep-kjar";
    private static final String VERSION = "1.0.0-SNAPSHOT";

    private static final String COMTAINER_ID = "MyContainer";

    @Test
    public void testProcess() {

        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(BASE_URL, USERNAME, PASSWORD);
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(MyPojo.class);
        config.addJaxbClasses(classes);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

        // make sure to dispose and create
        ServiceResponse<KieContainerResource> info = client.getContainerInfo(COMTAINER_ID);
        KieContainerResource result = info.getResult();
        System.out.println("Result: " + result);
        if (result != null) {
            client.disposeContainer(COMTAINER_ID);

        }
        ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        KieContainerResource resource = new KieContainerResource(COMTAINER_ID, releaseId);
        client.createContainer(COMTAINER_ID, resource);

        RuleServicesClient ruleClient = client.getServicesClient(RuleServicesClient.class);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();

        MyPojo myPojo = new MyPojo("John");

        commands.add(commandsFactory.newInsert(myPojo, "fact-1"));
        commands.add(commandsFactory.newFireAllRules("fire-result"));

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands);

        ServiceResponse<String> response = ruleClient.executeCommands(COMTAINER_ID, batchExecution);

        System.out.println(response);
    }

}