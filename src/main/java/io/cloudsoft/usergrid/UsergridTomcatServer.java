package io.cloudsoft.usergrid;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.entity.webapp.tomcat.TomcatServer;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(UsergridTomcatServerImpl.class)
public interface UsergridTomcatServer extends TomcatServer {
    
    @SetFromFlag("cassandraUrl")
    public static final ConfigKey<String> CASSANDRA_URL = ConfigKeys.newStringConfigKey("usergrid.cassandra.url",
        "URL of the Cassandra entity. Used when using a single cassandra node");
    
    @SetFromFlag("cassandraDatacenter")
    public static final ConfigKey<CassandraDatacenter> CASSANDRA_DATACENTER = ConfigKeys.newConfigKey(CassandraDatacenter.class,
        "usergrid.cassandra.datacenter",
        "Cassandra data center to be used as the data store");
    
    @SetFromFlag("usergridPropertiesTemplateUrl")
    ConfigKey<String> USERGRID_PROPERTIES_TEMPLATE_URL = ConfigKeys.newStringConfigKey(
            "usergrid.properties.templateUrl", "Template file (in freemarker format) for the cassandra.yaml config file", 
            "classpath://brooklyn/entity/nosql/cassandra/cassandra.yaml");
    
    public abstract String getCassandraUrl();
    
    public abstract String getHostAddress();
}
