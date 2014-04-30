package io.cloudsoft.usergrid;

import java.util.List;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.webapp.tomcat.TomcatServerImpl;
import brooklyn.util.text.Strings;
import brooklyn.util.text.TemplateProcessor;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public class UsergridTomcatServerImpl extends TomcatServerImpl implements UsergridTomcatServer {

    @Override
    public void init() {
        super.init();
        
        if (getConfig(ROOT_WAR) == null && getConfig(USERGRID_VERSION) != null) {
            // TODO Could do this later in start lifecycle, to inject entity+driver into warUrlTemplate processing
            String usergridVersion = getConfig(USERGRID_VERSION);
            String usergridWarUrlTemplate = getConfig(USERGRID_WAR_URL);
            String usergridWarUrl = TemplateProcessor.processTemplateContents(usergridWarUrlTemplate, ImmutableMap.of("usergridVersion", usergridVersion));
            setConfig(ROOT_WAR, usergridWarUrl);
        }
    }
    
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
