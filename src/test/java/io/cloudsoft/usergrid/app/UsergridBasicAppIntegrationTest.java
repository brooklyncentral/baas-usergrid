package io.cloudsoft.usergrid.app;

import io.cloudsoft.usergrid.UsergridTomcatServer;

import org.testng.annotations.Test;

import brooklyn.entity.Entity;
import brooklyn.test.EntityTestUtils;

@Test
public class UsergridBasicAppIntegrationTest extends AbstractYamlTest {

    @Test(groups={"Integration"})
    public void testAppComponents() throws Exception {
        Entity app = createStartWaitAndLogApplication("usergrid-basic.yaml");
        
        UsergridTomcatServer appserver = findOnlyEntity(app, UsergridTomcatServer.class);
        EntityTestUtils.assertAttributeEquals(appserver, UsergridTomcatServer.SERVICE_UP, true);
        EntityTestUtils.assertAttributeEqualsEventually(appserver, UsergridTomcatServer.CASSANDRA_AVAILABLE, true);
    }

}
