package io.cloudsoft.usergrid;

import java.util.List;

import com.google.common.base.Joiner;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.webapp.tomcat.TomcatServerImpl;
import brooklyn.util.text.Strings;

public class UsergridTomcatServerImpl extends TomcatServerImpl implements UsergridTomcatServer {

    @Override
    public String getCassandraUrl() {
        String url = getConfig(CASSANDRA_URL);
        CassandraDatacenter dataCenter = getConfig(CASSANDRA_DATACENTER);
        if (!(url == null ^ dataCenter == null)) {
            throw new IllegalStateException("Either cassandra url or datacenter must be set, but not both");
        }
        if (url == null) {
            Entities.waitForServiceUp(dataCenter);
            List<String> nodes = dataCenter.getAttribute(CassandraDatacenter.CASSANDRA_CLUSTER_NODES);
            return Joiner.on(",").join(nodes);
        } else {
            return Strings.removeFromStart(url, "cassandra://");
        }
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
