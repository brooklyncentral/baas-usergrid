package io.cloudsoft.usergrid.app;

import io.cloudsoft.usergrid.AbstractUsergridApplication;
import io.cloudsoft.usergrid.UsergridTomcatServer;

import java.util.Collection;

import brooklyn.catalog.Catalog;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.database.DatastoreMixins;
import brooklyn.entity.nosql.cassandra.CassandraNode;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.event.basic.DependentConfiguration;
import brooklyn.location.Location;

import com.google.common.base.Preconditions;

@Catalog(
    name = "Usergrid basic (2-host) app",
    description = "Deploys usergrid 2-host configuration",
    iconUrl = "classpath://usergrid.png"
)
public class UsergridBasicApp extends AbstractUsergridApplication {
    
    @Override
    public void init() {
        setDisplayName("Usergrid basic (2-host) deployment");
        
        CassandraNode cassandraNode = addChild(EntitySpec.create(CassandraNode.class));
        String propertiesUrl = getConfig(UsergridTomcatServer.USERGRID_PROPERTIES_TEMPLATE_URL);
        if (propertiesUrl == null) {
            propertiesUrl = "classpath://usergrid-basic.properties";
        }
        addChild(EntitySpec.create(UsergridTomcatServer.class)
            .configure(UsergridTomcatServer.CASSANDRA_URL, 
                DependentConfiguration.attributeWhenReady(cassandraNode, DatastoreMixins.DATASTORE_URL))
            .configure(SoftwareProcess.SUGGESTED_VERSION, "7.0.53")
            .configure(UsergridTomcatServer.USERGRID_PROPERTIES_TEMPLATE_URL, propertiesUrl)
            .configure(UsergridTomcatServer.ROOT_WAR, "http://search.maven.org/remotecontent?filepath=org/usergrid/usergrid-rest/0.0.27.1/usergrid-rest-0.0.27.1.war"));
    }
    
    @Override
    protected void doStart(Collection<? extends Location> locations) {
        Preconditions.checkArgument(locations.size() == 1, "expected 1 location but got %s", locations.size());
        super.doStart(locations);
    }
}
