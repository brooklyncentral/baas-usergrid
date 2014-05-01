package io.cloudsoft.usergrid.app;

import io.cloudsoft.usergrid.AbstractUsergridApplication;
import io.cloudsoft.usergrid.UsergridTomcatServer;

import java.util.Collection;

import brooklyn.catalog.Catalog;
import brooklyn.catalog.CatalogConfig;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.group.Cluster;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.entity.webapp.ControlledDynamicWebAppCluster;
import brooklyn.location.Location;

import com.google.common.base.Preconditions;

@Catalog(
    name = "Usergrid clustered (multi-host) deployment",
    description = "Cassandra datacenter and a dynamic, controlled (nginx) Tomcat cluster",
    iconUrl = "classpath://usergrid.png"
)
public class UsergridClusteredApp extends AbstractUsergridApplication {

    @CatalogConfig(label="Number of Cassandra Nodes")
    public static final ConfigKey<Integer> CASSANDRA_NODES = ConfigKeys.newIntegerConfigKey("usergrid.app.clustered.cassandra.nodes", "Number of Cassandra nodes", 3);
    @CatalogConfig(label="Number of Tomcat Nodes")
    public static final ConfigKey<Integer> TOMCAT_NODES = ConfigKeys.newIntegerConfigKey("usergrid.app.clustered.tomcat.nodes", "Number of Tomcat nodes", 1);
    @CatalogConfig(label="Properties template URL")
    public static final ConfigKey<String> PROPERTIES_TEMPLATE_URL = ConfigKeys.newStringConfigKey("usergrid.app.clustered.properties.url", 
        "Properties file freemarker template URL", 
        "classpath://usergrid-clustered.properties");
    
    @Override
    public void init() {
        setDisplayName("Usergrid clustered (muti-host deployment");
        
        CassandraDatacenter cassandraDatacenter = addChild(EntitySpec.create(CassandraDatacenter.class)
            .configure(Cluster.INITIAL_SIZE, getConfig(CASSANDRA_NODES))
        );
        addChild(EntitySpec.create(ControlledDynamicWebAppCluster.class)
            .configure(ControlledDynamicWebAppCluster.INITIAL_SIZE, getConfig(TOMCAT_NODES))
            .configure(ControlledDynamicWebAppCluster.MEMBER_SPEC, EntitySpec.create(UsergridTomcatServer.class)
                .configure(UsergridTomcatServer.CASSANDRA_DATACENTER, cassandraDatacenter)
                .configure(SoftwareProcess.SUGGESTED_VERSION, "7.0.53")
                .configure(UsergridTomcatServer.USERGRID_PROPERTIES_TEMPLATE_URL, getConfig(PROPERTIES_TEMPLATE_URL))
                .configure(UsergridTomcatServer.ROOT_WAR, "classpath://ROOT.war")
        ));
    }
    
    @Override
    protected void doStart(Collection<? extends Location> locations) {
        Preconditions.checkArgument(locations.size() == 1, "expected 1 location but got %s", locations.size());
        super.doStart(locations);
    }
}
