package io.cloudsoft.usergrid;

import java.util.List;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.webapp.tomcat.TomcatServerImpl;
import brooklyn.event.basic.DependentConfiguration;
import brooklyn.event.feed.http.HttpFeed;
import brooklyn.event.feed.http.HttpPollConfig;
import brooklyn.event.feed.http.HttpValueFunctions;
import brooklyn.location.access.BrooklynAccessUtils;
import brooklyn.util.text.Strings;
import brooklyn.util.text.TemplateProcessor;
import brooklyn.util.time.Duration;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;

public class UsergridTomcatServerImpl extends TomcatServerImpl implements UsergridTomcatServer {

    private volatile HttpFeed httpFeed;
    
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
            // This should only take a couple of minutes, but may take longer than the default 2 minutes
            // specified in BrooklynConfigKeys.START_TIMEOUT
            Entities.waitForServiceUp(dataCenter, Duration.seconds(15 * 60));
            List<String> nodes = dataCenter.getAttribute(CassandraDatacenter.CASSANDRA_CLUSTER_NODES);
            return Joiner.on(",").join(nodes);
        } else {
            return Strings.removeFromStart(url, "cassandra://");
        }
    }
    
    @Override
    public void connectSensors() {
        super.connectSensors();
        HostAndPort hostAndPort = BrooklynAccessUtils.getBrooklynAccessibleAddress(this, getAttribute(Attributes.HTTP_PORT));
        String statusUrl = String.format("http://%s:%s/status", hostAndPort.getHostText(), hostAndPort.getPort());
        setAttribute(STATUS_URL, statusUrl);
        httpFeed = HttpFeed.builder()
            .entity(this)
            .period(2000)
            .baseUri(statusUrl)
            .poll(new HttpPollConfig<Boolean>(CASSANDRA_AVAILABLE)
                    .onSuccess(HttpValueFunctions.jsonContents(new String[] {"status","cassandraAvailable"}, Boolean.class)))
            .build();
    }
    
    @Override
    public void disconnectSensors() {
        super.disconnectSensors();
        if (httpFeed != null) httpFeed.stop();
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
