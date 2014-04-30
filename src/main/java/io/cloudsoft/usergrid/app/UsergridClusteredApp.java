package io.cloudsoft.usergrid.app;

import io.cloudsoft.usergrid.AbstractUsergridApplication;
import brooklyn.catalog.Catalog;

@Catalog(
    name = "Usergrid clustered (multi-host) deployment",
    description = "Cassandra datacenter and a dynamic, controlled (nginx) Tomcat cluster",
    iconUrl = "classpath://usergrid.png"
)
public class UsergridClusteredApp extends AbstractUsergridApplication {

}
