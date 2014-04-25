package io.cloudsoft.usergrid;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.webapp.tomcat.TomcatServerImpl;
import brooklyn.util.text.Strings;

public class UsergridTomcatServerImpl extends TomcatServerImpl implements UsergridTomcatServer {

    @Override
    public String getCassandraUrl() {
        String url = getConfig(CASSANDRA_URL);
        if (url == null)
            return null;
        return Strings.removeFromStart(url, "cassandra://");
    }
    
    @Override
    public String getHostAddress() {
        return getAttribute(Attributes.ADDRESS);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Class getDriverInterface() {
        return UsergridTomcat7Driver.class;
    }
    
}
